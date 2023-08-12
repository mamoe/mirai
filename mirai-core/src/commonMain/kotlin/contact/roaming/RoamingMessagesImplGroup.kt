/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.contact.CommonGroupImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetGroupMsg
import net.mamoe.mirai.message.data.MessageChain

internal class RoamingMessagesImplGroup(
    override val contact: CommonGroupImpl
) : SequenceBasedRoamingMessagesImpl() {
    override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> = getMessagesImpl(
        preFilter = { maxTime -> maxTime >= timeStart || maxTime == 0 },
        preSortFilter = { msg -> msg.msgHead.msgTime in timeStart..timeEnd },
        filter = filter
    )

    override suspend fun getLastMsgSeq(): Int? {
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

    override suspend fun getMsg(seq: Int): List<MsgComm.Msg> {
        val resp = contact.bot.network.sendAndExpect(
            MessageSvcPbGetGroupMsg(
                client = contact.bot.client,
                groupUin = contact.uin,
                messageSequence = seq.toLong(),
                count = 20 // maximum 20
            )
        )

        if (resp is MessageSvcPbGetGroupMsg.Failed) return listOf()
        resp as MessageSvcPbGetGroupMsg.Success // stupid smart cast
        return resp.msgElem
    }
}