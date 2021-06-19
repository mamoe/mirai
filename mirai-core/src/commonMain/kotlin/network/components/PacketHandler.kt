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
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.cast
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.coroutines.cancellation.CancellationException

internal interface PacketHandler {
    suspend fun handlePacket(incomingPacket: IncomingPacket)

    companion object : ComponentKey<PacketHandler>
}

internal class PacketHandlerChain(
    private val components: ComponentStorage = ComponentStorage.EMPTY,
    private val instances: Collection<PacketHandler>
) : PacketHandler {

    constructor(
        components: ComponentStorage = ComponentStorage.EMPTY,
        vararg instances: PacketHandler?,
    ) : this(components, instances.filterNotNull())

    constructor(
        components: ComponentStorage = ComponentStorage.EMPTY,
        instances: Iterable<PacketHandler?>,
    ) : this(components, instances.filterNotNull())

    private val interceptor: PacketInterceptor by lazy {
        components.getOrNull(PacketInterceptor) ?: PacketInterceptor.NO
    }

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        val p = interceptor.interceptor(incomingPacket) ?: return
        for (instance in instances) {
            try {
                instance.handlePacket(p)
            } catch (e: Throwable) {
                if (e is CancellationException) return
                throw ExceptionInPacketHandlerException(instance, incomingPacket, e)
            }
        }
    }
}

internal data class ExceptionInPacketHandlerException(
    val packetHandler: PacketHandler,
    val incomingPacket: IncomingPacket,
    override val cause: Throwable,
) : IllegalStateException("Exception in PacketHandler '$packetHandler' for command '${incomingPacket.commandName}'.")

internal class LoggingPacketHandlerAdapter(
    private val strategy: PacketLoggingStrategy,
    private val logger: MiraiLogger,
) : PacketHandler {
    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        strategy.logReceived(logger, incomingPacket)
    }

    override fun toString(): String = "LoggingPacketHandlerAdapter"
}

internal class EventBroadcasterPacketHandler(
    private val components: ComponentStorage,
) : PacketHandler {

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        val data = incomingPacket.data ?: return
        impl(data)
    }

    private fun impl(packet: Packet) {
        if (packet is MultiPacket<*>) {
            for (p in packet) {
                impl(p)
            }
        }
        when {
            packet is CancellableEvent && packet.isCancelled -> return
            packet is BroadcastControllable && !packet.shouldBroadcast -> return
            packet is Event -> {
                components[EventDispatcher].broadcastAsync(packet)
            }
        }
    }

    override fun toString(): String = "EventBroadcasterPacketHandler"
}

internal class CallPacketFactoryPacketHandler(
    private val bot: QQAndroidBot,
) : PacketHandler {

    override suspend fun handlePacket(incomingPacket: IncomingPacket) {
        if (incomingPacket.exception != null) return // failure
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


internal interface PacketInterceptor {
    /**
     * Break packet handling chain if return `null`
     */
    suspend fun interceptor(incomingPacket: IncomingPacket): IncomingPacket?

    fun registerTemporaryInterceptor(
        block: suspend PacketTemporaryInterceptor.(
            PacketTemporaryInterceptor.Context,
            IncomingPacket,
        ) -> Unit
    ): PacketTemporaryInterceptor

    companion object : ComponentKey<PacketInterceptor> {
        val NO: PacketInterceptor = object : PacketInterceptor {
            override suspend fun interceptor(incomingPacket: IncomingPacket): IncomingPacket? {
                return incomingPacket
            }

            override fun registerTemporaryInterceptor(block: suspend PacketTemporaryInterceptor.(PacketTemporaryInterceptor.Context, IncomingPacket) -> Unit): PacketTemporaryInterceptor {
                throw UnsupportedOperationException()
            }
        }
    }
}

internal interface PacketTemporaryInterceptor {
    interface Context {
        suspend fun finished()
    }

    fun unregister()
}

internal class PacketInterceptorImpl : PacketInterceptor {
    private val interceptors = ConcurrentLinkedDeque<PacketTemporaryInterceptorImpl>()

    private inner class PacketTemporaryInterceptorImpl(
        @JvmField val func: suspend PacketTemporaryInterceptor.(PacketTemporaryInterceptor.Context, IncomingPacket) -> Unit,
    ) : PacketTemporaryInterceptor {
        override fun unregister() {
            interceptors.remove(this)
        }
    }

    override suspend fun interceptor(incomingPacket: IncomingPacket): IncomingPacket? {
        val context = object : PacketTemporaryInterceptor.Context {
            var f = false
            override suspend fun finished() {
                f = true
            }
        }
        interceptors.forEach { interceptor ->
            if (context.f) return null
            interceptor.func.invoke(interceptor, context, incomingPacket)
        }
        if (context.f) return null
        return incomingPacket
    }

    override fun registerTemporaryInterceptor(block: suspend PacketTemporaryInterceptor.(PacketTemporaryInterceptor.Context, IncomingPacket) -> Unit): PacketTemporaryInterceptor {
        val i = PacketTemporaryInterceptorImpl(block)
        this.interceptors.add(i)
        return i
    }
}
