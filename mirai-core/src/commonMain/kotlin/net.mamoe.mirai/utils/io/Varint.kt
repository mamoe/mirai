/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Varint")
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.Input
import kotlinx.io.core.Output
import kotlin.experimental.or
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * Tool class for VarInt or VarLong operations.
 *
 * Some code from http://wiki.vg/Protocol.
 *
 * Source project: [Nukkit](http://github.com/nukkit/nukkit)
 *
 * @author MagicDroidX from Nukkit Project
 * @author lmlstarqaq from Nukkit Project
 */

internal fun encodeZigZag32(signedInt: Int): Long {
    return (signedInt shl 1 xor (signedInt shr 31)).toLong()
}

@JvmSynthetic
internal fun decodeZigZag32(uint: UInt): Int {
    return decodeZigZag32(uint.toLong())
}

internal fun decodeZigZag32(uint: Long): Int {
    return (uint shr 1).toInt() xor -(uint and 1).toInt()
}

internal fun encodeZigZag64(signedLong: Long): Long {
    return signedLong shl 1 xor (signedLong shr 63)
}

internal fun decodeZigZag64(signedLong: Long): Long {
    return signedLong.ushr(1) xor -(signedLong and 1)
}


inline class UVarInt(
    val data: UInt
)

@JvmSynthetic
fun Input.readUVarInt(): UInt {
    return read(this, 5).toUInt()
}


fun Input.readVarLong(): Long {
    return decodeZigZag64(readUVarLong().toLong())
}


@JvmSynthetic
fun Input.readUVarLong(): ULong {
    return read(this, 10).toULong()
}

fun Output.writeVarInt(signedInt: Int) {
    this.writeUVarInt(encodeZigZag32(signedInt))
}

@JvmSynthetic
fun Output.writeUVarInt(uint: UInt) {
    return writeUVarInt(uint.toLong())
}

fun Output.writeUVarInt(uint: Long) {
    this.write0(uint)
}

fun Output.writeVarLong(signedLong: Long) {
    this.writeUVarLong(encodeZigZag64(signedLong))
}

fun Output.writeUVarLong(ulong: Long) {
    this.write0(ulong)
}

fun UVarInt.toByteArray(): ByteArray {
    val list = mutableListOf<Byte>()
    var value = this.data.toLong()
    do {
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0L) {
            temp = temp or 128.toByte()
        }
        list += temp
    } while (value != 0L)
    return list.toByteArray()
}

fun UVarInt.toUHexString(separator: String = " "): String = buildString {
    var value = data.toLong()

    var isFirst = true
    do {
        if (!isFirst) {
            append(separator)
        }
        var temp = (value and 127).toByte()
        value = value ushr 7
        if (value != 0L) {
            temp = temp or 128.toByte()
        }
        append(temp.toUByte().fixToUHex())
        isFirst = false
    } while (value != 0L)
}

private fun Output.write0(long: Long) {
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

private fun read(stream: Input, maxSize: Int): Long {
    var value: Long = 0
    var size = 0
    var b = stream.readByte().toInt()
    while (b and 0x80 == 0x80) {
        value = value or ((b and 0x7F).toLong() shl size++ * 7)
        require(size < maxSize) { "VarLong too big(expecting maxSize=$maxSize)" }
        b = stream.readByte().toInt()
    }

    return value or ((b and 0x7F).toLong() shl size * 7)
}