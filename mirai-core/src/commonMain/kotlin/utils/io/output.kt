/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.internal.utils.io

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.utils.coerceAtMostOrFail
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.withUse
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

internal fun BytePacketBuilder.writeResource(
    resource: ExternalResource,
    close: Boolean = false,
): Long = resource.input().withUse { copyTo(this@writeResource) }.also {
    if (close) resource.close()
}

internal inline fun BytePacketBuilder.writeShortLVByteArray(byteArray: ByteArray): Int {
    this.writeShort(byteArray.size.toShort())
    this.writeFully(byteArray)
    return byteArray.size
}

internal inline fun BytePacketBuilder.writeIntLVPacket(
    tag: UByte? = null,
    lengthOffset: ((Long) -> Long) = { it },
    crossinline builder: BytePacketBuilder.() -> Unit,
): Int =
    buildPacket(builder).use {
        if (tag != null) writeByte(tag.toByte())
        val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFFFFFL)
        writeInt(length.toInt())
        writePacket(it)
        return length.toInt()
    }

internal inline fun BytePacketBuilder.writeShortLVPacket(
    tag: UByte? = null,
    lengthOffset: ((Long) -> Long) = { it },
    crossinline builder: BytePacketBuilder.() -> Unit,
): Int = buildPacket(builder).use {
    if (tag != null) writeByte(tag.toByte())
    val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFFFFFL)
    writeShort(length.toUShort().toShort())
    writePacket(it)
    return length.toInt()
}

internal inline fun BytePacketBuilder.writeShortLVString(str: String) = writeShortLVByteArray(str.toByteArray())

internal fun BytePacketBuilder.writeHex(uHex: String) {
    uHex.split(" ").forEach {
        if (it.isNotBlank()) {
            writeByte(it.toUByte(16).toByte())
        }
    }
}


internal inline fun BytePacketBuilder.encryptAndWrite(
    key: ByteArray,
    crossinline encoder: BytePacketBuilder.() -> Unit
) = TEA.encrypt(buildPacket(encoder), key) { decrypted -> writeFully(decrypted) }