package net.mamoe.mirai.utils.internal

@PublishedApi
internal fun Int.coerceAtLeastOrFail(value: Int): Int {
    require(this > value)
    return this
}

@PublishedApi
internal fun Long.coerceAtLeastOrFail(value: Long): Long {
    require(this > value)
    return this
}

@PublishedApi
internal fun Int.coerceAtMostOrFail(maximumValue: Int): Int =
    if (this > maximumValue) error("value is greater than its expected maximum value $maximumValue")
    else this

@PublishedApi
internal fun Long.coerceAtMostOrFail(maximumValue: Long): Long =
    if (this > maximumValue) error("value is greater than its expected maximum value $maximumValue")
    else this

/**
 * 表示这个参数必须为正数
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class PositiveNumbers