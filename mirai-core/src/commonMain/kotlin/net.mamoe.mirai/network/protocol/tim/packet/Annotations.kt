@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.event.Subscribable
import kotlin.reflect.KClass


/**
 * 包 ID. 除特殊外, [PacketFactory] 都需要这个注解来指定包 ID.
 */
@Deprecated("Reflection is not supported in JS. Consider to remove")
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AnnotatedId(
    val id: KnownPacketId
)

inline val AnnotatedId.value: UShort get() = id.value


/**
 * 标记这个包对应的事件.
 * 这个注解应该被标记在 [Packet] 上
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CorrespondingEvent(
    val eventClass: KClass<out Subscribable>
)

/**
 * 包的最后一次修改时间, 和分析时使用的 TIM 版本
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
internal annotation class PacketVersion(val date: String, val timVersion: String)

/**
 * 带有这个注解的 [Packet], 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class NoLog