package net.mamoe.mirai.network.packet.server

import java.io.DataInputStream

/**
 * @author Him188moe
 */
class UnknownPacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }
}