@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import kotlin.random.Random
import kotlin.random.nextInt


fun BytePacketBuilder.writeZero(count: Int) = repeat(count) { this.writeByte(0) }

fun BytePacketBuilder.writeRandom(length: Int) = repeat(length) { this.writeByte(Random.Default.nextInt(255).toByte()) }

fun BytePacketBuilder.writeQQ(qq: Long) = this.writeUInt(qq.toUInt())

fun BytePacketBuilder.writeGroup(groupIdOrGroupNumber: Long) = this.writeFully(groupIdOrGroupNumber.toUInt().toByteArray())

fun BytePacketBuilder.writeLVByteArray(byteArray: ByteArray) {
    this.writeShort(byteArray.size.toShort())
    this.writeFully(byteArray)
}

fun BytePacketBuilder.writeLVPacket(packet: ByteReadPacket) {
    this.writeShort(packet.remaining.toShort())
    this.writePacket(packet)
    packet.release()
}

fun BytePacketBuilder.writeLVPacket(builder: BytePacketBuilder.() -> Unit) = this.writeLVPacket(BytePacketBuilder().apply(builder).build())

@Suppress("DEPRECATION")
fun BytePacketBuilder.writeLVString(str: String) = this.writeLVByteArray(str.toByteArray())

@Suppress("DEPRECATION")
fun BytePacketBuilder.writeLVHex(hex: String) = this.writeLVByteArray(hex.hexToBytes())

fun BytePacketBuilder.writeIP(ip: String) = writeFully(ip.trim().split(".").map { it.toUByte() }.toUByteArray())

fun BytePacketBuilder.writeTime() = this.writeInt(currentTime.toInt())

fun BytePacketBuilder.writeHex(uHex: String) = this.writeFully(uHex.hexToUBytes())

fun BytePacketBuilder.encryptAndWrite(key: IoBuffer, encoder: BytePacketBuilder.() -> Unit) = encryptAndWrite(key.readBytes(), encoder)
fun BytePacketBuilder.encryptAndWrite(key: ByteArray, encoder: BytePacketBuilder.() -> Unit) = writeFully(TEA.encrypt(BytePacketBuilder().apply(encoder).use { it.build().readBytes() }, key))
fun BytePacketBuilder.encryptAndWrite(keyHex: String, encoder: BytePacketBuilder.() -> Unit) = encryptAndWrite(keyHex.hexToBytes(), encoder)

fun BytePacketBuilder.writeTLV0006(qq: Long, password: String, loginTime: Int, loginIP: String, privateKey: ByteArray) {
    val firstMD5 = md5(password)
    val secondMD5 = md5(firstMD5 + byteArrayOf(0, 0, 0, 0) + qq.toUInt().toByteArray())

    this.encryptAndWrite(secondMD5) {
        writeRandom(4)
        writeHex("00 02")
        writeQQ(qq)
        writeHex(TIMProtocol.constantData2)
        writeHex("00 00 01")

        writeFully(firstMD5)
        writeInt(loginTime)
        writeByte(0)
        writeZero(4 * 3)
        writeIP(loginIP)
        writeZero(8)
        writeHex("00 10")//这两个hex是passwordSubmissionTLV2的末尾
        writeHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")//16
        writeFully(privateKey)
    }
}

@Tested
fun BytePacketBuilder.writeDeviceName(random: Boolean) {
    val deviceName: String = if (random) {
        "DESKTOP-" + String(ByteArray(7) {
            (if (Random.nextBoolean()) Random.nextInt('A'.toInt()..'Z'.toInt())
            else Random.nextInt('1'.toInt()..'9'.toInt())).toByte()
        })
    } else {
        deviceName
    }
    this.writeShort((deviceName.length + 2).toShort())
    this.writeShort(deviceName.length.toShort())
    this.writeStringUtf8(deviceName)//TODO TEST?
}