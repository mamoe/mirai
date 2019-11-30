package net.mamoe.mirai.utils.internal

/**
 * 要求 [this] 最小为 [min].
 */
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun Int.coerceAtLeastOrFail(min: Int): Int {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最小为 [min].
 */
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun Long.coerceAtLeastOrFail(min: Long): Long {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最大为 [max].
 */
@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun Int.coerceAtMostOrFail(max: Int): Int =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this

@Suppress("NOTHING_TO_INLINE")
@PublishedApi
internal inline fun Long.coerceAtMostOrFail(max: Long): Long =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this

/**
 * 表示这个参数必须为正数. 仅用于警示
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
internal annotation class PositiveNumbers