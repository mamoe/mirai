/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * 时间戳
 */
public expect val currentTimeMillis: Long

@get:JvmSynthetic
public inline val currentTimeSeconds: Long
    get() = currentTimeMillis / 1000


// 临时使用, 待 Kotlin Duration 稳定后使用 Duration.
// 内联属性, 则将来删除这些 API 将不会导致二进制不兼容.

@get:JvmSynthetic
public inline val Int.secondsToMillis: Long
    get() = this * 1000L

@get:JvmSynthetic
public inline val Int.minutesToMillis: Long
    get() = this * 60.secondsToMillis

@get:JvmSynthetic
public inline val Int.hoursToMillis: Long
    get() = this * 60.minutesToMillis

@get:JvmSynthetic
public inline val Int.daysToMillis: Long
    get() = this * 24.hoursToMillis

@get:JvmSynthetic
public inline val Int.weeksToMillis: Long
    get() = this * 7.daysToMillis

@get:JvmSynthetic
public inline val Int.monthsToMillis: Long
    get() = this * 30.daysToMillis


@get:JvmSynthetic
public inline val Int.millisToSeconds: Long
    get() = (this / 1000).toLong()

@get:JvmSynthetic
public inline val Int.minutesToSeconds: Long
    get() = (this * 60).toLong()

@get:JvmSynthetic
public inline val Int.hoursToSeconds: Long
    get() = this * 60.minutesToSeconds

@get:JvmSynthetic
public inline val Int.daysToSeconds: Long
    get() = this * 24.hoursToSeconds

@get:JvmSynthetic
public inline val Int.weeksToSeconds: Long
    get() = this * 7.daysToSeconds

@get:JvmSynthetic
public inline val Int.monthsToSeconds: Long
    get() = this * 30.daysToSeconds

@MiraiExperimentalAPI
@ExperimentalTime
public val Duration.asHumanReadable: String
    get() {
        val days = toInt(DurationUnit.DAYS)
        val hours = toInt(DurationUnit.HOURS) % 24
        val minutes = toInt(DurationUnit.MINUTES) % 60
        val s = floor(toDouble(DurationUnit.SECONDS) % 60 * 1000) / 1000
        return buildString {
            if (days != 0) append("${days}d ")
            if (hours != 0) append("${hours}h ")
            if (minutes != 0) append("${minutes}min ")
            append("${s}s")
        }
    }
