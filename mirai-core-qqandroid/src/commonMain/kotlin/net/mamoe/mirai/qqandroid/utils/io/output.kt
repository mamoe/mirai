/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.qqandroid.utils.io

import kotlinx.io.core.*
import net.mamoe.mirai.qqandroid.utils.coerceAtMostOrFail
import net.mamoe.mirai.qqandroid.utils.cryptor.TEA
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

internal fun BytePacketBuilder.writeShortLVByteArrayLimitedLength(array: ByteArray, maxLength: Int) {
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

internal inline fun BytePacketBuilder.writeShortLVByteArray(byteArray: ByteArray): Int {
    this.writeShort(byteArray.size.toShort())
    this.writeFully(byteArray)
    return byteArray.size
}

internal inline fun BytePacketBuilder.writeIntLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFFFFFL)
        writeInt(length.toInt())
        writePacket(it)
        return length.toInt()
    }

internal inline fun BytePacketBuilder.writeShortLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFFFFFL)
        writeUShort(length.toUShort())
        writePacket(it)
        return length.toInt()
    }

internal inline fun BytePacketBuilder.writeShortLVString(str: String) = writeShortLVByteArray(str.toByteArray())

internal fun BytePacketBuilder.writeHex(uHex: String) {
    uHex.split(" ").forEach {
        if (it.isNotBlank()) {
            writeUByte(it.toUByte(16))
        }
    }
}


internal inline fun BytePacketBuilder.encryptAndWrite(key: ByteArray, encoder: BytePacketBuilder.() -> Unit) =
    TEA.encrypt(BytePacketBuilder().apply(encoder).build(), key) { decrypted -> writeFully(decrypted) }