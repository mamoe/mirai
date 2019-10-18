package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.packet.EventPacketIdentity
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket


sealed class ServerFriendTypingPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    val qq get() = eventIdentity.from

}

/**
 * 对方正在输入
 */
class ServerFriendTypingStartedPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerFriendTypingPacket(input, eventIdentity)

/**
 * 对方取消了输入
 */
class ServerFriendTypingCanceledPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerFriendTypingPacket(input, eventIdentity)