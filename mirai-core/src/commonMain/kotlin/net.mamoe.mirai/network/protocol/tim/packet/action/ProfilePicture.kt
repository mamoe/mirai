@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import net.mamoe.mirai.network.protocol.tim.packet.*

// 用户资料的头像
/**
 * 请求获取头像
 */
@AnnotatedId(KnownPacketId.REQUEST_PROFILE)
object RequestProfilePicturePacket : OutgoingPacketBuilder {
    operator fun invoke(): OutgoingPacket = buildOutgoingPacket {

    }
}