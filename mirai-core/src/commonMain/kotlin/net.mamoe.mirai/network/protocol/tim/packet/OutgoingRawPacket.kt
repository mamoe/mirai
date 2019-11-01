@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.writeFully
import net.mamoe.mirai.utils.io.encryptAndWrite
import net.mamoe.mirai.utils.io.writeQQ

object OutgoingRawPacket : OutgoingPacketBuilder {
    operator fun invoke(
        id: PacketId,
        bot: UInt,
        version: ByteArray,
        sessionKey: ByteArray,
        data: ByteArray
    ): OutgoingPacket = buildOutgoingPacket(id = id) {
        writeQQ(bot)
        writeFully(version)

        encryptAndWrite(sessionKey) {
            writeFully(data)
        }
    }
}