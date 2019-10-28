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

/**
 * 表示这个参数必须为正数
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class PositiveNumbers