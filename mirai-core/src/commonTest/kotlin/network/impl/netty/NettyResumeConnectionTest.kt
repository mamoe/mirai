/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class NettyResumeConnectionTest : AbstractNettyNHTest() {

    private val packet = OutgoingPacket("", "", 1, ByteReadPacket.Empty)

    @Test
    fun `cannot resume on CLOSED`() = runBlockingUnit {
        network.setStateClosed()
        assertFailsWith<IllegalStateException> {
            network.sendWithoutExpect(packet)
        }
    }

    @Test
    fun `resumeConnection switches a state that can send packet on INITIALIZED`() = runBlockingUnit {
        network.resumeConnection()
        network.sendWithoutExpect(packet)
    }

    @Test
    fun `resumeConnection switches a state that can send packet on CONNECTING`() = runBlockingUnit {
        network.setStateConnecting()
        network.resumeConnection()
        network.sendWithoutExpect(packet)
    }

    @Test
    fun `resumeConnection switches a state that can send packet on LOADING`() = runBlockingUnit {
        network.setStateLoading(channel)
        network.resumeConnection()
        network.sendWithoutExpect(packet)
    }
}