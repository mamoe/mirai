@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeFully
import net.mamoe.mirai.utils.encryptAndWrite
import net.mamoe.mirai.utils.writeQQ

class ClientRawPacket(
        override val id: UShort,
        private val bot: UInt,
        private val version: ByteArray,
        private val sessionKey: ByteArray,
        private val data: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(bot)
        writeFully(version)

        encryptAndWrite(sessionKey) {
            writeFully(data)
        }
    }
}