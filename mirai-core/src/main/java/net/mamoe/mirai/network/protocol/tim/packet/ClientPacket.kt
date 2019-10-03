@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import lombok.Getter
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.PacketNameFormatter.adjustName
import net.mamoe.mirai.utils.*
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.security.MessageDigest

/**
 * @author Him188moe
 */

abstract class ClientPacket : ByteArrayDataOutputStream(), Packet {
    @Getter
    val idHex: String

    private var encoded: Boolean = false

    init {
        val annotation = this.javaClass.getAnnotation(PacketId::class.java)
        idHex = annotation.value.trim()

        try {
            this.writeHex(TIMProtocol.head)
            this.writeHex(TIMProtocol.ver)
            this.writePacketId()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    fun writePacketId() {
        this.writeHex(this@ClientPacket.idHex)
    }

    /**
     * Encode this packet.
     *
     *
     * Before sending the packet, a [tail][TIMProtocol.tail] will be added.
     */
    @Throws(IOException::class)
    protected abstract fun encode()

    fun encodePacket() {
        if (encoded) {
            return
        }
        encode()
        writeHex(TIMProtocol.tail)
    }

    @Throws(IOException::class)
    fun encodeToByteArray(): ByteArray {
        encodePacket()
        return toByteArray()
    }

    open fun getFixedId(): String = when (this.idHex.length) {
        0 -> "__ __ __ __"
        2 -> this.idHex + " __ __ __"
        5 -> this.idHex + " __ __"
        7 -> this.idHex + " __"
        else -> this.idHex
    }


    override fun toString(): String {
        return adjustName(this.javaClass.simpleName + "(${this.getFixedId()})") + this.getAllDeclaredFields().filterNot { it.name == "idHex" || it.name == "idByteArray" || it.name == "encoded" }.joinToString(", ", "{", "}") {
            it.trySetAccessible(); it.name + "=" + it.get(this).let { value ->
            when (value) {
                null -> null
                is ByteArray -> value.toUHexString()
                is UByteArray -> value.toUHexString()
                else -> value.toString()
            }
        }
        }
    }
}


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

@Throws(IOException::class)
fun DataOutputStream.writeHex(uHex: String) {
    for (s in uHex.trim().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        if (s.isEmpty()) {
            continue
        }
        this.writeByte(s.toUByte(16).toInt())
    }
}

fun DataOutputStream.encryptAndWrite(byteArray: ByteArray, key: ByteArray) {
    this.write(TEA.encrypt(byteArray, key))
}

fun DataOutputStream.encryptAndWrite(key: ByteArray, encoder: ByteArrayDataOutputStream.() -> Unit) {
    this.write(TEA.encrypt(ByteArrayDataOutputStream().apply(encoder).use { it.toByteArray() }, key))
}

fun DataOutputStream.encryptAndWrite(keyHex: String, encoder: ByteArrayDataOutputStream.() -> Unit) {
    this.encryptAndWrite(keyHex.hexToBytes(), encoder)
}

@Throws(IOException::class)
fun DataOutputStream.writeTLV0006(qq: Long, password: String, loginTime: Int, loginIP: String, privateKey: ByteArray) {
    val firstMD5 = md5(password)
    val secondMD5 = md5(firstMD5 + "00 00 00 00".hexToBytes() + qq.toUInt().toByteArray())

    this.encryptAndWrite(secondMD5) {
        writeRandom(4)
        writeHex("00 02")
        writeQQ(qq)
        writeHex(TIMProtocol.constantData2)
        writeHex("00 00 01")

        write(firstMD5)
        writeInt(loginTime)
        writeByte(0)
        writeZero(4 * 3)
        writeIP(loginIP)
        writeZero(8)
        writeHex("00 10")//这两个hex是passwordSubmissionTLV2的末尾
        writeHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")//16
        write(privateKey)
    }
}

@Tested
fun DataOutputStream.writeCRC32() = writeCRC32(getRandomByteArray(16))


fun DataOutputStream.writeCRC32(key: ByteArray) {
    write(key)//key
    writeInt(getCrc32(key))
}


@Tested
fun DataOutputStream.writeDeviceName(random: Boolean = false) {
    val deviceName: String = if (random) {
        String(getRandomByteArray(10))
    } else {
        InetAddress.getLocalHost().hostName
    }
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

fun UInt.toByteArray(): ByteArray = byteArrayOf(
        (this.shr(24) and 255u).toByte(),
        (this.shr(16) and 255u).toByte(),
        (this.shr(8) and 255u).toByte(),
        (this.shr(0) and 255u).toByte()
)


fun Int.toUHexString(separator: String = " "): String = this.toByteArray().toUHexString(separator)

fun md5(str: String): ByteArray = MessageDigest.getInstance("MD5").digest(str.toByteArray())

fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)


@Throws(IOException::class)
fun DataOutputStream.writeZero(count: Int) {
    repeat(count) {
        this.writeByte(0)
    }
}

@Throws(IOException::class)
fun DataOutputStream.writeRandom(length: Int) {
    repeat(length) {
        this.writeByte((Math.random() * 255).toInt())
    }
}


@Throws(IOException::class)
fun DataOutputStream.writeQQ(qq: Long) {
    this.write(qq.toUInt().toByteArray())
}

@Throws(IOException::class)
fun DataOutputStream.writeGroup(groupIdOrGroupNumber: Long) {
    this.write(groupIdOrGroupNumber.toUInt().toByteArray())
}

fun DataOutputStream.writeLVByteArray(byteArray: ByteArray) {
    this.writeShort(byteArray.size)
    this.write(byteArray)
}

fun DataOutputStream.writeLVString(str: String) {
    this.writeLVByteArray(str.toByteArray())
}

fun DataOutputStream.writeLVHex(hex: String) {
    this.writeLVByteArray(hex.hexToBytes())
}