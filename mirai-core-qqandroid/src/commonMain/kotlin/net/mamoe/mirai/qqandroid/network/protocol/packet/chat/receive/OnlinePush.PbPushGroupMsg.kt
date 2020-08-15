/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.MemberCardChangeEvent
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.contact.GroupImpl
import net.mamoe.mirai.qqandroid.contact.MemberImpl
import net.mamoe.mirai.qqandroid.message.toMessageChain
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Oidb0x8fc
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.utils._miraiContentToString
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.utils.warning

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

        if (pbPushMsg.msg.msgHead.fromUin == bot.id) {
            return SendGroupMessageReceipt(
                pbPushMsg.msg.msgBody.richText.attr!!.random,
                pbPushMsg.msg.msgHead.msgSeq
            )
        }

        var extraInfo: ImMsgBody.ExtraInfo? = null
        var anonymous: ImMsgBody.AnonymousGroupMsg? = null

        for (elem in pbPushMsg.msg.msgBody.richText.elems) {
            when {
                elem.extraInfo != null -> extraInfo = elem.extraInfo
                elem.anonGroupMsg != null -> anonymous = elem.anonGroupMsg
            }
        }

        val group =
            bot.getGroupOrNull(pbPushMsg.msg.msgHead.groupInfo!!.groupCode) as GroupImpl? ?: return null // 机器人还正在进群
        val sender = if (anonymous != null) {
            group.newAnonymous(anonymous.anonNick.encodeToString())
        } else {
            group[pbPushMsg.msg.msgHead.fromUin]
        } as MemberImpl

        val name = if (anonymous != null) {
            sender.nameCard
        } else {
            extraInfo?.groupCard?.takeIf { it.isNotEmpty() }?.run {
                kotlin.runCatching {
                    if (this[0] == 0x0A.toByte())
                        loadAs(Oidb0x8fc.CommCardNameBuf.serializer()).richCardName?.joinToString("") { it.text.encodeToString() }
                    else return@runCatching null
                }.getOrNull() ?: encodeToString()
            } ?: pbPushMsg.msg.msgHead.groupInfo.groupCard.takeIf { it.isNotEmpty() }
            ?: sender.nameCardOrNick // 没有 extraInfo 就从 head 里取
        }

        val flags = extraInfo?.flags ?: 0
        return GroupMessageEvent(
            senderName = name.also {
                if (it != sender.nameCard) {
                    val origin = sender._nameCard
                    sender._nameCard = name
                    MemberCardChangeEvent(origin, name, sender).broadcast()
                }
            },
            sender = sender,
            message = pbPushMsg.msg.toMessageChain(bot, groupIdOrZero = group.id, onlineSource = true),
            permission = when {
                flags and 16 != 0 -> MemberPermission.ADMINISTRATOR
                flags and 8 != 0 -> MemberPermission.OWNER
                flags == 0 -> MemberPermission.MEMBER
                else -> {
                    bot.logger.warning { "判断群 ${sender.group.id} 的群员 ${sender.id} 的权限失败: ${flags._miraiContentToString()}. 请完整截图或复制此日志并确认其真实权限后发送给 mirai 维护者以帮助解决问题." }
                    sender.permission
                }
            },
            time = pbPushMsg.msg.msgHead.msgTime
        )
    }
}
