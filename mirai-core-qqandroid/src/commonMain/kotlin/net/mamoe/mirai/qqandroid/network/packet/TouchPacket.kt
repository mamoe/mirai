package net.mamoe.mirai.qqandroid.network.packet

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.packet.DecrypterByteArray
import net.mamoe.mirai.network.packet.DecrypterType
import net.mamoe.mirai.network.packet.PacketFactory
import net.mamoe.mirai.network.packet.PacketId


object TouchKey : DecrypterByteArray, DecrypterType<TouchKey> {
    override val value: ByteArray
        get() = TODO("not implemented")
}

object TouchPacket : PacketFactory<TouchPacketResponse, TouchKey>(TouchKey) {
    @UseExperimental(ExperimentalUnsignedTypes::class)
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): TouchPacketResponse {
        TODO("not implemented")
    }
}


interface TouchPacketResponse : Packet