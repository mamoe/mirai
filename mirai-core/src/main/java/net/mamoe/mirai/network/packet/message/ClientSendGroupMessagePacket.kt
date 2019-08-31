package net.mamoe.mirai.network.packet.message

import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.encryptAndWrite

/**
 * @author Him188moe
 */
@PacketId("00 CD")
@ExperimentalUnsignedTypes
class ClientSendGroupMessagePacket(
        val group: Int,
        val qq: Int,
        val sessionKey: ByteArray,
        val message: String
) : ClientPacket() {
    override fun encode() {
        TODO()

        this.encryptAndWrite(sessionKey) {
            // it.write()

        }
    }
}