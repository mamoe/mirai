/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.Streamable

internal sealed class SequenceBasedRoamingMessagesImpl : AbstractRoamingMessages() {
    internal val bot get() = contact.bot
    override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        error("not implemented in SequenceBasedRoamingMessage.")
    }

    override suspend fun getMessagesBefore(
        messageId: Int?,
        filter: RoamingMessageFilter?
    ): Streamable<MessageChain> {
        val flow = getMessagesImpl(messageId, preSortFilter = { true }, filter = filter)
        return object : Streamable<MessageChain> {
            override fun asFlow(): Flow<MessageChain> {
                return flow
            }
        }
    }

    override suspend fun getAllMessages(
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> = getMessagesBefore().asFlow()


    /**
     * get message sequences
     * @param preFilter: filter before emitting message elements, break loop if false.
     *  use it to predict if we fetched all messages. param1 is time of newest message.
     * @param preSortFilter: message element filter, param is msgElem
     * @param filter: user-defined roaming message filter
     */
    internal suspend fun getMessagesImpl(
        initialSeq: Int? = null,
        preFilter: (maxTime: Int) -> Boolean = { true },
        preSortFilter: (msg: MsgComm.Msg) -> Boolean,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        var currentSeq: Int = initialSeq ?: getLastMsgSeq() ?: return emptyFlow()
        var lastOfferedSeq = -1

        return flow {
            while (true) {
                val msgElem = getMsg(currentSeq)
                if (msgElem.isEmpty()) break

                // the message may be sorted increasing by message time,
                // if so, additional sortBy will not take cost.
                val maxTime = msgElem.asSequence().map { it.msgHead.msgTime }.max()
                if (!preFilter(maxTime)) break

                emitAll(
                    msgElem.asSequence()
                        .filter { lastOfferedSeq == -1 || it.msgHead.msgSeq < lastOfferedSeq }
                        .filter(preSortFilter)
                        .sortedByDescending { it.msgHead.msgSeq } // Ensure caller receives newer messages first
                        .filter { filter.apply(it) } // Call filter after sort
                        .asFlow()
                        .map { listOf(it).toMessageChainOnline(bot, contact.id, MessageSourceKind.GROUP) }
                )

                currentSeq = msgElem.first().msgHead.msgSeq
                lastOfferedSeq = currentSeq
            }
        }
    }

    private fun RoamingMessageFilter?.apply(it: MsgComm.Msg) =
        this?.invoke(createRoamingMessage(it, listOf())) != false

    internal abstract suspend fun getLastMsgSeq(): Int?

    internal abstract suspend fun getMsg(seq: Int): List<MsgComm.Msg>
}