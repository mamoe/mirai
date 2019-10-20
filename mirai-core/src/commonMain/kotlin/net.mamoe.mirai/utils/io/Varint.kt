@file:JvmName("Varint")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlin.experimental.or
import kotlin.jvm.JvmName

/**
 * Tool class for VarInt or VarLong operations.
 *
 * Some code from http://wiki.vg/Protocol.
 *
 * @author MagicDroidX of Nukkit Project
 * @author lmlstarqaq of Nukkit Project
 */

fun encodeZigZag32(signedInt: Int): Long {
    return (signedInt shl 1 xor (signedInt shr 31)).toLong()
}


//@JvmSynthetic
fun decodeZigZag32(uint: UInt): Int {
    return decodeZigZag32(uint.toLong())
}

fun decodeZigZag32(uint: Long): Int {
    return (uint shr 1).toInt() xor -(uint and 1).toInt()
}

fun encodeZigZag64(signedLong: Long): Long {
    return signedLong shl 1 xor (signedLong shr 63)
}

fun decodeZigZag64(signedLong: Long): Long {
    return signedLong.ushr(1) xor -(signedLong and 1)
}


fun ByteReadPacket.readVarInt(): Int {
    return decodeZigZag32(this.readUnsignedVarInt())
}


fun ByteReadPacket.readUnsignedVarInt(): UInt {
    return read(this, 5).toUInt()
}


fun ByteReadPacket.readVarLong(): Long {
    return decodeZigZag64(readUnsignedVarLong().toLong())
}


fun ByteReadPacket.readUnsignedVarLong(): ULong {
    return read(this, 10).toULong()
}

fun BytePacketBuilder.writeVarInt(signedInt: Int) {
    this.writeUVarInt(encodeZigZag32(signedInt))
}

fun BytePacketBuilder.writeUVarInt(uint: UInt) {
    return writeUVarInt(uint.toLong())
}

fun BytePacketBuilder.writeUVarInt(uint: Long) {
    this.write0(uint)
}

fun BytePacketBuilder.writeVarLong(signedLong: Long) {
    this.writeUVarLong(encodeZigZag64(signedLong))
}

fun BytePacketBuilder.writeUVarLong(ulong: Long) {
    this.write0(ulong)
}


private fun BytePacketBuilder.write0(long: Long) {
    var value = long
    do {
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0L) {
            temp = temp or 128.toByte()
        }
        this.writeByte(temp)
    } while (value != 0L)
}

private fun read(stream: ByteReadPacket, maxSize: Int): Long {
    var value: Long = 0
    var size = 0
    var b = stream.readByte().toInt()
    while (b and 0x80 == 0x80) {
        value = value or ((b and 0x7F).toLong() shl size++ * 7)
        require(size < maxSize) { "VarLong too bigger(expecting maxSize=$maxSize)" }
        b = stream.readByte().toInt()
    }

    return value or ((b and 0x7F).toLong() shl size * 7)
}