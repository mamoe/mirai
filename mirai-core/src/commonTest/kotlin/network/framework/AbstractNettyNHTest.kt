/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.components.BotOfflineEventMonitor
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler
import net.mamoe.mirai.utils.ExceptionCollector
import java.net.SocketAddress

/**
 * You may need to override [createConnection]
 */
internal abstract class TestNettyNH(
    override val bot: QQAndroidBot,
    context: NetworkHandlerContext,
    address: SocketAddress,
) : NettyNetworkHandler(context, address), ITestNetworkHandler {

    abstract override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel

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
        var fakeServer: (NettyNHTestChannel.(msg: Any?) -> Unit)? = null,
    ) : EmbeddedChannel() {
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

    val channel = NettyNHTestChannel()

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
}
