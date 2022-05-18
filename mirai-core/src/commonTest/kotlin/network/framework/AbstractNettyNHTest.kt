/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import io.ktor.utils.io.core.*
import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.handler.SocketAddress
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.ExceptionCollector
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.error
import java.net.SocketAddress

/**
 * You may need to override [createConnection]
 */
internal abstract class TestNettyNH(
    override val bot: QQAndroidBot,
    context: NetworkHandlerContext,
    address: SocketAddress,
) : NettyNetworkHandler(context, address), ITestNetworkHandler {

    protected abstract suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel
    final override suspend fun createConnection(): Channel {
        return createConnection(createDummyDecodePipeline())
    }

    override fun setStateClosed(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateClosed(exception) }
    }

    override fun setStateConnecting(exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateConnecting(ExceptionCollector(exception)) }
    }

    override fun setStateOK(channel: Channel, exception: Throwable?): NetworkHandlerSupport.BaseStateImpl? {
        exception?.printStackTrace()
        return setState { StateOK(channel, CompletableDeferred(Unit)) }
    }

    override fun setStateLoading(channel: Channel): NetworkHandlerSupport.BaseStateImpl? {
        return setState { StateLoading(channel) }
    }

}

/**
 * Without selector. When network is closed, it will not reconnect, so that you can check for its states.
 *
 * @see AbstractNettyNHTestWithSelector
 */
internal abstract class AbstractNettyNHTest : AbstractRealNetworkHandlerTest<TestNettyNH>() {

    init {
        overrideComponents[BotOfflineEventMonitor] = object : BotOfflineEventMonitor {
            override fun attachJob(bot: AbstractBot, scope: CoroutineScope) {
            }
        }
    }

    class NettyNHTestChannel(
        val logger: Lazy<MiraiLogger>,
        var fakeServer: (NettyNHTestChannel.(msg: Any?) -> Unit)? = null,
    ) : EmbeddedChannel() {
        @OptIn(InternalSerializationApi::class)
        fun listen(listener: (OutgoingPacket) -> Any?) {
            fakeServer = { packet ->
                if (packet is OutgoingPacket) {
                    val rsp0 = when (val rsp = listener(packet)) {
                        null -> null
                        is Unit -> null
                        is ByteArray -> {
                            RawIncomingPacket(
                                commandName = packet.commandName,
                                sequenceId = packet.sequenceId,
                                body = rsp
                            )
                        }
                        is RawIncomingPacket -> rsp
                        is ProtoBuf -> {
                            RawIncomingPacket(
                                commandName = packet.commandName,
                                sequenceId = packet.sequenceId,
                                body = buildPacket {
                                    writeProtoBuf(
                                        rsp::class.serializer().cast<KSerializer<ProtoBuf>>(),
                                        rsp
                                    )
                                }.readBytes()
                            )
                        }
                        else -> {
                            logger.value.error { "Failed to respond $rsp" }
                            null
                        }
                    }
                    if (rsp0 != null) {
                        pipeline().fireChannelRead(rsp0)
                    }
                }
                ReferenceCountUtil.release(packet)
            }
        }

        public /*internal*/ override fun doRegister() {
            super.doRegister() // Set channel state to ACTIVE
            // Drop old handlers
            pipeline().let { p ->
                while (p.first() != null) {
                    p.removeFirst()
                }
            }
        }

        override fun handleInboundMessage(msg: Any?) {
            ReferenceCountUtil.release(msg) // Not handled, Drop
        }

        override fun handleOutboundMessage(msg: Any?) {
            fakeServer?.invoke(this, msg) ?: ReferenceCountUtil.release(msg)
        }
    }

    val channel = NettyNHTestChannel(
        logger = lazy { bot.logger },
    )

    override val network: TestNettyNH get() = bot.network as TestNettyNH

    override val factory: NetworkHandlerFactory<TestNettyNH> =
        NetworkHandlerFactory<TestNettyNH> { context, address ->
            object : TestNettyNH(bot, context, address) {
                override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                    channel.apply {
                        doRegister() // restart channel
                        setupChannelPipeline(pipeline(), decodePipeline)
                    }
            }
        }

    protected fun removeOutgoingPacketEncoder() {
        kotlin.runCatching {
            channel.pipeline().remove("outgoing-packet-encoder")
        }
    }
}
