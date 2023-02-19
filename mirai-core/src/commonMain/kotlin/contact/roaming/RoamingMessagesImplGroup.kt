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
import net.mamoe.mirai.internal.message.getMessageSourceKindFromC2cCmdOrNull
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetGroupMsg
import net.mamoe.mirai.message.data.MessageChain

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

                if (maxTime < timeStart) break // we have fetched all messages

                emitAll(
                    resp.msgElem.asSequence()
                        .filter { getMessageSourceKindFromC2cCmdOrNull(it.msgHead.c2cCmd) != null } // ignore unsupported messages
                        .filter { it.time in timeStart..timeEnd }
                        .sortedByDescending { it.time } // Ensure caller receiver newer messages first
                        .filter { filter.apply(it) } // Call filter after sort
                        .asFlow()
                        .map { it.toMessageChainOnline(bot) }
                )

                currentSeq = resp.msgElem.minBy { it.time }.msgHead.msgSeq
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