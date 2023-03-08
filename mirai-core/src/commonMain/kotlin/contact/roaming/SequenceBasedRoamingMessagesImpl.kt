/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.flow.Flow
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.message.data.MessageChain
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
        val flow = getMessagesBeforeFlow(messageId, filter)
        return object : Streamable<MessageChain> {
            override fun asFlow(): Flow<MessageChain> {
                return flow
            }
        }
    }

    override suspend fun getAllMessages(
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> = getMessagesBefore().asFlow()

    abstract suspend fun getMessagesBeforeFlow(
        messageId: Int?,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain>

    internal suspend fun getLastMsgSeq(): Int? {
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