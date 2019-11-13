package net.mamoe.mirai.utils.internal

/**
 * 要求 [this] 最小为 [min].
 */
@PublishedApi
internal fun Int.coerceAtLeastOrFail(min: Int): Int {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最小为 [min].
 */
@PublishedApi
internal fun Long.coerceAtLeastOrFail(min: Long): Long {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最大为 [max].
 */
@PublishedApi
internal fun Int.coerceAtMostOrFail(max: Int): Int =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this

@PublishedApi
internal fun Long.coerceAtMostOrFail(max: Long): Long =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this

/**
 * 表示这个参数必须为正数. 仅用于警示
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
internal annotation class PositiveNumbers