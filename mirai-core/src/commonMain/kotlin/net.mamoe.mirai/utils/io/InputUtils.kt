@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils.io

import kotlinx.io.OutputStream
import kotlinx.io.charsets.Charset
import kotlinx.io.charsets.Charsets
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.contentToString
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("NOTHING_TO_INLINE")
inline fun Input.discardExact(n: Short) = this.discardExact(n.toInt())

@Suppress("NOTHING_TO_INLINE")
@JvmSynthetic
inline fun Input.discardExact(n: UShort) = this.discardExact(n.toInt())

@Suppress("NOTHING_TO_INLINE")
@JvmSynthetic
inline fun Input.discardExact(n: UByte) = this.discardExact(n.toInt())

@Suppress("NOTHING_TO_INLINE")
inline fun Input.discardExact(n: Byte) = this.discardExact(n.toInt())

fun ByteReadPacket.transferTo(outputStream: OutputStream) {
    ByteArrayPool.useInstance {
        while (this.isNotEmpty) {
            outputStream.write(it, 0, this.readAvailable(it))
        }
    }
}

@MiraiInternalAPI
inline fun <R> ByteReadPacket.useBytes(
    n: Int = remaining.toInt(),//not that safe but adequate
    block: (data: ByteArray, length: Int) -> R
): R = ByteArrayPool.useInstance {
    this.readFully(it, 0, n)
    block(it, n)
}

inline fun ByteReadPacket.readPacket(
    n: Int = remaining.toInt()//not that safe but adequate
): ByteReadPacket = this.readBytes(n).toReadPacket()

inline fun Input.readUByteLVString(): String = String(this.readUByteLVByteArray())

inline fun Input.readUShortLVString(): String = String(this.readUShortLVByteArray())

inline fun Input.readUByteLVByteArray(): ByteArray = this.readBytes(this.readUByte().toInt())

inline fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readUShort().toInt())

private inline fun <R> inline(block: () -> R): R = block()


typealias TlvMap = MutableMap<Int, ByteArray>

inline fun TlvMap.getOrFail(tag: Int): ByteArray {
    return this[tag] ?: error("cannot find tlv 0x${tag.toUHexString("")}($tag)")
}

inline fun TlvMap.getOrFail(tag: Int, lazyMessage: (tag: Int) -> String): ByteArray {
    return this[tag] ?: error(lazyMessage(tag))
}

@MiraiDebugAPI
inline fun Input.readTLVMap(tagSize: Int = 2, suppressDuplication: Boolean = true): TlvMap = readTLVMap(true, tagSize, suppressDuplication)

@MiraiDebugAPI
@Suppress("DuplicatedCode")
fun Input.readTLVMap(expectingEOF: Boolean = true, tagSize: Int, suppressDuplication: Boolean = true): TlvMap {
    val map = mutableMapOf<Int, ByteArray>()
    var key = 0

    while (inline {
            try {
                key = when (tagSize) {
                    1 -> readUByte().toInt()
                    2 -> readUShort().toInt()
                    4 -> readUInt().toInt()
                    else -> error("Unsupported tag size: $tagSize")
                }
            } catch (e: Exception) { // java.nio.BufferUnderflowException is not a EOFException...
                if (expectingEOF) {
                    return map
                }
                throw e
            }
            key
        }.toUByte() != UByte.MAX_VALUE) {

        if (map.containsKey(key)) {
            if (!suppressDuplication) {
                DebugLogger.error(
                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    """
                Error readTLVMap: 
                duplicated key ${when (tagSize) {
                        1 -> key.toByte()
                        2 -> key.toShort()
                        4 -> key
                        else -> error("unreachable")
                    }.contentToString()}
                map=${map.contentToString()}
                duplicating value=${this.readUShortLVByteArray().toUHexString()}
                """.trimIndent()
                )
            }
        } else {
            try {
                map[key] = this.readUShortLVByteArray()
            } catch (e: Exception) { // BufferUnderflowException, java.io.EOFException
                // if (expectingEOF) {
                //     return map
                // }
                throw e
            }
        }
    }
    return map
}

inline fun Input.readString(length: Int, charset: Charset = Charsets.UTF_8): String = String(this.readBytes(length), charset = charset)
inline fun Input.readString(length: Long, charset: Charset = Charsets.UTF_8): String = String(this.readBytes(length.toInt()), charset = charset)
inline fun Input.readString(length: Short, charset: Charset = Charsets.UTF_8): String = String(this.readBytes(length.toInt()), charset = charset)
@JvmSynthetic
inline fun Input.readString(length: UShort, charset: Charset = Charsets.UTF_8): String = String(this.readBytes(length.toInt()), charset = charset)

inline fun Input.readString(length: Byte, charset: Charset = Charsets.UTF_8): String = String(this.readBytes(length.toInt()), charset = charset)