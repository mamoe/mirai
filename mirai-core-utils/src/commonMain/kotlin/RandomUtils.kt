/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")


package net.mamoe.mirai.utils


import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt


/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
public fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0..255).toByte() }

/**
 * 随机生成一个正整数
 */
public fun getRandomUnsignedInt(): Int = Random.nextInt().absoluteValue

/**
 * 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int): String =
    getRandomString(length, *defaultRanges)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')
private val intCharRanges: Array<CharRange> = arrayOf('0'..'9')

/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int, charRange: CharRange): String =
    CharArray(length) { charRange.random() }.concatToString()

/**
 * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int, vararg charRanges: CharRange): String =
    CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() }.concatToString()


public fun getRandomIntString(length: Int): String =
    getRandomString(length, *intCharRanges)