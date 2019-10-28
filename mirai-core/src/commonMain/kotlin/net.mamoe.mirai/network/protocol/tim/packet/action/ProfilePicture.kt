@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.BytePacketBuilder
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId

// 用户资料的头像
/**
 * 请求获取头像
 */
@PacketId(0x00_31u)
class RequestProfilePicturePacket : OutgoingPacket() {
    override fun encode(builder: BytePacketBuilder) {
        TODO("not implemented")
    }
}