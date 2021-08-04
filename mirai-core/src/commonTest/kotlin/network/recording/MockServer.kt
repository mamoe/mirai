/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.recording

import io.netty.channel.embedded.EmbeddedChannel
import io.netty.util.ReferenceCountUtil
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket

internal class MockServer {
    val state: AtomicRef<State> = atomic(AuthenticationState())

    val channel: EmbeddedChannel = object : EmbeddedChannel() {
        override fun handleInboundMessage(msg: Any?) {
            ReferenceCountUtil.release(msg) // Not handled, Drop
        }

        override fun handleOutboundMessage(msg: Any?) {
            ReferenceCountUtil.release(msg)
        }
    }

    internal fun send(incomingPacket: IncomingPacket) {
        channel.writeInbound()
    }

    internal abstract inner class State(

    ) {
        fun onReceived(outgoing: OutgoingPacket) {

        }
    }

    internal inner class AuthenticationState(

    ) : State() {

    }
}