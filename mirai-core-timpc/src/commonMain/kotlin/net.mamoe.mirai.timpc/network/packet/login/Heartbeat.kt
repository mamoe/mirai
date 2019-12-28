@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.utils.NoLog

import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.cryptor.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

@NoLog
internal object HeartbeatPacket : SessionPacketFactory<HeartbeatPacketResponse>() {
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        encryptAndWrite(sessionKey) {
            writeHex("00 01 00 01")
        }
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): HeartbeatPacketResponse =
        HeartbeatPacketResponse
}

@NoLog
internal object HeartbeatPacketResponse : Packet, Subscribable