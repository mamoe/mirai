package net.mamoe.mirai.network.packet.client.login

import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.ClientPacket

/**
 * Dispose_0836
 *
 * @author Him188moe @ Mirai Project
 */
@PacketId("08 36 31 04")
@ExperimentalUnsignedTypes
class ClientLoginResendPacket3104(val tgtgtKey: ByteArray, val token00BA: ByteArray) : ClientPacket() {
    override fun encode() {

    }
}