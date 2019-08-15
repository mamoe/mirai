package net.mamoe.mirai.network

import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.server.ServerPacket

/**
 * @author Him188moe @ Mirai Project
 */
open class Robot() {

    internal fun onPacketReceived(packet: Packet) {
        if (packet !is ServerPacket) {
            return;
        }

        packet.decode()
    }
}