package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.packet.EventPacketIdentity
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket


/**
 * Android 客户端上线
 */
class ServerAndroidOnlineEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity)

/**
 * Android 客户端下线
 */
class ServerAndroidOfflineEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity)