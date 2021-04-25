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
import kotlinx.coroutines.delay
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.handler.NetworkHandlerSupport
import net.mamoe.mirai.internal.test.runBlockingUnit
import java.net.SocketAddress
import kotlin.reflect.jvm.javaGetter
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * When offline, handler will try endlessly to re-establish a connection. Exceptions are preserved as suppressed exceptions however, duplicates must be dropped to save memory.
 */
internal class NettyEndlessReconnectionTest : AbstractNettyNHTest() {

    override val factory: NetworkHandlerFactory<TestNettyNH> = object : NetworkHandlerFactory<TestNettyNH> {
        override fun create(context: NetworkHandlerContext, address: SocketAddress): TestNettyNH {
            return object : TestNettyNH(context, address) {
                override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                    error("fail")
            }
        }
    }

    @Test
    fun `massive reconnection`() = runBlockingUnit {
        val r = NettyNetworkHandler.Companion.RECONNECT_DELAY
        NettyNetworkHandler.Companion.RECONNECT_DELAY = 0
        network.setStateConnecting() // will connect endlessly and create a massive amount of exceptions
        delay(10000) // if exceptions are ignored by ExceptionCollector, memory usage will not exceed limitation.

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        val state = network::_state.javaGetter!!.apply { isAccessible = true }
            .invoke(network) as NetworkHandlerSupport.BaseStateImpl

        assertTrue { state.getCause()!!.suppressed.size <= 1 } // might be zero if just created since at this time network is still running.
        // size <= 1 means duplicates are dropped.


        network.close(null)
        NettyNetworkHandler.Companion.RECONNECT_DELAY = r
    }
}