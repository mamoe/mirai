@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.io.toUHexString

inline class IgnoredEventPacket(val id: UShort) : EventPacket {
    override fun toString(): String = "IgnoredEventPacket(id=0x${id.toUHexString("")})"
}

object IgnoredEventIds : List<IgnoredEventParserAndHandler> by {
    listOf(
        0x0021u,
        0x0210u // 新朋友等字符串通知
    ).map { IgnoredEventParserAndHandler(it.toUShort()) }
}()

inline class IgnoredEventParserAndHandler(override val id: UShort) : EventParserAndHandler<IgnoredEventPacket> {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): IgnoredEventPacket = IgnoredEventPacket(id)
}
