@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.utils.Tested
import net.mamoe.mirai.utils.coerceAtMostOrFail
import net.mamoe.mirai.utils.cryptor.encryptBy
import net.mamoe.mirai.utils.currentTime
import net.mamoe.mirai.utils.deviceName
import kotlin.random.Random
import kotlin.random.nextInt

fun BytePacketBuilder.writeZero(count: Int) {
    require(count != 0) { "Trying to write zero with count 0, you made a mistake?" }
    require(count > 0) { "writeZero: count must > 0" }
    repeat(count) { this.writeByte(0) }
}

fun BytePacketBuilder.writeRandom(length: Int) = repeat(length) { this.writeByte(Random.Default.nextInt(255).toByte()) }

fun BytePacketBuilder.writeQQ(qq: Long) = this.writeUInt(qq.toUInt()) // same bit rep.
fun BytePacketBuilder.writeGroup(groupId: GroupId) = this.writeUInt(groupId.value.toUInt())
fun BytePacketBuilder.writeGroup(groupInternalId: GroupInternalId) = this.writeUInt(groupInternalId.value.toUInt())

fun BytePacketBuilder.writeShortLVByteArrayLimitedLength(array: ByteArray, maxLength: Int) {
    if (array.size <= maxLength) {
        writeShort(array.size.toShort())
        writeFully(array)
    } else {
        writeShort(maxLength.toShort())
        repeat(maxLength) {
            writeByte(array[it])
        }
    }
}

fun BytePacketBuilder.writeShortLVByteArray(byteArray: ByteArray): Int {
    this.writeShort(byteArray.size.toShort())
    this.writeFully(byteArray)
    return byteArray.size
}

inline fun BytePacketBuilder.writeIntLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = (lengthOffset.invoke(it.remaining) ?: it.remaining).coerceAtMostOrFail(0xFFFFL)
        writeInt(length.toInt())
        writePacket(it)
        return length.toInt()
    }

inline fun BytePacketBuilder.writeShortLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = (lengthOffset.invoke(it.remaining) ?: it.remaining).coerceAtMostOrFail(0xFFFFL)
        writeUShort(length.toUShort())
        writePacket(it)
        return length.toInt()
    }

inline fun BytePacketBuilder.writeUVarIntLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit) =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        writeUVarInt((lengthOffset.invoke(it.remaining) ?: it.remaining).coerceAtMostOrFail(0xFFFFL))
        writePacket(it)
    }

fun BytePacketBuilder.writeShortLVString(str: String) = writeShortLVByteArray(str.toByteArray())

fun BytePacketBuilder.writeIP(ip: String) = writeFully(ip.trim().split(".").map { it.toUByte() }.toUByteArray())

fun BytePacketBuilder.writeTime() = this.writeInt(currentTime.toInt())

fun BytePacketBuilder.writeHex(uHex: String) {
    uHex.split(" ").forEach {
        if (it.isNotBlank()) {
            writeUByte(it.toUByte(16))
        }
    }
}


fun BytePacketBuilder.writeTLV(tag: UByte, values: UByteArray) {
    writeUByte(tag)
    writeUVarInt(values.size.toUInt())
    writeFully(values)
}

fun BytePacketBuilder.writeTLV(tag: UByte, values: ByteArray) {
    writeUByte(tag)
    writeUVarInt(values.size.toUInt())
    writeFully(values)
}

fun BytePacketBuilder.writeTHex(tag: UByte, uHex: String) {
    this.writeUByte(tag)
    this.writeFully(uHex.hexToUBytes())
}

fun BytePacketBuilder.writeTV(tagValue: UShort) = writeUShort(tagValue)

fun BytePacketBuilder.writeTV(tag: UByte, value: UByte) {
    writeUByte(tag)
    writeUByte(value)
}

fun BytePacketBuilder.writeTUbyte(tag: UByte, value: UByte) {
    this.writeUByte(tag)
    this.writeUByte(value)
}

fun BytePacketBuilder.writeTUVarint(tag: UByte, value: UInt) {
    this.writeUByte(tag)
    this.writeUVarInt(value)
}

fun BytePacketBuilder.writeTByteArray(tag: UByte, value: ByteArray) {
    this.writeUByte(tag)
    this.writeFully(value)
}

fun BytePacketBuilder.writeTByteArray(tag: UByte, value: UByteArray) {
    this.writeUByte(tag)
    this.writeFully(value)
}

/**
 * 会使用 [ByteArrayPool] 缓存
 */
inline fun BytePacketBuilder.encryptAndWrite(key: ByteArray, encoder: BytePacketBuilder.() -> Unit) =
    BytePacketBuilder().apply(encoder).build().encryptBy(key) { decrypted -> writeFully(decrypted) }

inline fun BytePacketBuilder.encryptAndWrite(key: IoBuffer, encoder: BytePacketBuilder.() -> Unit) = ByteArrayPool.useInstance {
    key.readFully(it, 0, key.readRemaining)
    encryptAndWrite(it, encoder)
}

inline fun BytePacketBuilder.encryptAndWrite(keyHex: String, encoder: BytePacketBuilder.() -> Unit) = encryptAndWrite(keyHex.hexToBytes(), encoder)

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
    this.writeStringUtf8(deviceName)
}