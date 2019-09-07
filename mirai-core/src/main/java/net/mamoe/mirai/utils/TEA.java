package net.mamoe.mirai.utils;

import net.mamoe.mirai.network.Protocol;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * TEA 加密
 *
 * @author iweiz https://github.com/iweizime/StepChanger/blob/master/app/src/main/java/me/iweizi/stepchanger/qq/Cryptor.java
 */
public final class TEA {
    public static final TEA CRYPTOR_SHARE_KEY = new TEA(Protocol.INSTANCE.hexToBytes(Protocol.shareKey));
    public static final TEA CRYPTOR_0825KEY = new TEA(Protocol.INSTANCE.hexToBytes(Protocol.key0825));

    private static final long UINT32_MASK = 0xffffffffL;
    private final long[] mKey;
    private final Random mRandom;
    private byte[] mOutput;
    private byte[] mInBlock;
    private int mIndexPos;
    private byte[] mIV;
    private int mOutPos;
    private int mPreOutPos;
    private boolean isFirstBlock;

    public TEA(byte[] key) {
        mKey = new long[4];
        for (int i = 0; i < 4; i++) {
            mKey[i] = pack(key, i * 4, 4);
        }
        mRandom = new Random();
        isFirstBlock = true;
    }

    public static byte[] encrypt(byte[] source, byte[] key) {
        return new TEA(key).encrypt(source);
    }

    public static byte[] encrypt(byte[] source, String keyHex) {
        return encrypt(source, UtilsKt.hexToBytes(keyHex));
    }

    public static byte[] decrypt(byte[] source, byte[] key) {
        return new TEA(key).decrypt(source);
    }

    public static byte[] decrypt(byte[] source, String keyHex) {
        return decrypt(source, UtilsKt.hexToBytes(keyHex));
    }

    @SuppressWarnings("SameParameterValue")
    private static long pack(byte[] bytes, int offset, int len) {
        long result = 0;
        int max_offset = len > 8 ? offset + 8 : offset + len;
        for (int index = offset; index < max_offset; index++) {
            result = result << 8 | ((long) bytes[index] & 0xffL);
        }
        return result >> 32 | result & UINT32_MASK;
    }

    private int rand() {
        return mRandom.nextInt();
    }

    private byte[] encode(byte[] bytes) {
        long v0 = pack(bytes, 0, 4);
        long v1 = pack(bytes, 4, 4);
        long sum = 0;
        long delta = 0x9e3779b9L;
        for (int i = 0; i < 16; i++) {
            sum = (sum + delta) & UINT32_MASK;
            v0 += ((v1 << 4) + mKey[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + mKey[1]);
            v0 &= UINT32_MASK;
            v1 += ((v0 << 4) + mKey[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + mKey[3]);
            v1 &= UINT32_MASK;
        }
        return ByteBuffer.allocate(8).putInt((int) v0).putInt((int) v1).array();
    }

    private byte[] decode(byte[] bytes, int offset) {
        long v0 = pack(bytes, offset, 4);
        long v1 = pack(bytes, offset + 4, 4);
        long delta = 0x9e3779b9L;
        long sum = (delta << 4) & UINT32_MASK;
        for (int i = 0; i < 16; i++) {
            v1 -= ((v0 << 4) + mKey[2]) ^ (v0 + sum) ^ ((v0 >>> 5) + mKey[3]);
            v1 &= UINT32_MASK;
            v0 -= ((v1 << 4) + mKey[0]) ^ (v1 + sum) ^ ((v1 >>> 5) + mKey[1]);
            v0 &= UINT32_MASK;
            sum = (sum - delta) & UINT32_MASK;
        }
        return ByteBuffer.allocate(8).putInt((int) v0).putInt((int) v1).array();
    }

    private void encodeOneBlock() {
        for (mIndexPos = 0; mIndexPos < 8; mIndexPos++) {
            mInBlock[mIndexPos] = isFirstBlock ?
                    mInBlock[mIndexPos]
                    : ((byte) (mInBlock[mIndexPos] ^ mOutput[mPreOutPos + mIndexPos]));
        }

        System.arraycopy(encode(mInBlock), 0, mOutput, mOutPos, 8);
        for (mIndexPos = 0; mIndexPos < 8; mIndexPos++) {
            int out_pos = mOutPos + mIndexPos;
            mOutput[out_pos] = (byte) (mOutput[out_pos] ^ mIV[mIndexPos]);
        }
        System.arraycopy(mInBlock, 0, mIV, 0, 8);
        mPreOutPos = mOutPos;
        mOutPos += 8;
        mIndexPos = 0;
        isFirstBlock = false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean decodeOneBlock(byte[] ciphertext, int offset, int len) {
        for (mIndexPos = 0; mIndexPos < 8; mIndexPos++) {
            if (mOutPos + mIndexPos < len) {
                mIV[mIndexPos] = (byte) (mIV[mIndexPos] ^ ciphertext[mOutPos + offset + mIndexPos]);
                continue;
            }
            return true;
        }

        mIV = decode(mIV, 0);
        mOutPos += 8;
        mIndexPos = 0;
        return true;

    }

    @SuppressWarnings("SameParameterValue")
    private byte[] encrypt(byte[] plaintext, int offset, int len) {
        mInBlock = new byte[8];
        mIV = new byte[8];
        mOutPos = 0;
        mPreOutPos = 0;
        isFirstBlock = true;
        mIndexPos = (len + 10) % 8;
        if (mIndexPos != 0) {
            mIndexPos = 8 - mIndexPos;
        }
        mOutput = new byte[mIndexPos + len + 10];
        mInBlock[0] = (byte) (rand() & 0xf8 | mIndexPos);
        for (int i = 1; i <= mIndexPos; i++) {
            mInBlock[i] = (byte) (rand() & 0xff);
        }
        ++mIndexPos;
        for (int i = 0; i < 8; i++) {
            mIV[i] = 0;
        }

        int g = 0;
        while (g < 2) {
            if (mIndexPos < 8) {
                mInBlock[mIndexPos++] = (byte) (rand() & 0xff);
                ++g;
            }
            if (mIndexPos == 8) {
                encodeOneBlock();
            }
        }

        for (; len > 0; len--) {
            if (mIndexPos < 8) {
                mInBlock[mIndexPos++] = plaintext[offset++];
            }
            if (mIndexPos == 8) {
                encodeOneBlock();
            }
        }
        for (g = 0; g < 7; g++) {
            if (mIndexPos < 8) {
                mInBlock[mIndexPos++] = (byte) 0;
            }
            if (mIndexPos == 8) {
                encodeOneBlock();
            }
        }
        return mOutput;
    }

    @SuppressWarnings("SameParameterValue")
    private byte[] decrypt(byte[] cipherText, int offset, int len) {
        if (len % 8 != 0 || len < 16) {
            throw new IllegalArgumentException("must len % 8 == 0 && len >= 16");
        }
        mIV = decode(cipherText, offset);
        mIndexPos = mIV[0] & 7;
        int plen = len - mIndexPos - 10;
        isFirstBlock = true;
        if (plen < 0) {
            return null;
        }
        mOutput = new byte[plen];
        mPreOutPos = 0;
        mOutPos = 8;
        ++mIndexPos;
        int g = 0;
        while (g < 2) {
            if (mIndexPos < 8) {
                ++mIndexPos;
                ++g;
            }
            if (mIndexPos == 8) {
                isFirstBlock = false;
                if (!decodeOneBlock(cipherText, offset, len)) {
                    throw new RuntimeException("Unable to decode");
                }
            }
        }

        for (int outpos = 0; plen != 0; plen--) {
            if (mIndexPos < 8) {
                mOutput[outpos++] = isFirstBlock ?
                        mIV[mIndexPos] :
                        (byte) (cipherText[mPreOutPos + offset + mIndexPos] ^ mIV[mIndexPos]);
                ++mIndexPos;
            }
            if (mIndexPos == 8) {
                mPreOutPos = mOutPos - 8;
                isFirstBlock = false;
                if (!decodeOneBlock(cipherText, offset, len)) {
                    throw new RuntimeException("Unable to decode");
                }
            }
        }
        for (g = 0; g < 7; g++) {
            if (mIndexPos < 8) {
                if ((cipherText[mPreOutPos + offset + mIndexPos] ^ mIV[mIndexPos]) != 0) {
                    throw new RuntimeException();
                } else {
                    ++mIndexPos;
                }
            }

            if (mIndexPos == 8) {
                mPreOutPos = mOutPos;
                if (!decodeOneBlock(cipherText, offset, len)) {
                    throw new RuntimeException("Unable to decode");
                }
            }
        }
        return mOutput;
    }

    public byte[] encrypt(byte[] plaintext) {
        return encrypt(plaintext, 0, plaintext.length);
    }

    public byte[] decrypt(byte[] ciphertext) {
        return decrypt(ciphertext, 0, ciphertext.length);
    }
}