package net.mamoe.mirai.network.protocol.tim.packet

import kotlin.reflect.KClass

/**
 * 标记一个 [OutgoingPacket] 的服务器回复包.
 * 在这个包发送时将会记录回复包信息.
 * 收到回复包时将解密为指定的包
 *
 *
 * // TODO: 2019/10/27 暂未实现. 计划中
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Response(
    val responseClass: KClass<out ResponsePacket>
)