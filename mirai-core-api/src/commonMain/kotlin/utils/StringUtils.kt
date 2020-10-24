package net.mamoe.mirai.utils

import kotlin.jvm.JvmSynthetic
import kotlin.random.Random
import kotlin.random.nextInt


/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
@JvmSynthetic
internal fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0..255).toByte() }

/**
 * 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int): String =
    getRandomString(length, *defaultRanges)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')
private val intCharRanges: Array<CharRange> = arrayOf('0'..'9')
/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int, charRange: CharRange): String =
    CharArray(length) { charRange.random() }.concatToString()

/**
 * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int, vararg charRanges: CharRange): String =
    CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() }.concatToString()


@JvmSynthetic
internal fun getRandomIntString(length: Int): String =
    getRandomString(length, *intCharRanges)