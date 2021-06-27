/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.getGroupByUin
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.notice.SystemMessageProcessor.Companion.fromSync
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.utils.context

/**
 * Handles [UserMessageEvent] and their sync events. For [GroupMessageEvent], see [GroupMessageProcessor]
 *
 * @see StrangerMessageEvent
 * @see StrangerMessageSyncEvent
 *
 * @see FriendMessageEvent
 * @see FriendMessageSyncEvent
 *
 * @see GroupTempMessageEvent
 * @see GroupTempMessageSyncEvent
 */
internal class PrivateMessageNoticeProcessor : SimpleNoticeProcessor<MsgComm.Msg>(type()) {
    override suspend fun PipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        if (msgHead.fromUin == bot.id && fromSync) {
            // Bot send message to himself? or from other client? I am not the implementer.
            bot.client.sendFriendMessageSeq.updateIfSmallerThan(msgHead.msgSeq)
            return
        }
        if (!bot.components[SsoProcessor].firstLoginSucceed) return
        val senderUin = if (fromSync) msgHead.toUin else msgHead.fromUin
        when (msgHead.msgType) {
            166, 167, // 单向好友
            208, // friend ptt, maybe also support stranger
            -> {
                handlePrivateMessage(data, bot.getFriend(senderUin) ?: bot.getStranger(senderUin) ?: return)
                markAsConsumed()
            }

            141, // group temp
            -> {
                val tmpHead = msgHead.c2cTmpMsgHead ?: return
                val group = bot.getGroupByUin(tmpHead.groupUin) ?: return
                handlePrivateMessage(data, group[senderUin] ?: return)
                markAsConsumed()
            }
        }

    }

    private suspend fun PipelineContext.handlePrivateMessage(
        data: MsgComm.Msg,
        user: User,
    ) = data.context {
        user.impl()
        if (!user.messageSeq.updateIfDifferentWith(msgHead.msgSeq)) return
        if (contentHead?.autoReply == 1) return

        val msgs = user.fragmentedMessageMerger.tryMerge(this)
        if (msgs.isEmpty()) return

        val chain = msgs.toMessageChainOnline(bot, 0, user.correspondingMessageSourceKind)
        val time = msgHead.msgTime

        collected += if (fromSync) {
            when (user) {
                is FriendImpl -> FriendMessageSyncEvent(user, chain, time)
                is StrangerImpl -> StrangerMessageSyncEvent(user, chain, time)
                is NormalMemberImpl -> GroupTempMessageSyncEvent(user, chain, time)
                else -> null
            }
        } else {
            when (user) {
                is FriendImpl -> FriendMessageEvent(user, chain, time)
                is StrangerImpl -> StrangerMessageEvent(user, chain, time)
                is NormalMemberImpl -> GroupTempMessageEvent(user, chain, time)
                else -> null
            }
        } ?: error("unreachable")
    }
}