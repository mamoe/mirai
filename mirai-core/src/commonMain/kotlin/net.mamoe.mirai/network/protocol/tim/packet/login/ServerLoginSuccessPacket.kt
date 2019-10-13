package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket

/**
 * Congratulations!
 *
 * @author Him188moe
 */
@PacketId("00 EC")
class ServerLoginSuccessPacket(input: ByteReadPacket) : ServerPacket(input)