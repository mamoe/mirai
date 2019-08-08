package net.mamoe.mirai.util;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * 加密解密QQ消息的工具类. QQ消息的加密算法是一个16次的迭代过程，并且是反馈的，每一个加密单元是8字节，输出也是8字节，密钥是16字节
 * 我们以prePlain表示前一个明文块，plain表示当前明文块，crypt表示当前明文块加密得到的密文块，preCrypt表示前一个密文块
 * f表示加密算法，d表示解密算法 那么从plain得到crypt的过程是: crypt = f(plain &circ; preCrypt) &circ;
 * prePlain 所以，从crypt得到plain的过程自然是 plain = d(crypt &circ; prePlain) &circ;
 * preCrypt 此外，算法有它的填充机制，其会在明文前和明文后分别填充一定的字节数，以保证明文长度是8字节的倍数
 * 填充的字节数与原始明文长度有关，填充的方法是:
 *
 * <pre>
 * <code>
 *
 *      ------- 消息填充算法 -----------
 *      a = (明文长度 + 10) mod 8
 *      if(a 不等于 0) a = 8 - a;
 *      b = 随机数 &amp; 0xF8 | a;              这个的作用是把a的值保存了下来
 *      plain[0] = b;                   然后把b做为明文的第0个字节，这样第0个字节就保存了a的信息，这个信息在解密时就要用来找到真正明文的起始位置
 *      plain[1 至 a+2] = 随机数 &amp; 0xFF;    这里用随机数填充明文的第1到第a+2个字节
 *      plain[a+3 至 a+3+明文长度-1] = 明文; 从a+3字节开始才是真正的明文
 *      plain[a+3+明文长度, 最后] = 0;       在最后，填充0，填充到总长度为8的整数为止。到此为止，结束了，这就是最后得到的要加密的明文内容
 *      ------- 消息填充算法 ------------
 *
 * </code>
 * </pre>
 *
 * @author https://juejin.im/post/5c5bdf8cf265da2dc706d532
 * @author luma
 * @author notXX
 */
final class _TEAEncryption {
    // 指向当前的明文块
    private byte[] plain;
    // 这指向前面一个明文块
    private byte[] prePlain;
    // 输出的密文或者明文
    private byte[] out;
    // 当前加密的密文位置和上一次加密的密文块位置，他们相差8
    private int crypt, preCrypt;
    // 当前处理的加密解密块的位置
    private int pos;
    // 填充数
    private int padding;
    // 密钥
    private byte[] key;
    // 用于加密时，表示当前是否是第一个8字节块，因为加密算法是反馈的
    // 但是最开始的8个字节没有反馈可用，所有需要标明这种情况
    private boolean header = true;
    // 这个表示当前解密开始的位置，之所以要这么一个变量是为了避免当解密到最后时
    // 后面已经没有数据，这时候就会出错，这个变量就是用来判断这种情况免得出错
    private int contextStart;
    // 随机数对象
    private static Random random = new Random();
    // 字节输出流
    private ByteArrayOutputStream baos;

    /**
     * 构造函数
     */
    public _TEAEncryption() {
        baos = new ByteArrayOutputStream(8);
    }

    /**
     * 把字节数组从offset开始的len个字节转换成一个unsigned int， 因为java里面没有unsigned，所以unsigned
     * int使用long表示的， 如果len大于8，则认为len等于8。如果len小于8，则高位填0 <br>
     * (edited by notxx) 改变了算法, 性能稍微好一点. 在我的机器上测试10000次, 原始算法花费18s, 这个算法花费12s.
     *
     * @param in     字节数组.
     * @param offset 从哪里开始转换.
     * @param len    转换长度, 如果len超过8则忽略后面的
     * @return
     */
    private static long getUnsignedInt(byte[] in, int offset, int len) {
        long ret = 0;
        int end = 0;
        if (len > 8)
            end = offset + 8;
        else
            end = offset + len;
        for (int i = offset; i < end; i++) {
            ret <<= 8;
            ret |= in[i] & 0xff;
        }
        return (ret & 0xffffffffL) | (ret >>> 32);
    }

    /**
     * 解密
     *
     * @param in     密文
     * @param offset 密文开始的位置
     * @param len    密文长度
     * @param k      密钥
     * @return 明文
     */
    public byte[] decrypt(byte[] in, int offset, int len, byte[] k) {
        // 检查密钥
        if (k == null)
            return null;

        crypt = preCrypt = 0;
        this.key = k;
        int count;
        byte[] m = new byte[offset + 8];

        // 因为QQ消息加密之后至少是16字节，并且肯定是8的倍数，这里检查这种情况
        if ((len % 8 != 0) || (len < 16)) return null;
        // 得到消息的头部，关键是得到真正明文开始的位置，这个信息存在第一个字节里面，所以其用解密得到的第一个字节与7做与
        prePlain = decipher(in, offset);
        pos = prePlain[0] & 0x7;
        // 得到真正明文的长度
        count = len - pos - 10;
        // 如果明文长度小于0，那肯定是出错了，比如传输错误之类的，返回
        if (count < 0) return null;

        // 这个是临时的preCrypt，和加密时第一个8字节块没有prePlain一样，解密时
        // 第一个8字节块也没有preCrypt，所有这里建一个全0的
        for (int i = offset; i < m.length; i++)
            m[i] = 0;
        // 通过了上面的代码，密文应该是没有问题了，我们分配输出缓冲区
        out = new byte[count];
        // 设置preCrypt的位置等于0，注意目前的preCrypt位置是指向m的，因为java没有指针，所以我们在后面要控制当前密文buf的引用
        preCrypt = 0;
        // 当前的密文位置，为什么是8不是0呢？注意前面我们已经解密了头部信息了，现在当然该8了
        crypt = 8;
        // 自然这个也是8
        contextStart = 8;
        // 加1，和加密算法是对应的
        pos++;

        // 开始跳过头部，如果在这个过程中满了8字节，则解密下一块
        // 因为是解密下一块，所以我们有一个语句 m = in，下一块当然有preCrypt了，我们不再用m了
        // 但是如果不满8，这说明了什么？说明了头8个字节的密文是包含了明文信息的，当然还是要用m把明文弄出来
        // 所以，很显然，满了8的话，说明了头8个字节的密文除了一个长度信息有用之外，其他都是无用的填充
        padding = 1;
        while (padding <= 2) {
            if (pos < 8) {
                pos++;
                padding++;
            }
            if (pos == 8) {
                m = in;
                if (!decrypt8Bytes(in, offset, len)) return null;
            }
        }

        // 这里是解密的重要阶段，这个时候头部的填充都已经跳过了，开始解密
        // 注意如果上面一个while没有满8，这里第一个if里面用的就是原始的m，否则这个m就是in了
        int i = 0;
        while (count != 0) {
            if (pos < 8) {
                out[i] = (byte) (m[offset + preCrypt + pos] ^ prePlain[pos]);
                i++;
                count--;
                pos++;
            }
            if (pos == 8) {
                m = in;
                preCrypt = crypt - 8;
                if (!decrypt8Bytes(in, offset, len))
                    return null;
            }
        }

        // 最后的解密部分，上面一个while已经把明文都解出来了，就剩下尾部的填充了，应该全是0
        // 所以这里有检查是否解密了之后是不是0，如果不是的话那肯定出错了，返回null
        for (padding = 1; padding < 8; padding++) {
            if (pos < 8) {
                if ((m[offset + preCrypt + pos] ^ prePlain[pos]) != 0)
                    return null;
                pos++;
            }
            if (pos == 8) {
                m = in;
                preCrypt = crypt;
                if (!decrypt8Bytes(in, offset, len))
                    return null;
            }
        }
        return out;
    }

    /**
     * @param in 需要被解密的密文
     * @param k  密钥
     * @return Message 已解密的消息
     * @paraminLen 密文长度
     */
    public byte[] decrypt(byte[] in, byte[] k) {
        return decrypt(in, 0, in.length, k);
    }

    /**
     * 加密
     *
     * @param in     明文字节数组
     * @param offset 开始加密的偏移
     * @param len    加密长度
     * @param k      密钥
     * @return 密文字节数组
     */
    public byte[] encrypt(byte[] in, int offset, int len, byte[] k) {
        // 检查密钥
        if (k == null)
            return in;

        plain = new byte[8];
        prePlain = new byte[8];
        pos = 1;
        padding = 0;
        crypt = preCrypt = 0;
        this.key = k;
        header = true;

        // 计算头部填充字节数
        pos = (len + 0x0A) % 8;
        if (pos != 0)
            pos = 8 - pos;
        // 计算输出的密文长度
        out = new byte[len + pos + 10];
        // 这里的操作把pos存到了plain的第一个字节里面
        // 0xF8后面三位是空的，正好留给pos，因为pos是0到7的值，表示文本开始的字节位置
        plain[0] = (byte) ((rand() & 0xF8) | pos);

        // 这里用随机产生的数填充plain[1]到plain[pos]之间的内容
        for (int i = 1; i <= pos; i++)
            plain[i] = (byte) (rand() & 0xFF);
        pos++;
        // 这个就是prePlain，第一个8字节块当然没有prePlain，所以我们做一个全0的给第一个8字节块
        for (int i = 0; i < 8; i++)
            prePlain[i] = 0x0;

        // 继续填充2个字节的随机数，这个过程中如果满了8字节就加密之
        padding = 1;
        while (padding <= 2) {
            if (pos < 8) {
                plain[pos++] = (byte) (rand() & 0xFF);
                padding++;
            }
            if (pos == 8)
                encrypt8Bytes();
        }

        // 头部填充完了，这里开始填真正的明文了，也是满了8字节就加密，一直到明文读完
        int i = offset;
        while (len > 0) {
            if (pos < 8) {
                plain[pos++] = in[i++];
                len--;
            }
            if (pos == 8)
                encrypt8Bytes();
        }

        // 最后填上0，以保证是8字节的倍数
        padding = 1;
        while (padding <= 7) {
            if (pos < 8) {
                plain[pos++] = 0x0;
                padding++;
            }
            if (pos == 8)
                encrypt8Bytes();
        }

        return out;
    }

    /**
     * @param in 需要加密的明文
     * @param k  密钥
     * @return Message 密文
     * @paraminLen 明文长度
     */
    public byte[] encrypt(byte[] in, byte[] k) {
        return encrypt(in, 0, in.length, k);
    }

    /**
     * 加密一个8字节块
     *
     * @param in 明文字节数组
     * @return 密文字节数组
     */
    private byte[] encipher(byte[] in) {
        // 迭代次数，16次
        int loop = 0x10;
        // 得到明文和密钥的各个部分，注意java没有无符号类型，所以为了表示一个无符号的整数
        // 我们用了long，这个long的前32位是全0的，我们通过这种方式模拟无符号整数，后面用到的long也都是一样的
        // 而且为了保证前32位为0，需要和0xFFFFFFFF做一下位与
        long y = getUnsignedInt(in, 0, 4);
        long z = getUnsignedInt(in, 4, 4);
        long a = getUnsignedInt(key, 0, 4);
        long b = getUnsignedInt(key, 4, 4);
        long c = getUnsignedInt(key, 8, 4);
        long d = getUnsignedInt(key, 12, 4);
        // 这是算法的一些控制变量，为什么delta是0x9E3779B9呢？
        // 这个数是TEA算法的delta，实际是就是(sqr(5) - 1) * 2^31 (根号5，减1，再乘2的31次方)
        long sum = 0;
        long delta = 0x9E3779B9;
        delta &= 0xFFFFFFFFL;

        // 开始迭代了，乱七八糟的，我也看不懂，反正和DES之类的差不多，都是这样倒来倒去
        while (loop-- > 0) {
            sum += delta;
            sum &= 0xFFFFFFFFL;
            y += ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            y &= 0xFFFFFFFFL;
            z += ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            z &= 0xFFFFFFFFL;
        }

        // 最后，我们输出密文，因为我用的long，所以需要强制转换一下变成int
        baos.reset();
        writeInt((int) y);
        writeInt((int) z);
        return baos.toByteArray();
    }

    /**
     * 解密从offset开始的8字节密文
     *
     * @param in     密文字节数组
     * @param offset 密文开始位置
     * @return 明文
     */
    private byte[] decipher(byte[] in, int offset) {
        // 迭代次数，16次
        int loop = 0x10;
        // 得到密文和密钥的各个部分，注意java没有无符号类型，所以为了表示一个无符号的整数
        // 我们用了long，这个long的前32位是全0的，我们通过这种方式模拟无符号整数，后面用到的long也都是一样的
        // 而且为了保证前32位为0，需要和0xFFFFFFFF做一下位与
        long y = getUnsignedInt(in, offset, 4);
        long z = getUnsignedInt(in, offset + 4, 4);
        long a = getUnsignedInt(key, 0, 4);
        long b = getUnsignedInt(key, 4, 4);
        long c = getUnsignedInt(key, 8, 4);
        long d = getUnsignedInt(key, 12, 4);
        // 算法的一些控制变量，sum在这里也有数了，这个sum和迭代次数有关系
        // 因为delta是这么多，所以sum如果是这么多的话，迭代的时候减减减，减16次，最后
        // 得到0。反正这就是为了得到和加密时相反顺序的控制变量，这样才能解密呀～～
        long sum = 0xE3779B90;
        sum &= 0xFFFFFFFFL;
        long delta = 0x9E3779B9;
        delta &= 0xFFFFFFFFL;

        // 迭代开始了， @_@
        while (loop-- > 0) {
            z -= ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            z &= 0xFFFFFFFFL;
            y -= ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            y &= 0xFFFFFFFFL;
            sum -= delta;
            sum &= 0xFFFFFFFFL;
        }

        baos.reset();
        writeInt((int) y);
        writeInt((int) z);
        return baos.toByteArray();
    }

    /**
     * 写入一个整型到输出流，高字节优先
     *
     * @param t
     */
    private void writeInt(int t) {
        baos.write(t >>> 24);
        baos.write(t >>> 16);
        baos.write(t >>> 8);
        baos.write(t);
    }

    /**
     * 解密
     *
     * @param in 密文
     * @return 明文
     */
    private byte[] decipher(byte[] in) {
        return decipher(in, 0);
    }

    /**
     * 加密8字节
     */
    private void encrypt8Bytes() {
        // 这部分完成我上面所说的 plain ^ preCrypt，注意这里判断了是不是第一个8字节块，如果是的话，那个prePlain就当作preCrypt用
        for (pos = 0; pos < 8; pos++) {
            if (header)
                plain[pos] ^= prePlain[pos];
            else
                plain[pos] ^= out[preCrypt + pos];
        }
        // 这个完成我上面说的 f(plain ^ preCrypt)
        byte[] crypted = encipher(plain);
        // 这个没什么，就是拷贝一下，java不像c，所以我只好这么干，c就不用这一步了
        System.arraycopy(crypted, 0, out, crypt, 8);

        // 这个完成了 f(plain ^ preCrypt) ^ prePlain，ok，下面拷贝一下就行了
        for (pos = 0; pos < 8; pos++)
            out[crypt + pos] ^= prePlain[pos];
        System.arraycopy(plain, 0, prePlain, 0, 8);

        // 完成了加密，现在是调整crypt，preCrypt等等东西的时候了
        preCrypt = crypt;
        crypt += 8;
        pos = 0;
        header = false;
    }

    /**
     * 解密8个字节
     *
     * @param in     密文字节数组
     * @param offset 从何处开始解密
     * @param len    密文的长度
     * @return true表示解密成功
     */
    private boolean decrypt8Bytes(byte[] in, int offset, int len) {
        // 这里第一步就是判断后面还有没有数据，没有就返回，如果有，就执行 crypt ^ prePlain
        for (pos = 0; pos < 8; pos++) {
            if (contextStart + pos >= len)
                return true;
            prePlain[pos] ^= in[offset + crypt + pos];
        }

        // 好，这里执行到了 d(crypt ^ prePlain)
        prePlain = decipher(prePlain);
        if (prePlain == null)
            return false;

        // 解密完成，最后一步好像没做？
        // 这里最后一步放到decrypt里面去做了，因为解密的步骤有点不太一样
        // 调整这些变量的值先
        contextStart += 8;
        crypt += 8;
        pos = 0;
        return true;
    }

    /**
     * 这是个随机因子产生器，用来填充头部的，如果为了调试，可以用一个固定值
     * 随机因子可以使相同的明文每次加密出来的密文都不一样
     *
     * @return 随机因子
     */
    private int rand() {
        return random.nextInt();
    }
}
