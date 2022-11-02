/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")


package net.mamoe.mirai.utils


import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.random.nextInt


/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
public fun getRandomByteArray(length: Int, random: Random = Random): ByteArray =
    ByteArray(length) { random.nextInt(0..255).toByte() }

/**
 * 随机生成一个正整数
 */
public fun getRandomUnsignedInt(): Int = Random.nextInt().absoluteValue

/**
 * 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int, random: Random = Random): String =
    getRandomString(length, *defaultRanges, random = random)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')
private val intCharRanges: Array<CharRange> = arrayOf('0'..'9')

/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int, charRange: CharRange, random: Random = Random): String =
    CharArray(length) { charRange.random(random) }.concatToString()

/**
 * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
 */
public fun getRandomString(length: Int, vararg charRanges: CharRange, random: Random = Random): String =
    CharArray(length) { charRanges[random.nextInt(0..charRanges.lastIndex)].random(random) }.concatToString()


public fun getRandomIntString(length: Int, random: Random = Random): String =
    getRandomString(length, charRanges = intCharRanges, random = random)