/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupMessageSyncEvent
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.contact.NormalMemberImpl
import net.mamoe.mirai.internal.contact.newAnonymous
import net.mamoe.mirai.internal.message.toMessageChain
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x8fc
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.*

/**
 * 接受群消息
 */
internal object OnlinePushPbPushGroupMsg : IncomingPacketFactory<Packet?>("OnlinePush.PbPushGroupMsg") {
    internal class SendGroupMessageReceipt(
        val messageRandom: Int,
        val sequenceId: Int
    ) : Packet, Event, Packet.NoLog, AbstractEvent() {
        override fun toString(): String {
            return "OnlinePush.PbPushGroupMsg.SendGroupMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        // 00 00 02 E4 0A D5 05 0A 4F 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 52 20 00 28 BC 3D 30 8C 82 AB F1 05 38 D2 80 E0 8C 80 80 80 80 02 4A 21 08 E7 C1 AD B8 02 10 01 18 BA 05 22 09 48 69 6D 31 38 38 6D 6F 65 30 06 38 02 42 05 4D 69 72 61 69 50 01 58 01 60 00 88 01 08 12 06 08 01 10 00 18 00 1A F9 04 0A F6 04 0A 26 08 00 10 87 82 AB F1 05 18 B7 B4 BF 30 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 E6 03 42 E3 03 12 2A 7B 34 45 31 38 35 38 32 32 2D 30 45 37 42 2D 46 38 30 46 2D 43 35 42 31 2D 33 34 34 38 38 33 37 34 44 33 39 43 7D 2E 6A 70 67 22 00 2A 04 03 00 00 00 32 60 15 36 20 39 36 6B 45 31 41 38 35 32 32 39 64 63 36 39 38 34 37 39 37 37 62 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 34 45 31 38 35 38 32 32 2D 30 45 37 42 2D 46 38 30 46 2D 43 35 42 31 2D 33 34 34 38 38 33 37 34 44 33 39 43 7D 2E 6A 70 67 31 32 31 32 41 38 C6 BB 8A A9 08 40 FB AE 9E C2 09 48 50 50 41 5A 00 60 01 6A 10 4E 18 58 22 0E 7B F8 0F C5 B1 34 48 83 74 D3 9C 72 59 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 31 39 38 3F 74 65 72 6D 3D 32 82 01 57 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 30 3F 74 65 72 6D 3D 32 B0 01 4D B8 01 2E C8 01 FF 05 D8 01 4D E0 01 2E FA 01 59 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 34 30 30 3F 74 65 72 6D 3D 32 80 02 4D 88 02 2E 12 45 AA 02 42 50 03 60 00 68 00 9A 01 39 08 09 20 BF 50 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 04 08 02 08 01 90 04 80 80 80 10 B8 04 00 C0 04 00 12 06 4A 04 08 00 40 01 12 14 82 01 11 0A 09 48 69 6D 31 38 38 6D 6F 65 18 06 20 08 28 03 10 8A CA 9D A1 07 1A 00
        if (!bot.firstLoginSucceed) return null
        val pbPushMsg = readProtoBuf(MsgOnlinePush.PbPushMsg.serializer())

        val msgHead = pbPushMsg.msg.msgHead

        val isFromSelfAccount = msgHead.fromUin == bot.id
        if (isFromSelfAccount) {
            val messageRandom = pbPushMsg.msg.msgBody.richText.attr?.random ?: return null

            if (bot.client.syncingController.pendingGroupMessageReceiptCacheList.contains { it.messageRandom == messageRandom }) {
                // message sent by bot
                return SendGroupMessageReceipt(
                    messageRandom,
                    msgHead.msgSeq
                )
            }
            // else: sync form other device
        }

        if (msgHead.groupInfo == null) return null

        val group = bot.getGroup(msgHead.groupInfo.groupCode) as GroupImpl? ?: return null // 机器人还正在进群


        // fragmented message
        val msgs = group.groupPkgMsgParsingCache.tryMerge(pbPushMsg).ifEmpty { return null }

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
        val name: String

        if (anonymous != null) { // anonymous member
            sender = group.newAnonymous(anonymous.anonNick.encodeToString(), anonymous.anonId.encodeToBase64())
            name = sender.nameCard
        } else { // normal member chat
            sender = group[msgHead.fromUin] as NormalMemberImpl
            name = findSenderName(extraInfo, msgHead.groupInfo) ?: sender.nameCardOrNick
        }

        if (isFromSelfAccount) {
            return GroupMessageSyncEvent(
                message = msgs.toMessageChain(
                    bot,
                    groupIdOrZero = group.id,
                    onlineSource = true,
                    MessageSourceKind.GROUP
                ),
                time = msgHead.msgTime,
                group = group,
                sender = sender,
                senderName = name,
            )
        } else {

            broadcastNameCardChangedEventIfNecessary(sender, name)

            return GroupMessageEvent(
                senderName = name,
                sender = sender,
                message = msgs.toMessageChain(
                    bot,
                    groupIdOrZero = group.id,
                    onlineSource = true,
                    MessageSourceKind.GROUP
                ),
                permission = findMemberPermission(extraInfo?.flags ?: 0, sender, bot),
                time = msgHead.msgTime
            )
        }
    }

    private suspend inline fun broadcastNameCardChangedEventIfNecessary(sender: Member, name: String) {
        val currentNameCard = sender.nameCard
        if (sender is NormalMemberImpl && name != currentNameCard) {
            sender._nameCard = name
            MemberCardChangeEvent(currentNameCard, name, sender).broadcast()
        }
    }

    private fun findMemberPermission(
        flags: Int,
        sender: Member,
        bot: QQAndroidBot,
    ) = when {
        flags and 16 != 0 -> MemberPermission.ADMINISTRATOR
        flags and 8 != 0 -> MemberPermission.OWNER
        flags == 0 || flags == 1 -> MemberPermission.MEMBER
        else -> {
            bot.logger.warning { "判断群 ${sender.group.id} 的群员 ${sender.id} 的权限失败: ${flags._miraiContentToString()}. 请完整截图或复制此日志并确认其真实权限后发送给 mirai 维护者以帮助解决问题." }
            sender.permission
        }
    }

    private fun findSenderName(
        extraInfo: ImMsgBody.ExtraInfo?,
        groupInfo: MsgComm.GroupInfo
    ) = extraInfo?.groupCard?.takeIf { it.isNotEmpty() }?.decodeCommCardNameBuf()
        ?: groupInfo.groupCard.takeIf { it.isNotEmpty() }

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
