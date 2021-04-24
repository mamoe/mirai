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
import kotlinx.coroutines.delay
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.ExceptionCollector
import java.net.SocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.seconds


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

}

internal class NettyHandlerEventTest : AbstractRealNetworkHandlerTest<TestNettyNH>() {
    val channel = EmbeddedChannel()
    val network get() = bot.network as TestNettyNH

    override val factory: NetworkHandlerFactory<TestNettyNH> =
        object : NetworkHandlerFactory<TestNettyNH> {
            override fun create(context: NetworkHandlerContext, address: SocketAddress): TestNettyNH {
                return object : TestNettyNH(context, address) {
                    override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                        channel.apply { setupChannelPipeline(pipeline(), decodePipeline) }
                }
            }
        }

    @Test
    fun `BotOnlineEvent after successful logon`() = runBlockingUnit {
        assertEventBroadcasts<BotOnlineEvent> {
            assertEquals(State.INITIALIZED, network.state)
            bot.login() // launches a job which broadcasts the event
            delay(3.seconds)
            assertEquals(State.OK, network.state)
        }
    }

    @Test
    fun `BotReloginEvent after successful reconnection`() = runBlockingUnit {
        assertEventBroadcasts<BotReloginEvent> {
            assertEquals(State.INITIALIZED, network.state)
            bot.login()
            bot.firstLoginSucceed = true
            network.setStateConnecting()
            network.resumeConnection()
            delay(3.seconds) // `login` launches a job which broadcasts the event
            assertEquals(State.OK, network.state)
        }
    }

    @Test
    fun `BotOnlineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(State.INITIALIZED, network.state)
        bot.login()
        bot.firstLoginSucceed = true
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOnlineEvent>(1) {
            network.setStateConnecting()
            network.resumeConnection()
            delay(3.seconds)
            assertEquals(State.OK, network.state)
        }
    }

    @Test
    fun `BotOfflineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(State.INITIALIZED, network.state)
        bot.login()
        bot.firstLoginSucceed = true
        assertEquals(State.OK, network.state)
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOfflineEvent>(1) {
            network.setStateClosed()
            delay(3.seconds)
            assertEquals(State.CLOSED, network.state)
        }
    }
}