@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

@NoLog
@AnnotatedId(KnownPacketId.HEARTBEAT)
object HeartbeatPacket : SessionPacketFactory<HeartbeatPacketResponse>() {
    operator fun invoke(
        bot: UInt,
        sessionKey: SessionKey
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer)
        encryptAndWrite(sessionKey) {
            writeHex("00 01 00 01")
        }
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): HeartbeatPacketResponse =
        HeartbeatPacketResponse
}

@NoLog
@AnnotatedId(KnownPacketId.HEARTBEAT)
object HeartbeatPacketResponse : Packet