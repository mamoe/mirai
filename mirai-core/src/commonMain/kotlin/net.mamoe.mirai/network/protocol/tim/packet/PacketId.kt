@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class PacketId(
        val value: UShort
)
