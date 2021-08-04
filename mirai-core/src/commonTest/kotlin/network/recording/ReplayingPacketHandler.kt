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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.internal.network.components.PacketHandler
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.time.Duration
import kotlin.time.TimeSource

internal class ReplayingPacketHandler(
    val bundle: PacketRecordBundle
) : PacketHandler {
    private val records = bundle.records.toCollection(ConcurrentLinkedQueue())
    private val startTime = TimeSource.Monotonic.markNow()
    private val logger = MiraiLogger.create("ReplayingPacketHandler")

    val channel: EmbeddedChannel = object : EmbeddedChannel() {
        val incomingPacketThread = thread {
            // runBlocking is interruptable
            runBlocking {
                while (isActive) {
                    val record = records.peek() ?: continue
                    if (record.isIncoming && startTime.elapsedNow() >= record.timeFromStart) {
                        // record 要走网络 IO, 速度一定远慢于重放
                        // 在首条 incoming record 时间到达后就重放可以在大部分情况下 work well
                        if (records.removeFirst { it === record } != null) writeInbound(record.data)
                    }

                    delay(Duration.seconds(1)) // delay is cancellable
                }
            }
        }

        override fun handleInboundMessage(msg: Any?) {
            ReferenceCountUtil.release(msg) // Not handled, Drop
        }

        override fun handleOutboundMessage(msg: Any?) {
            ReferenceCountUtil.release(msg)
        }

        override fun doClose() {
            super.doClose()
            incomingPacketThread.interrupt()
        }
    }


    override suspend fun handlePacket(outgoingPacket: OutgoingPacket) {
        if (outgoingPacket.commandName == "friendlist.GetTroopMemberListReq") {
            channel.writeInbound(IncomingPacket)
            return
        }
        val response = records.removeFirst { it.data.identityEquals(outgoingPacket) }
            ?: records.removeFirst { it.data.commandName == outgoingPacket.commandName }
            ?: return
        logger.info { "Replaying ${response.data.commandName} ${response.data.sequenceId}" }
        channel.writeInbound(response.data)
    }
}

private inline fun <E : Any> MutableCollection<E>.removeFirst(predicate: (E) -> Boolean): E? {
    val itr = this.iterator()
    for (e in itr) {
        if (predicate(e)) {
            itr.remove()
            return e
        }
    }
    return null
}
