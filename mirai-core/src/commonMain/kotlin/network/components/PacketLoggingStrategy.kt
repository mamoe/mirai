/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.contact.logMessageReceived
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.ParseErrorPacket
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.systemProp
import net.mamoe.mirai.utils.verbose
import kotlin.coroutines.cancellation.CancellationException

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
    private val blacklist: Set<String> = getDefaultBlacklist(),
) : PacketLoggingStrategy {
    override fun logSent(logger: MiraiLogger, outgoingPacket: OutgoingPacket) {
        if (outgoingPacket.commandName in blacklist) return
        logger.verbose { "Send: ${outgoingPacket.commandName}" }
    }

    override fun logReceived(logger: MiraiLogger, incomingPacket: IncomingPacket) {
        incomingPacket.exception?.let {
            if (it is CancellationException) return
            logger.error("Exception in decoding packet.", it)
            return
        }
        val packet = incomingPacket.data ?: return
        if (!bot.logger.isEnabled && !logger.isEnabled) return
        if (packet is ParseErrorPacket) {
            packet.direction.getLogger(bot).error("Exception in parsing packet.", packet.error)
        }
        if (incomingPacket.data is MultiPacket<*>) {
            for (d in incomingPacket.data) {
                logReceivedImpl(d, incomingPacket, logger)
            }
        }
        logReceivedImpl(packet, incomingPacket, logger)
    }

    private fun logReceivedImpl(packet: Packet, incomingPacket: IncomingPacket, logger: MiraiLogger) {
        when (packet) {
            is MessageEvent -> packet.logMessageReceived()
            is Packet.NoLog -> {
                // nothing to do
            }
            // packet is Event && packet !is Packet.NoEventLog -> bot.logger.verbose {
            //     "Event: $packet".replaceMagicCodes()
            // } // processed in global `Event.broadcast`
            else -> {
                if (incomingPacket.commandName in blacklist) return
                if (SHOW_PACKET_DETAILS) {
                    logger.verbose { "Recv: ${incomingPacket.commandName} ${incomingPacket.data}".replaceMagicCodes() }
                } else {
                    logger.verbose { "Recv: ${incomingPacket.commandName}".replaceMagicCodes() }
                }
            }
        }
    }

    companion object {
        fun getDefaultBlacklist(): Set<String> {
            if (systemProp("mirai.debug.network.show.verbose.packets", false)) return emptySet()
            return DEFAULT_BLACKLIST
        }

        private val DEFAULT_BLACKLIST: Set<String> by lazy {
            setOf(
                // C2C event sync, too verbose to show.
                "MessageSvc.PushNotify",
                "MessageSvc.PbGetMsg",
                "MessageSvc.PbDeleteMsg",

                // Group event sync, decoded as specific events, to optimize logs.
                "OnlinePush.ReqPush",
                "OnlinePush.RespPush",

                // Periodic heartbeat, showing them does not help anything.
                "Heartbeat.Alive",
                "StatSvc.SimpleGet",
            )
        }

        @JvmField
        var SHOW_PACKET_DETAILS = systemProp("mirai.debug.network.show.packet.details", false)
    }
}
