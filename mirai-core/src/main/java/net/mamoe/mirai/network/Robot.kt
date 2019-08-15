package net.mamoe.mirai.network

import net.mamoe.mirai.network.packet.Packet
import net.mamoe.mirai.network.packet.client.Client0825ResponsePacket
import net.mamoe.mirai.network.packet.server.Server0825Packet
import net.mamoe.mirai.network.packet.server.ServerPacket

/**
 * [number] is a QQ number.
 *
 * @author Him188moe @ Mirai Project
 */
class Robot(val number: Long) {

    internal fun onPacketReceived(packet: Packet) {
        if (packet !is ServerPacket) {
            return;
        }

        packet.decode()
        if (packet is Server0825Packet) {//todo 检查是否需要修改 UDP 连接???
            sendPacket(Client0825ResponsePacket(packet.serverIP, number));
        }
    }

    internal fun sendPacket(packet: Packet) {
        TODO()
    }
}