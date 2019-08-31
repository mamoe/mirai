package net.mamoe.mirai.network.packet

import java.io.DataInputStream

/**
 * @author Him188moe
 */
class UnknownServerPacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }
}