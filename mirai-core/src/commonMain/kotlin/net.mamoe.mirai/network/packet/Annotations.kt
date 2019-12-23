@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.network.packet

/**
 * 包的最后一次修改时间, 和分析时使用的 TIM 版本
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class PacketVersion(val date: String, val timVersion: String)

/**
 * 带有这个注解的 [Packet] 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoLog