@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.writeFully
import kotlin.jvm.Synchronized
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * 255 -> 00 00 00 FF
 */
fun Int.toByteArray(): ByteArray = byteArrayOf(
        (ushr(24) and 0xFF).toByte(),
        (ushr(16) and 0xFF).toByte(),
        (ushr(8) and 0xFF).toByte(),
        (ushr(0) and 0xFF).toByte()
)

/**
 * 255 -> 00 FF
 */
fun UShort.toByteArray(): ByteArray = with(toUInt()) {
    byteArrayOf(
            (shr(8) and 255u).toByte(),
            (shr(0) and 255u).toByte()
    )
}

/**
 * 255u -> 00 00 00 FF
 */
fun UInt.toByteArray(): ByteArray = byteArrayOf(
        (shr(24) and 255u).toByte(),
        (shr(16) and 255u).toByte(),
        (shr(8) and 255u).toByte(),
        (shr(0) and 255u).toByte()
)

fun Int.toUHexString(separator: String = " "): String = this.toByteArray().toUHexString(separator)
fun Byte.toUHexString(): String = this.toUByte().toString(16).toUpperCase().let {
    if (it.length == 1) "0$it"
    else it
}

fun String.hexToBytes(): ByteArray = HexCache.hexToBytes(this)

/**
 * 将 Hex 转为 UByteArray, 有根据 hex 的 [hashCode] 建立的缓存.
 */
fun String.hexToUBytes(): UByteArray = HexCache.hexToUBytes(this)

fun String.hexToInt(): Int = hexToBytes().toUInt().toInt()
fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0..255).toByte() }
fun getRandomString(length: Int, charRange: CharRange): String = String(CharArray(length) { charRange.random() })
fun getRandomString(length: Int, vararg charRanges: CharRange): String = String(CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() })
fun ByteArray.toUInt(): UInt = this[0].toUInt().and(255u).shl(24) + this[1].toUInt().and(255u).shl(16) + this[2].toUInt().and(255u).shl(8) + this[3].toUInt().and(255u).shl(0)

fun ByteArray.toIoBuffer(): IoBuffer = IoBuffer.Pool.borrow().let { it.writeFully(this); it }

/**
 * Hex 转换 [ByteArray] 和 [UByteArray] 缓存.
 * 为 [net.mamoe.mirai.network.protocol.tim.TIMProtocol] 的 hex 常量使用
 */
internal object HexCache {
    private val hexToByteArrayCacheMap: MutableMap<Int, ByteArray> = mutableMapOf()

    @Synchronized
    internal fun hexToBytes(hex: String): ByteArray = hex.hashCode().let { id ->
        if (hexToByteArrayCacheMap.containsKey(id)) {
            return hexToByteArrayCacheMap[id]!!.copyOf()
        } else {
            hexToUBytes(hex).toByteArray().let {
                hexToByteArrayCacheMap[id] = it.copyOf()
                return it
            }
        }
    }

    internal fun hexToUBytes(hex: String): UByteArray =
            hex.split(" ")
                    .filterNot { it.isEmpty() }
                    .map { s -> s.toUByte(16) }
                    .toUByteArray()
}