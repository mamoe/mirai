@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeHex
import net.mamoe.mirai.utils.io.writeQQ

@NoLog
@AnnotatedId(KnownPacketId.HEARTBEAT)
object HeartbeatPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        sessionKey: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer)
        encryptAndWrite(sessionKey) {
            writeHex("00 01 00 01")
        }
    }

    @NoLog
    @AnnotatedId(KnownPacketId.HEARTBEAT)
    class Response(input: ByteReadPacket) : ResponsePacket(input)
}