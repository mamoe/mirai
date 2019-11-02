@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass


/**
 * 包 ID. 除特殊外, [OutgoingPacketBuilder] 都需要这个注解来指定包 ID.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AnnotatedId(
    val id: KnownPacketId
)

inline val AnnotatedId.value: UShort get() = id.value


/**
 * 标记这个包对应的事件.
 * 这个注解应该被标记在 [ServerPacket] 上
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class CorrespondingEvent(
    val eventClass: KClass<out Event>
)

/**
 * 版本信息
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
internal annotation class PacketVersion(val date: String, val timVersion: String)

/**
 * 带有这个注解的 [Packet], 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class NoLog