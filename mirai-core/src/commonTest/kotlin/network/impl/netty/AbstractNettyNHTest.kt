/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import io.netty.channel.Channel
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.utils.ExceptionCollector
import java.net.SocketAddress

internal open class TestNettyNH(
    context: NetworkHandlerContext,
    address: SocketAddress
) : NettyNetworkHandler(context, address) {

    fun setStateClosed(exception: Throwable? = null) {
        setState { StateClosed(exception) }
    }

    fun setStateConnecting(exception: Throwable? = null) {
        setState { StateConnecting(ExceptionCollector(exception), false) }
    }

    fun setStateOK(channel: Channel, exception: Throwable? = null) {
        setState { StateOK(channel, CompletableDeferred(Unit)) }
    }

    fun setStateLoading(channel: Channel) {
        setState { StateLoading(channel) }
    }

}

internal abstract class AbstractNettyNHTest : AbstractRealNetworkHandlerTest<TestNettyNH>() {
    var fakeServer: (NettyNHTestChannel.(msg: Any?) -> Unit)? = null

    internal inner class NettyNHTestChannel : EmbeddedChannel() {
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
        object : NetworkHandlerFactory<TestNettyNH> {
            override fun create(context: NetworkHandlerContext, address: SocketAddress): TestNettyNH {
                return object : TestNettyNH(context, address) {
                    override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                        channel.apply {
                            doRegister() // restart channel
                            setupChannelPipeline(pipeline(), decodePipeline)
                        }
                }
            }
        }
}

internal fun AbstractNettyNHTest.setSsoProcessor(action: suspend SsoProcessor.(handler: NetworkHandler) -> Unit) {
    defaultComponents[SsoProcessor] = object : SsoProcessor by defaultComponents[SsoProcessor] {
        override suspend fun login(handler: NetworkHandler) = action(handler)
    }
}