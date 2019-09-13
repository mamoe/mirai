package net.mamoe.mirai.network.handler

import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.ClientMessageResponsePacket
import net.mamoe.mirai.network.packet.ServerEventPacket
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.utils.notice

/**
 * Kind of [PacketHandler] that prints all packets received in the format of hex byte array.
 *
 * @author Him188moe
 */
class DebugPacketHandler(session: LoginSession) : PacketHandler(session) {

    @ExperimentalUnsignedTypes
    override fun onPacketReceived(packet: ServerPacket) {
        if (!packet.javaClass.name.endsWith("Encrypted") && !packet.javaClass.name.endsWith("Raw")) {
            session.bot.notice("Packet received: $packet")
        }

        if (packet is ServerEventPacket) {
            session.socket.sendPacket(ClientMessageResponsePacket(session.bot.account.qqNumber, packet.packetId, session.sessionKey, packet.eventIdentity))
        }
    }
}