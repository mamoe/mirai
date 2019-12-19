package net.mamoe.mirai.utils

/**
 * 表示这里是不可到达的位置.
 */
@Suppress("NOTHING_TO_INLINE")
internal inline fun assertUnreachable(): Nothing = error("This clause should not be reached")
