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
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.handler.NetworkHandlerContext
import net.mamoe.mirai.internal.network.handler.NetworkHandlerFactory
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test
import java.net.SocketAddress
import java.util.concurrent.Executors
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class NettySendPacketTest : AbstractNettyNHTest() {
    override val factory: NetworkHandlerFactory<TestNettyNH> = object : NetworkHandlerFactory<TestNettyNH> {
        override fun create(context: NetworkHandlerContext, address: SocketAddress): TestNettyNH {
            return object : TestNettyNH(bot, context, address) {
                override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                    channel.apply {
                        doRegister() // restart channel
                        setupChannelPipeline(pipeline(), decodePipeline)
                    }
            }
        }
    }

    // single thread so we can use [yield] to transfer dispatch
    private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Test
    fun `sendPacketImpl suspends until a valid state`() = runBlockingUnit(singleThreadDispatcher) {
        val expectStop = atomic(false)

        // coroutine starts immediately and suspends at `net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler.sendPacketImpl`
        launch(singleThreadDispatcher, start = CoroutineStart.UNDISPATCHED) {
            assertNotNull(network.sendAndExpect(OutgoingPacket("name", "cmd", 1, ByteReadPacket.Empty)))
            assertTrue { expectStop.value }
        }
        network.setStateOK(channel) // then we can send packet.
        yield() // yields the thread to run `sendAndExpect`

        // when we got thread here again, `sendAndExpect` is suspending for response [Packet].
        network.collectReceived(IncomingPacket("cmd", 1, object : Packet {}, null))
        // now `sendAndExpect` should finish (asynchronously).
        expectStop.value = true
    }

    @Test
    fun `sendPacketImpl does not suspend if state is valid`() = runBlockingUnit(singleThreadDispatcher) {
        network.setStateOK(channel) // then we can send packet.
        val expectStop = atomic(false)

        val job = launch(singleThreadDispatcher, start = CoroutineStart.UNDISPATCHED) {
            assertNotNull(network.sendAndExpect(OutgoingPacket("name", "cmd", 1, ByteReadPacket.Empty)))
            assertTrue { expectStop.value } // ensures `sendAndExpect` does not finish immediately. (We expect one suspension.)
        }
        expectStop.value = true
        network.collectReceived(IncomingPacket("cmd", 1, object : Packet {}, null))
        yield()
        assertTrue(job.isCompleted)
    }
}