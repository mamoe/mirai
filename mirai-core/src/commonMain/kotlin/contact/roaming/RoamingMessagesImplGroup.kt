/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.contact.CommonGroupImpl
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.message.SimpleRefineContext
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetGroupMsg
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind

internal class RoamingMessagesImplGroup(
    override val contact: CommonGroupImpl
) : AbstractRoamingMessages() {
    private val bot get() = contact.bot

    override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        var currentSeq: Int = getLastMsgSeq() ?: return emptyFlow()
        var lastOfferedSeq = -1

        return flow {
            while (true) {
                val resp = contact.bot.network.sendAndExpect(
                    MessageSvcPbGetGroupMsg(
                        client = contact.bot.client,
                        groupUin = contact.uin,
                        messageSequence = currentSeq.toLong(),
                        count = 20 // maximum 20
                    )
                )

                if (resp is MessageSvcPbGetGroupMsg.Failed) break
                resp as MessageSvcPbGetGroupMsg.Success // stupid smart cast
                if (resp.msgElem.isEmpty()) break

                // the message may be sorted increasing by message time,
                // if so, additional sortBy will not take cost.
                val messageTimeSequence = resp.msgElem.asSequence().map { it.time }

                val maxTime = messageTimeSequence.max()


                // we have fetched all messages
                // note: maxTime = 0 means all fetched messages were recalled
                if (maxTime < timeStart && maxTime != 0) break

                emitAll(
                    resp.msgElem.asSequence()
                        .filter { lastOfferedSeq == -1 || it.msgHead.msgSeq < lastOfferedSeq }
                        .filter { it.time in timeStart..timeEnd }
                        .sortedByDescending { it.msgHead.msgSeq } // Ensure caller receives newer messages first
                        .filter { filter.apply(it) } // Call filter after sort
                        .asFlow()
                        .map {
                            listOf(it).toMessageChainOnline(
                                bot,
                                contact.id,
                                MessageSourceKind.GROUP,
                                SimpleRefineContext(
                                    RefineContextKey.MessageSourceKind to MessageSourceKind.GROUP,
                                    RefineContextKey.FromId to it.msgHead.fromUin,
                                    RefineContextKey.GroupIdOrZero to contact.id,
                                )
                            )
                        }
                )

                currentSeq = resp.msgElem.first().msgHead.msgSeq
                lastOfferedSeq = currentSeq
            }
        }
    }

    private val MsgComm.Msg.time get() = msgHead.msgTime

    private fun RoamingMessageFilter?.apply(
        it: MsgComm.Msg
    ) = this?.invoke(createRoamingMessage(it, listOf())) != false

    private suspend fun getLastMsgSeq(): Int? {
        // Iterate from the newest message to find messages within [timeStart] and [timeEnd]
        val lastMsgSeqResp = bot.network.sendAndExpect(
            TroopManagement.GetGroupLastMsgSeq(
                client = bot.client,
                groupUin = contact.uin
            )
        )

        return when (lastMsgSeqResp) {
            TroopManagement.GetGroupLastMsgSeq.Response.Failed -> null
            is TroopManagement.GetGroupLastMsgSeq.Response.Success -> lastMsgSeqResp.seq
        }
    }
}