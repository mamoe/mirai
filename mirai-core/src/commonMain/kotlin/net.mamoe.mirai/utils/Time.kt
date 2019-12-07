package net.mamoe.mirai.utils

import kotlin.time.seconds

// 临时使用, 待 Kotlin Duration 稳定后使用 Duration.
// 内联属性, 则将来删除这些 API 将不会导致二进制不兼容.


inline val Int.secondsToMillis: Long get() = this * 1000L

inline val Int.minutesToMillis: Long get() = this * 60.secondsToMillis

inline val Int.hoursToMillis: Long get() = this * 60.minutesToMillis

inline val Int.daysToMillis: Long get() = this * 24.hoursToMillis

inline val Int.weeksToMillis: Long get() = this * 7.daysToMillis

inline val Int.monthsToMillis: Long get() = this * 30.daysToMillis



inline val Int.millisToSeconds: Long get() = (this / 1000).toLong()

inline val Int.minutesToSeconds: Long get() = (this * 60).toLong()

inline val Int.hoursToSeconds: Long get() = this * 60.minutesToSeconds

inline val Int.daysToSeconds: Long get() = this * 24.hoursToSeconds

inline val Int.weeksToSeconds: Long get() = this * 7.daysToSeconds

inline val Int.monthsToSeconds: Long get() = this * 30.daysToSeconds