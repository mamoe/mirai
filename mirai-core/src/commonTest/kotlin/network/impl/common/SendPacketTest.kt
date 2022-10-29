/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.common

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SendPacketTest : AbstractCommonNHTest() {
    // single thread so we can use [yield] to transfer dispatch
    private val singleThreadDispatcher = borrowSingleThreadDispatcher()

    @Test
    fun `sendPacketImpl suspends until a valid state`() = runBlockingUnit(singleThreadDispatcher) {
        val expectStop = AtomicBoolean(false)

        // coroutine starts immediately and suspends at `net.mamoe.mirai.internal.network.impl.netty.NettyNetworkHandler.sendPacketImpl`
        launch(singleThreadDispatcher, start = CoroutineStart.UNDISPATCHED) {
            assertNotNull(network.sendAndExpect(OutgoingPacket("name", "cmd", 1, ByteReadPacket.Empty)))
            assertTrue { expectStop.value }
        }
        network.setStateOK(conn) // then we can send packet.
        yield() // yields the thread to run `sendAndExpect`

        // when we got thread here again, `sendAndExpect` is suspending for response [Packet].
        network.collectReceived(IncomingPacket("cmd", 1, object : Packet {}))
        // now `sendAndExpect` should finish (asynchronously).
        expectStop.value = true
    }

    @Test
    fun `sendPacketImpl does not suspend if state is valid`() = runBlockingUnit(singleThreadDispatcher) {
        network.setStateOK(conn) // then we can send packet.
        val expectStop = AtomicBoolean(false)

        val job = launch(singleThreadDispatcher, start = CoroutineStart.UNDISPATCHED) {
            assertNotNull(network.sendAndExpect(OutgoingPacket("name", "cmd", 1, ByteReadPacket.Empty)))
            assertTrue { expectStop.value } // ensures `sendAndExpect` does not finish immediately. (We expect one suspension.)
        }
        expectStop.value = true
        network.collectReceived(IncomingPacket("cmd", 1, object : Packet {}))
        yield()
        assertTrue(job.isCompleted)
    }
}