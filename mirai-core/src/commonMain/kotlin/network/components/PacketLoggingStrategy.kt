/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.contact.logMessageReceived
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.ParseErrorPacket
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.verbose

/**
 * Implementation must be fast and non-blocking, throwing no exception.
 */
internal interface PacketLoggingStrategy {
    fun logSent(logger: MiraiLogger, outgoingPacket: OutgoingPacket)
    fun logReceived(logger: MiraiLogger, incomingPacket: IncomingPacket)

    companion object : ComponentKey<PacketLoggingStrategy>
}

internal class PacketLoggingStrategyImpl(
    private val bot: AbstractBot,
    private val blacklist: Set<String> = DEFAULT_BLACKLIST,
) : PacketLoggingStrategy {
    override fun logSent(logger: MiraiLogger, outgoingPacket: OutgoingPacket) {
        if (outgoingPacket.commandName in blacklist) return
        logger.verbose { "Send: ${outgoingPacket.commandName}" }
    }

    override fun logReceived(logger: MiraiLogger, incomingPacket: IncomingPacket) {
        val packet = incomingPacket.data ?: return
        if (!bot.logger.isEnabled && !logger.isEnabled) return
        when {
            packet is ParseErrorPacket -> {
                packet.direction.getLogger(bot).error(packet.error)
            }
            packet is MessageEvent -> packet.logMessageReceived()
            packet is Packet.NoLog -> {
                // nothing to do
            }
            packet is Event && packet !is Packet.NoEventLog -> bot.logger.verbose {
                "Event: $packet".replaceMagicCodes()
            }
            else -> {
                if (SHOW_PACKET_DETAILS) {
                    logger.verbose { "Recv: ${incomingPacket.commandName} ${incomingPacket.data}".replaceMagicCodes() }
                } else {
                    logger.verbose { "Recv: ${incomingPacket.commandName}".replaceMagicCodes() }
                }
            }
        }
    }

    companion object {
        @JvmField
        val DEFAULT_BLACKLIST = setOf(
            "MessageSvc.PbDeleteMsg",
            "MessageSvc.PbGetMsg", // they are too verbose.
            "OnlinePush.RespPush"
        )

        @JvmField
        var SHOW_PACKET_DETAILS = systemProp("mirai.debug.network.show.packet.details", false)
    }
}
