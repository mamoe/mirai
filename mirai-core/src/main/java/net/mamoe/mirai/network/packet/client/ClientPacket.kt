package net.mamoe.mirai.network.packet.client

import lombok.Getter
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.util.*
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.security.MessageDigest

/**
 * @author Him188moe @ Mirai Project
 */
@ExperimentalUnsignedTypes
abstract class ClientPacket : ByteArrayDataOutputStream(), Packet {
    @Getter
    val packageId: String

    init {
        val annotation = this.javaClass.getAnnotation(PacketId::class.java)
        packageId = annotation.value

        try {
            this.writeHex(Protocol.head)
            this.writeHex(Protocol.ver)
            this.writePacketId()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Throws(IOException::class)
    fun writePacketId() {
        this.writeHex(this@ClientPacket.packageId)
    }

    /**
     * Encode this packet.
     *
     *
     * Before sending the packet, an [tail][Protocol.tail] will be added.
     */
    @Throws(IOException::class)
    abstract fun encode()

    @Throws(IOException::class)
    fun encodeToByteArray(): ByteArray {
        encode()
        return toByteArray()
    }

}


@Throws(IOException::class)
fun DataOutputStream.writeIP(ip: String) {
    for (s in ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        this.writeByte(s.toInt())
    }
}


@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeHex(hex: String) {
    for (s in hex.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        if (s.isEmpty()) {
            continue
        }
        this.writeByte(s.toUByte(16).toByte().toInt())
    }
}

@ExperimentalUnsignedTypes
fun DataOutputStream.writeVarInt(dec: UInt) {
    /*.判断开始 (n ＜ 256)
    返回 (取文本右边 (“0” ＋ 取十六进制文本 (n), 2))
    .判断 (n ≥ 256)
    hex ＝ 取文本右边 (“0” ＋ 取十六进制文本 (n), 4)
    返回 (取文本左边 (hex, 2) ＋ “ ” ＋ 取文本右边 (hex, 2))
    .默认
    返回 (“”)
    .判断结束*/

    if (dec < 256u) {
        this.writeByte(dec.toByte().toInt())//drop other bits
    }

    if (dec > 256u) {
        this.writeShort(dec.toShort().toInt())
    }

    throw UnsupportedOperationException()
}

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeTLV0006(qq: Int, password: String, loginTime: Int, loginIP: String, tgtgtKey: ByteArray) {
    ByteArrayDataOutputStream().let {
        it.writeRandom(4)
        it.writeHex("00 02")
        it.writeQQ(qq)
        it.writeHex(Protocol._0825data2)
        it.writeHex("00 00 01")

        val md5_1 = md5(password);
        val md5_2 = md5(md5_1 + "00 00 00 00".hexToBytes() + qq.toByteArray())
        it.write(md5_1)
        it.writeInt(loginTime)//todo FIXED 12(maybe 11???) bytes??? check that
        it.writeByte(0);
        it.writeZero(4 * 3)
        it.writeIP(loginIP)
        it.writeHex("00 10")
        it.writeHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")
        it.write(tgtgtKey)
        this.write(TEACryptor.encrypt(md5_2, it.toByteArray()))
    }
}

@ExperimentalUnsignedTypes
fun DataOutputStream.writeCRC32() {
    getRandomKey(16).let {
        write(it)//key
        writeLong(getCrc32(it))//todo may be int? check that.
    }
}

fun DataOutputStream.writeHostname() {
    val hostName: String = InetAddress.getLocalHost().hostName.let { it.substring(0, it.length - 3) };
    this.writeShort(hostName.length / 2);//todo check that
    this.writeShort(hostName.length);
    this.writeBytes(hostName)//todo 这个对吗?
}

fun Int.toByteArray(): ByteArray = byteArrayOf(//todo 检查这方法对不对, 这其实就是从 DataInputStream copy来的
        (this.ushr(24) and 0xFF).toByte(),
        (this.ushr(16) and 0xFF).toByte(),
        (this.ushr(8) and 0xFF).toByte(),
        (this.ushr(0) and 0xFF).toByte()
)

@ExperimentalUnsignedTypes
fun Int.toHexString(separator: String = " "): String = this.toByteArray().toUByteArray().toHexString(separator);

private fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())

private fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeZero(count: Int) {
    for (x in 0..count) {
        this.writeByte(0)
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeRandom(length: Int) {
    for (i in 0 until length) {
        this.writeByte((Math.random() * 255).toInt().toByte().toInt())
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeQQ(qq: Int) {
    this.writeInt(qq)
}