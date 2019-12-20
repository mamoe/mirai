@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.ConnectionOccupiedEvent
import net.mamoe.mirai.utils.io.encodeToString

internal object ConnectionOccupiedPacketHandler : KnownEventParserAndHandler<ConnectionOccupiedEvent>(0x0030u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): ConnectionOccupiedEvent {
        discardExact(6)
        return ConnectionOccupiedEvent(readBytes((remaining - 8).toInt()).encodeToString())
    }
}