/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("TimeUtilsKt_common")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.floor
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

public expect fun currentTimeMillis(): Long

/**
 * 时间戳到秒
 */
public fun currentTimeSeconds(): Long = currentTimeMillis() / 1000

public expect fun currentTimeFormatted(format: String? = null): String


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

// @MiraiExperimentalApi
@Deprecated("Do not use unstable API", level = DeprecationLevel.HIDDEN)
@ExperimentalTime
@DeprecatedSinceMirai(errorSince = "2.7", hiddenSince = "2.10") // maybe 2.7
public fun Duration.toHumanReadableString(): String {
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

// since 2.7

public fun Int.millisToHumanReadableString(): String = toLongUnsigned().millisToHumanReadableString()

public fun Long.millisToHumanReadableString(): String {
    val days = this / 1000 / 3600 / 24
    val hours = this / 1000 / 3600 % 24
    val minutes = this / 1000 / 60 % 60
    val s = floor(this.toDouble() / 1000 % 60 * 1000) / 1000
    return buildString {
        if (days != 0L) append("${days}d ")
        if (hours != 0L) append("${hours}h ")
        if (minutes != 0L) append("${minutes}min ")
        append("${s}s")
    }
}
