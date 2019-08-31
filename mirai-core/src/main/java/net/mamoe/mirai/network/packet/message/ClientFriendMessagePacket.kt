package net.mamoe.mirai.network.packet.message

import net.mamoe.mirai.network.packet.ClientPacket

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
class ClientFriendMessagePacket(
        val qq: Int,
        val sessionKey: ByteArray,
        val message: String
) : ClientPacket() {
    override fun encode() {

    }
}