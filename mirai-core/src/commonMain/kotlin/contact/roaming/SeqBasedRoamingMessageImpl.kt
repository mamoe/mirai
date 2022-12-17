/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.message.data.MessageChain

private typealias Seq = Long

internal sealed class SeqBasedRoamingMessageImpl : AbstractRoamingMessages() {
    final override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        val (seqStart, seqEnd) = getSeqForTime(timeStart, timeEnd)
        return getMessageImpl(seqStart, seqEnd, filter)
    }

    protected abstract suspend fun getSeqForTime(timeStart: Long, timeEnd: Long): Pair<Seq, Seq>

    @Suppress("DuplicatedCode") // Generalizing this code would even complicate logic
    private suspend fun getMessageImpl(
        seqStart: Seq,
        seqEnd: Seq,
        filter: RoamingMessageFilter?,
    ): Flow<MessageChain> {
        return flow {
            var currentSeqStart = seqEnd.coerceAtMost(seqStart)
            while (currentCoroutineContext().isActive) {
                val resp = requestRoamMsg(currentSeqStart, seqEnd)
                val messages = resp.messages ?: break
                if (filter == null || filter === RoamingMessageFilter.ANY) {
                    // fast path
                    messages.forEach { emit(it.toMessageChainOnline(contact.bot)) }
                } else {
                    for (message in messages) {
                        if (filter.invoke(createRoamingMessage(message, messages))) {
                            emit(message.toMessageChainOnline(contact.bot))
                        }
                    }
                }
                currentSeqStart = resp.nextSeqStart
            }
        }

    }

    abstract suspend fun requestRoamMsg(
        seqStart: Seq,
        seqEnd: Seq,
    ): SeqBasedRoamingMessageChunk
}

internal interface SeqBasedRoamingMessageChunk {
    val messages: List<MsgComm.Msg>?
    val nextSeqStart: Seq
}
