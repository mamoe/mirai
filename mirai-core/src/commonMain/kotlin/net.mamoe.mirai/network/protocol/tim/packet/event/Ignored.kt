@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot

object IgnoredEventPacket : EventPacket

object IgnoredEventIds : List<IgnoredEventParserAndHandler> by {
    listOf(
        0x0021u
    ).map { IgnoredEventParserAndHandler(it.toUShort()) }
}()

inline class IgnoredEventParserAndHandler(override val id: UShort) : EventParserAndHandler<IgnoredEventPacket> {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): IgnoredEventPacket = IgnoredEventPacket
}
