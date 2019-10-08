@file:JvmName("Varint")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.experimental.or

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


@JvmSynthetic
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


@Throws(IOException::class)
fun DataInputStream.readVarInt(): Int {
    return decodeZigZag32(this.readUnsignedVarInt())
}


@Throws(IOException::class)
fun DataInputStream.readUnsignedVarInt(): UInt {
    return read(this, 5).toUInt()
}


@Throws(IOException::class)
fun DataInputStream.readVarLong(): Long {
    return decodeZigZag64(readUnsignedVarLong().toLong())
}


@Throws(IOException::class)
fun DataInputStream.readUnsignedVarLong(): ULong {
    return read(this, 10).toULong()
}

@Throws(IOException::class)
fun DataOutputStream.writeVarInt(signedInt: Int) {
    this.writeUVarInt(encodeZigZag32(signedInt))
}

@Throws(IOException::class)
fun DataOutputStream.writeUVarInt(uint: UInt) {
    return writeUVarInt(uint.toLong())
}

@Throws(IOException::class)
fun DataOutputStream.writeUVarInt(uint: Long) {
    this.write0(uint)
}

@Throws(IOException::class)
fun DataOutputStream.writeVarLong(signedLong: Long) {
    this.writeUVarLong(encodeZigZag64(signedLong))
}

@Throws(IOException::class)
fun DataOutputStream.writeUVarLong(ulong: Long) {
    this.write0(ulong)
}


@Throws(IOException::class)
private fun DataOutputStream.write0(long: Long) {
    var value = long
    do {
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0L) {
            temp = temp or 128.toByte()
        }
        this.writeByte(temp.toInt())
    } while (value != 0L)
}

@Throws(IOException::class)
private fun read(stream: DataInputStream, maxSize: Int): Long {
    var value: Long = 0
    var size = 0
    var b = stream.readByte().toInt()
    while (b and 0x80 == 0x80) {
        value = value or ((b and 0x7F).toLong() shl size++ * 7)
        require(size < maxSize) { "VarLong too big" }
        b = stream.readByte().toInt()
    }

    return value or ((b and 0x7F).toLong() shl size * 7)
}

@Throws(IOException::class)
private fun read(stream: InputStream, maxSize: Int): Long {
    var value: Long = 0
    var size = 0
    var b = stream.read()
    while (b and 0x80 == 0x80) {
        value = value or ((b and 0x7F).toLong() shl size++ * 7)
        require(size < maxSize) { "VarLong too big" }
        b = stream.read()
    }

    return value or ((b and 0x7F).toLong() shl size * 7)
}