package net.mamoe.mirai.utils

/**
 * 要求 [this] 最小为 [min].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Int.coerceAtLeastOrFail(min: Int): Int {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最小为 [min].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Long.coerceAtLeastOrFail(min: Long): Long {
    require(this >= min)
    return this
}

/**
 * 要求 [this] 最大为 [max].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Int.coerceAtMostOrFail(max: Int): Int =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this

@Suppress("NOTHING_TO_INLINE")
inline fun Long.coerceAtMostOrFail(max: Long): Long =
    if (this >= max) error("value is greater than its expected maximum value $max")
    else this