/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.logMessageReceived
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.ParseErrorPacket
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.verbose

internal interface PacketHandler {
    suspend fun handlePacket(incomingPacket: IncomingPacket)

    companion object : ComponentKey<PacketHandler>
}

internal class PacketHandlerChain(
    private val instances: Collection<PacketHandler>
) : PacketHandler {
    constructor(vararg instances: PacketHandler?) : this(instances.filterNotNull())
    constructor(instances: Iterable<PacketHandler?>) : this(instances.filterNotNull())

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        for (instance in instances) {
            try {
                instance.handlePacket(incomingPacket)
            } catch (e: Throwable) {
                throw ExceptionInPacketHandlerException(instance, e)
            }
        }
    }
}

internal data class ExceptionInPacketHandlerException(
    val packetHandler: PacketHandler,
    override val cause: Throwable,
) : IllegalStateException("Exception in PacketHandler '$packetHandler'.")

internal class LoggingPacketHandler(
    private val bot: QQAndroidBot,
    private val logger: MiraiLogger,
) : PacketHandler {
    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
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
            else -> logger.verbose { "Recv: ${incomingPacket.commandName} ${incomingPacket.data}".replaceMagicCodes() }
        }
    }

    override fun toString(): String = "LoggingPacketHandler"
}

internal class EventBroadcasterPacketHandler(
    private val logger: MiraiLogger,
) : PacketHandler {

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        val packet = incomingPacket.data ?: return
        when {
            packet is CancellableEvent && packet.isCancelled -> return
            packet is BroadcastControllable && !packet.shouldBroadcast -> return
            packet is Event -> {
                try {
                    packet.broadcast()
                } catch (e: Throwable) {
                    if (logger.isEnabled) {
                        val msg = optimizeEventToString(packet)
                        logger.error(IllegalStateException("Exception while broadcasting event '$msg'", e))
                    }
                }
            }
        }
    }

    private fun optimizeEventToString(event: Event): String {
        val qualified = event::class.java.canonicalName ?: return this.toString()
        return qualified.substringAfter("net.mamoe.mirai.event.events.")
    }

    override fun toString(): String = "LoggingPacketHandler"
}

internal class CallPacketFactoryPacketHandler(
    private val bot: QQAndroidBot,
) : PacketHandler {

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        val factory = KnownPacketFactories.findPacketFactory(incomingPacket.commandName) ?: return
        factory.cast<PacketFactory<Packet?>>().run {
            when (this) {
                is IncomingPacketFactory -> {
                    val r = bot.handle(incomingPacket.data, incomingPacket.sequenceId)
                    if (r != null) {
                        bot.network.sendWithoutExpect(r)
                    }
                }
                is OutgoingPacketFactory -> bot.handle(incomingPacket.data)
            }
        }
    }

    override fun toString(): String = "CallPacketFactoryPacketHandler"
}