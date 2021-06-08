/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import io.netty.channel.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.HeartbeatFailureHandler
import net.mamoe.mirai.internal.network.components.HeartbeatScheduler
import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.impl.netty.AbstractNettyNHTest
import net.mamoe.mirai.internal.network.impl.netty.HeartbeatFailedException
import net.mamoe.mirai.internal.network.impl.netty.TestNettyNH
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.cast
import org.junit.jupiter.api.Test
import java.net.SocketAddress
import kotlin.test.assertFails

internal class SelectorNetworkHandlerTest : AbstractRealNetworkHandlerTest<SelectorNetworkHandler>() {
    val channel = AbstractNettyNHTest.NettyNHTestChannel()

    private val selector = TestSelector {
        object : TestNettyNH(bot, createContext(), address) {
            override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel {
                return channel
            }
        }
    }

    override val factory: NetworkHandlerFactory<SelectorNetworkHandler> =
        object : NetworkHandlerFactory<SelectorNetworkHandler> {
            override fun create(context: NetworkHandlerContext, address: SocketAddress): SelectorNetworkHandler {
                return SelectorNetworkHandler(context, selector)
            }
        }

    override val network: SelectorNetworkHandler get() = bot.network.cast()

    @Test
    fun `stop on manual close`() = runBlockingUnit {
        network.resumeConnection()
        network.close(IllegalStateException("Closed by test"))
        assertFails { network.resumeConnection() }
    }


    /**
     * Emulates system hibernation and network failure.
     * @see HeartbeatFailedException
     */
    @Test
    fun `can recover on heartbeat failure`() = runBlockingUnit {
        testRecover { HeartbeatFailedException("test", null) } // NetworkException
    }

    @Test
    fun `cannot recover on other failures`() = runBlockingUnit {
        testRecover { IllegalStateException() }
    }

    private suspend fun testRecover(exception: () -> Exception) {
        val heartbeatScheduler = object : HeartbeatScheduler {
            lateinit var onHeartFailure: HeartbeatFailureHandler
            override fun launchJobsIn(
                network: NetworkHandlerSupport,
                scope: CoroutineScope,
                onHeartFailure: HeartbeatFailureHandler
            ): List<Job> {
                this.onHeartFailure = onHeartFailure
                return listOf(Job())
            }
        }
        overrideComponents[HeartbeatScheduler] = heartbeatScheduler

        bot.login()
        bot.network.context[EventDispatcher].joinBroadcast()
        assertState(NetworkHandler.State.OK)

        heartbeatScheduler.onHeartFailure("Test", exception())
        assertState(NetworkHandler.State.CLOSED)

        bot.network.resumeConnection() // in real, this is called by BotOnlineWatchdog in SelectorNetworkHandler
        assertState(NetworkHandler.State.OK)
    }
}