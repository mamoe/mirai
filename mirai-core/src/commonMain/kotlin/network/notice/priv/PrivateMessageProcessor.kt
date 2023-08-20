/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.priv

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.getGroupByUinOrCode
import net.mamoe.mirai.internal.message.RefineContextKey
import net.mamoe.mirai.internal.message.SimpleRefineContext
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.KEY_FROM_SYNC
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.fromSync
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.notice.group.GroupMessageProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.PttStore
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.assertUnreachable
import net.mamoe.mirai.utils.context

/**
 * Handles [UserMessageEvent] and their sync events. Requires [KEY_FROM_SYNC].
 *
 * For [GroupMessageEvent], see [GroupMessageProcessor].
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
internal class PrivateMessageProcessor : SimpleNoticeProcessor<MsgComm.Msg>(type()) {

    internal data class SendPrivateMessageReceipt(
        val bot: Bot?,
        val messageRandom: Int,
        val sequenceId: Int,
        val fromAppId: Int,
    ) : Packet, Event, Packet.NoLog, AbstractEvent() {
        override fun toString(): String {
            return "OnlinePush.PbC2CMsgSync.SendPrivateMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
        }

        companion object {
            val EMPTY = SendPrivateMessageReceipt(null, 0, 0, 0)
        }
    }

    override suspend fun NoticePipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        markAsConsumed()
        val fromSync = attributes[KEY_FROM_SYNC, null] ?: return

        if (fromSync) {
            val msgFromAppid = msgHead.fromAppid
            // 3116 = music share
            // message sent by bot
            if (msgFromAppid == 3116) {
                handleSpecialMessageSendingResponse(data, msgFromAppid)
                return
            }
        }

        if (msgHead.fromUin == bot.id && fromSync) {
            // Bot send message to himself? or from other client? I am not the implementer.

            // This was `bot.client.sendFriendMessageSeq.updateIfSmallerThan(msgHead.msgSeq)`,
            // changed to `if (!bot.client.sendFriendMessageSeq.updateIfDifferentWith(msgHead.msgSeq)) return`
            // in 2021/12/20, 2.10.0-RC, 2.8.4, 2.9.0
            // to fix 好友无法消息同步（FriendMessageSyncEvent） #1624
            // Relevant tests: `MessageSyncTest`
            if (!bot.client.sendFriendMessageSeq.updateIfDifferentWith(msgHead.msgSeq)) return
        }

        if (!bot.components[SsoProcessor].firstLoginSucceed) return
        val senderUin = if (fromSync) msgHead.toUin else msgHead.fromUin
        when (msgHead.msgType) {
            166, 167, // 单向好友
            208, // friend ptt, maybe also support stranger
            -> {
                data.msgBody.richText.ptt?.let { ptt ->
                    if (ptt.downPara.isEmpty()) {
                        val rsp = bot.network.sendAndExpect(
                            PttStore.C2CPttDown(bot.client, senderUin, ptt.fileUuid)
                        )
                        if (rsp is PttStore.C2CPttDown.Response.Success) {
                            ptt.downPara = rsp.downloadUrl.encodeToByteArray()
                        }
                    }
                }
                handlePrivateMessage(
                    data,
                    bot.getFriend(senderUin)?.impl()
                        ?: bot.getStranger(senderUin)?.impl()
                        ?: return
                )
            }

            141, // group temp
            -> {
                val tmpHead = msgHead.c2cTmpMsgHead ?: return
                val group = bot.getGroupByUinOrCode(tmpHead.groupUin) ?: return
                handlePrivateMessage(data, group[senderUin] ?: return)
            }

            else -> markNotConsumed()
        }

    }

    private suspend fun NoticePipelineContext.handlePrivateMessage(
        data: MsgComm.Msg,
        user: AbstractUser,
    ) = data.context {
        if (!user.messageSeq.updateIfDifferentWith(msgHead.msgSeq)) return
        if (contentHead?.autoReply == 1) return

        val msgs = user.fragmentedMessageMerger.tryMerge(this)
        if (msgs.isEmpty()) return

        val chain = msgs.toMessageChainOnline(
            bot,
            0,
            user.correspondingMessageSourceKind,
            SimpleRefineContext(
                RefineContextKey.MessageSourceKind to MessageSourceKind.FRIEND,
                RefineContextKey.FromId to user.uin,
                RefineContextKey.GroupIdOrZero to 0L,
            )
        )
        val time = msgHead.msgTime

        collected += if (fromSync) {
            val client = bot.otherClients.find { it.appId == msgHead.fromInstid }
                ?: return // don't compare with dstAppId. diff.
            when (user) {
                is FriendImpl -> FriendMessageSyncEvent(client, user, chain, time)
                is StrangerImpl -> StrangerMessageSyncEvent(client, user, chain, time)
                is NormalMemberImpl -> GroupTempMessageSyncEvent(client, user, chain, time)
                is AnonymousMemberImpl -> assertUnreachable()
            }
        } else {
            when (user) {
                is FriendImpl -> FriendMessageEvent(user, chain, time)
                is StrangerImpl -> StrangerMessageEvent(user, chain, time)
                is NormalMemberImpl -> GroupTempMessageEvent(user, chain, time)
                is AnonymousMemberImpl -> assertUnreachable()
            }
        }
    }

    private fun NoticePipelineContext.handleSpecialMessageSendingResponse(
        data: MsgComm.Msg,
        fromAppId: Int,
    ) = data.context {
        val messageRandom = data.msgBody.richText.attr?.random ?: return
        collect(
            SendPrivateMessageReceipt(
                bot, messageRandom, data.msgHead.msgSeq, fromAppId
            )
        )
    }
}