package net.mamoe.mirai.network.packet.server.event

import net.mamoe.mirai.network.packet.client.ClientPacket
import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe
 */
class ServerMessageEventPacket(input: DataInputStream) : ServerPacket(input) {


    override fun decode() {

    }
}

@ExperimentalUnsignedTypes
class ClientGroupMessageResponsePacket : ClientMessageResponsePacket() {
}

/**
 * 告知服务器已经收到数据
 */
@ExperimentalUnsignedTypes
open class ClientMessageResponsePacket : ClientPacket() {
    override fun encode() {

    }
}