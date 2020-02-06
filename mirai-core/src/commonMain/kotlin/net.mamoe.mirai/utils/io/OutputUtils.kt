@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import net.mamoe.mirai.utils.coerceAtMostOrFail
import net.mamoe.mirai.utils.cryptor.encryptBy
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

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

inline fun BytePacketBuilder.writeShortLVByteArray(byteArray: ByteArray): Int {
    this.writeShort(byteArray.size.toShort())
    this.writeFully(byteArray)
    return byteArray.size
}

inline fun BytePacketBuilder.writeIntLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFL)
        writeInt(length.toInt())
        writePacket(it)
        return length.toInt()
    }

inline fun BytePacketBuilder.writeShortLVPacket(tag: UByte? = null, lengthOffset: ((Long) -> Long) = {it}, builder: BytePacketBuilder.() -> Unit): Int =
    BytePacketBuilder().apply(builder).build().use {
        if (tag != null) writeUByte(tag)
        val length = lengthOffset.invoke(it.remaining).coerceAtMostOrFail(0xFFFFL)
        writeUShort(length.toUShort())
        writePacket(it)
        return length.toInt()
    }

inline fun BytePacketBuilder.writeShortLVString(str: String) = writeShortLVByteArray(str.toByteArray())

fun BytePacketBuilder.writeHex(uHex: String) {
    uHex.split(" ").forEach {
        if (it.isNotBlank()) {
            writeUByte(it.toUByte(16))
        }
    }
}
/**
 * 会使用 [ByteArrayPool] 缓存
 */
inline fun BytePacketBuilder.encryptAndWrite(key: ByteArray, encoder: BytePacketBuilder.() -> Unit) =
    BytePacketBuilder().apply(encoder).build().encryptBy(key) { decrypted -> writeFully(decrypted) }