@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.io.encodeToString

/**
 * 被挤下线. 只能获取到中文的消息
 */
inline class ConnectionOccupiedEvent(val message: String) : EventPacket {
    override fun toString(): String = "ConnectionOccupiedEvent(${message.replace("\n", "")})"
}

internal object ConnectionOccupiedPacketHandler : KnownEventParserAndHandler<ConnectionOccupiedEvent>(0x0030u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): ConnectionOccupiedEvent {
        discardExact(6)
        return ConnectionOccupiedEvent(readBytes((remaining - 8).toInt()).encodeToString())
    }
}