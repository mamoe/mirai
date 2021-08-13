/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.group

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.NormalMemberImpl
import net.mamoe.mirai.internal.contact.info
import net.mamoe.mirai.internal.contact.info.MemberInfoImpl
import net.mamoe.mirai.internal.contact.newAnonymous
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.components.SyncController.Companion.syncController
import net.mamoe.mirai.internal.network.notice.group.GroupMessageProcessor.MemberNick.Companion.generateMemberNickFromMember
import net.mamoe.mirai.internal.network.notice.priv.PrivateMessageNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x8fc
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.*

/**
 * Handles [GroupMessageEvent]. For private message events, see [PrivateMessageNoticeProcessor]
 */
internal class GroupMessageProcessor(
    private val logger: MiraiLogger,
) : SimpleNoticeProcessor<MsgOnlinePush.PbPushMsg>(type()) {
    internal data class SendGroupMessageReceipt(
        val messageRandom: Int,
        val sequenceId: Int,
        val fromAppId: Int,
    ) : Packet, Event, Packet.NoLog, AbstractEvent() {
        override fun toString(): String {
            return "OnlinePush.PbPushGroupMsg.SendGroupMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
        }

        companion object {
            val EMPTY = SendGroupMessageReceipt(0, 0, 0)
        }
    }

    private data class MemberNick(val nick: String, val isNameCard: Boolean = false) {
        companion object {
            fun Member.generateMemberNickFromMember(): MemberNick {
                return nameCard.takeIf { nameCard.isNotEmpty() }?.let {
                    MemberNick(nameCard, true)
                } ?: MemberNick(nick, false)
            }
        }
    }


    override suspend fun PipelineContext.processImpl(data: MsgOnlinePush.PbPushMsg) {
        val msgHead = data.msg.msgHead

        val isFromSelfAccount = msgHead.fromUin == bot.id
        if (isFromSelfAccount) {
            val messageRandom = data.msg.msgBody.richText.attr?.random ?: return

            if (bot.syncController.containsGroupMessageReceipt(messageRandom)
                || msgHead.fromAppid == 3116 || msgHead.fromAppid == 2021
            ) {
                // 3116=group music share
                // 2021=group file
                // message sent by bot
                collect(SendGroupMessageReceipt(messageRandom, msgHead.msgSeq, msgHead.fromAppid))
                return
            }
            // else: sync form other device
        }

        if (msgHead.groupInfo == null) return

        val group = bot.getGroup(msgHead.groupInfo.groupCode) as GroupImpl? ?: return // 机器人还正在进群


        // fragmented message
        val msgs = group.groupPkgMsgParsingCache.tryMerge(data).ifEmpty { return }

        var extraInfo: ImMsgBody.ExtraInfo? = null
        var anonymous: ImMsgBody.AnonymousGroupMsg? = null

        for (msg in msgs) {
            for (elem in msg.msg.msgBody.richText.elems) {
                when {
                    elem.extraInfo != null -> extraInfo = elem.extraInfo
                    elem.anonGroupMsg != null -> anonymous = elem.anonGroupMsg
                }
            }
        }


        val sender: Member  // null if sync from other client
        val nameCard: MemberNick

        if (anonymous != null) { // anonymous member
            sender = group.newAnonymous(anonymous.anonNick.encodeToString(), anonymous.anonId.encodeBase64())
            nameCard = sender.generateMemberNickFromMember()
        } else { // normal member chat
            sender = group[msgHead.fromUin] ?: kotlin.run {
                logger.warning { "Failed to find member ${msgHead.fromUin} in group ${group.id}" }
                return
            }
            nameCard = findSenderName(extraInfo, msgHead.groupInfo) ?: sender.generateMemberNickFromMember()
        }

        sender.info?.castOrNull<MemberInfoImpl>()?.run {
            lastSpeakTimestamp = currentTimeSeconds().toInt()
        }

        if (isFromSelfAccount) {
            collect(
                GroupMessageSyncEvent(
                    message = msgs.map { it.msg }.toMessageChainOnline(bot, group.id, MessageSourceKind.GROUP),
                    time = msgHead.msgTime,
                    group = group,
                    sender = sender,
                    senderName = nameCard.nick,
                ),
            )
            return
        } else {

            broadcastNameCardChangedEventIfNecessary(sender, nameCard)

            collect(
                GroupMessageEvent(
                    senderName = nameCard.nick,
                    sender = sender,
                    message = msgs.map { it.msg }.toMessageChainOnline(bot, group.id, MessageSourceKind.GROUP),
                    permission = sender.permission,
                    time = msgHead.msgTime,
                ),
            )
            return
        }
    }

    private suspend inline fun broadcastNameCardChangedEventIfNecessary(
        sender: Member,
        new: MemberNick,
    ) {
        if (sender is NormalMemberImpl) {
            val currentNameCard = sender.nameCard
            if (new.isNameCard) {
                new.nick.let { name ->
                    if (currentNameCard != name) {
                        sender._nameCard = name
                        MemberCardChangeEvent(currentNameCard, name, sender).broadcast()
                    }
                }
            } else {
                // 说明删除了群名片
                if (currentNameCard.isNotEmpty()) {
                    sender._nameCard = ""
                    MemberCardChangeEvent(currentNameCard, "", sender).broadcast()
                }
            }
        }
    }

    private fun findSenderName(
        extraInfo: ImMsgBody.ExtraInfo?,
        groupInfo: MsgComm.GroupInfo,
    ): MemberNick? =
        extraInfo?.groupCard?.takeIf { it.isNotEmpty() }?.decodeCommCardNameBuf()?.let {
            MemberNick(it, true)
        } ?: groupInfo.takeIf { it.groupCard.isNotEmpty() }?.let {
            MemberNick(it.groupCard, it.groupCardType != 2)
        }

    private fun ByteArray.decodeCommCardNameBuf() = kotlin.runCatching {
        if (this[0] == 0x0A.toByte()) {
            val nameBuf = loadAs(Oidb0x8fc.CommCardNameBuf.serializer())
            if (nameBuf.richCardName.isNotEmpty()) {
                return@runCatching nameBuf.richCardName.joinToString("") { it.text.encodeToString() }
            }
        }
        return@runCatching null
    }.getOrNull() ?: encodeToString()
}