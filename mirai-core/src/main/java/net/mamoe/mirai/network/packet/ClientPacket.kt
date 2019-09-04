package net.mamoe.mirai.network.packet

import lombok.Getter
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.security.MessageDigest

/**
 * @author Him188moe
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
     * Before sending the packet, a [tail][Protocol.tail] will be added.
     */
    @Throws(IOException::class)
    abstract fun encode()

    @Throws(IOException::class)
    fun encodeToByteArray(): ByteArray {
        encode()
        return toByteArray()
    }

    override fun toString(): String {
        return this.javaClass.simpleName + this.getAllDeclaredFields().joinToString(", ", "{", "}") {
            it.trySetAccessible(); it.name + "=" + it.get(this).let { value ->
            when (value) {
                is ByteArray -> value.toUHexString()
                is UByteArray -> value.toUHexString()
                else -> value.toString()
            }
        }
        }
    }
}


@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeIP(ip: String) {
    for (s in ip.trim().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        this.writeByte(s.toInt())
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeTime() {
    this.writeInt(System.currentTimeMillis().toInt())
}

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeHex(hex: String) {
    for (s in hex.trim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
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

    when {
        dec < 256u -> this.writeByte(dec.toByte().toInt())//drop other bits
        dec > 256u -> this.writeShort(dec.toShort().toInt())
        else -> throw IllegalArgumentException(dec.toString())
    }
}

fun DataOutputStream.encryptAndWrite(byteArray: ByteArray, key: ByteArray) {
    this.write(TEA.encrypt(byteArray, key))
}

fun DataOutputStream.encryptAndWrite(byteArray: ByteArray, cryptor: TEA) {
    this.write(cryptor.encrypt(byteArray))
}

fun DataOutputStream.encryptAndWrite(key: ByteArray, encoder: (ByteArrayDataOutputStream) -> Unit) {
    this.write(TEA.encrypt(ByteArrayDataOutputStream().let { encoder(it); it.toByteArray() }, key))
}

@ExperimentalUnsignedTypes
fun DataOutputStream.encryptAndWrite(keyHex: String, encoder: (ByteArrayDataOutputStream) -> Unit) {
    this.encryptAndWrite(keyHex.hexToBytes(), encoder)
}

fun DataOutputStream.encryptAndWrite(cryptor: TEA, encoder: (ByteArrayDataOutputStream) -> Unit) {
    this.write(cryptor.encrypt(ByteArrayDataOutputStream().let { encoder(it); it.toByteArray() }))
}

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeTLV0006(qq: Long, password: String, loginTime: Int, loginIP: String, tgtgtKey: ByteArray) {
    ByteArrayDataOutputStream().let {
        it.writeRandom(4)
        it.writeHex("00 02")
        it.writeQQ(qq)
        it.writeHex(Protocol.constantData1)
        it.writeHex("00 00 01")

        val firstMD5 = md5(password)
        val secondMD5 = md5(firstMD5 + "00 00 00 00".hexToBytes() + qq.toUInt().toByteArray())
        it.write(firstMD5)
        it.writeInt(loginTime)
        it.writeByte(0)
        it.writeZero(4 * 3)
        it.writeIP(loginIP)
        it.writeZero(8)
        it.writeHex("00 10")
        it.writeHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")
        it.write(tgtgtKey)
        this.write(TEA.encrypt(it.toByteArray(), secondMD5))
    }
}

fun main() {

}

/*
@ExperimentalUnsignedTypes
fun main() {
    println(lazyEncode { it.writeTLV0006(1994701021, "D1 A5 C8 BB E1 Q3 CC DD", 131513, "123.123.123.123", "AA BB CC DD EE FF AA BB CC".hexToBytes()) }.toUByteArray().toUHexString())
}*/

@ExperimentalUnsignedTypes
@TestedSuccessfully
fun DataOutputStream.writeCRC32() = writeCRC32(getRandomKey(16))


@ExperimentalUnsignedTypes
fun DataOutputStream.writeCRC32(key: ByteArray) {
    key.let {
        write(it)//key
        writeInt(getCrc32(it))
    }
}

@TestedSuccessfully
fun DataOutputStream.writeDeviceName() {
    val deviceName = InetAddress.getLocalHost().hostName
    this.writeShort(deviceName.length + 2)
    this.writeShort(deviceName.length)
    this.writeBytes(deviceName)
}

/**
 * 255 -> 00 00 00 FF
 */
fun Int.toByteArray(): ByteArray = byteArrayOf(
        (this.ushr(24) and 0xFF).toByte(),
        (this.ushr(16) and 0xFF).toByte(),
        (this.ushr(8) and 0xFF).toByte(),
        (this.ushr(0) and 0xFF).toByte()
)

/**
 * 255u -> 00 00 00 FF
 */
@ExperimentalUnsignedTypes
fun UInt.toByteArray(): ByteArray = byteArrayOf(
        (this.shr(24) and 255u).toByte(),
        (this.shr(16) and 255u).toByte(),
        (this.shr(8) and 255u).toByte(),
        (this.shr(0) and 255u).toByte()
)

/**
 * 255 -> FF 00 00 00
 */
fun Int.toLByteArray(): ByteArray = byteArrayOf(
        (this.ushr(0) and 0xFF).toByte(),
        (this.ushr(8) and 0xFF).toByte(),
        (this.ushr(16) and 0xFF).toByte(),
        (this.ushr(24) and 0xFF).toByte()
)

@ExperimentalUnsignedTypes
fun Int.toHexString(separator: String = " "): String = this.toByteArray().toUByteArray().toUHexString(separator)

internal fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())

internal fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeZero(count: Int) {
    repeat(count) {
        this.writeByte(0)
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeRandom(length: Int) {
    repeat(length) {
        this.writeByte((Math.random() * 255).toInt().toByte().toInt())
    }
}

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeQQ(qq: Long) {
    this.write(qq.toUInt().toByteArray())
}

@ExperimentalUnsignedTypes
@Throws(IOException::class)
fun DataOutputStream.writeGroup(groupIdOrGroupNumber: Long) {
    this.write(groupIdOrGroupNumber.toUInt().toByteArray())
}