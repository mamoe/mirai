@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.network.protocol.tim.packet


/**
 * 包 ID. 除特殊外, [PacketFactory] 都需要这个注解来指定包 ID.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
internal annotation class AnnotatedId( // 注解无法在 JS 平台使用, 但现在暂不需要考虑 JS
    val id: KnownPacketId
)

internal inline val AnnotatedId.value: UShort get() = id.value

/**
 * 包的最后一次修改时间, 和分析时使用的 TIM 版本
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
internal annotation class PacketVersion(val date: String, val timVersion: String)

/**
 * 带有这个注解的 [Packet] 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class NoLog