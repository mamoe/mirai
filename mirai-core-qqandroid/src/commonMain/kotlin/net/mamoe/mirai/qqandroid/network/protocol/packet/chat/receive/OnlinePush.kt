/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.*
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.contact.GroupImpl
import net.mamoe.mirai.qqandroid.contact.MemberImpl
import net.mamoe.mirai.qqandroid.contact.checkIsInstance
import net.mamoe.mirai.qqandroid.contact.checkIsMemberImpl
import net.mamoe.mirai.qqandroid.message.toMessageChain
import net.mamoe.mirai.qqandroid.network.MultiPacketBySequence
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.qqandroid.utils._miraiContentToString
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.readString
import net.mamoe.mirai.qqandroid.utils.io.serialization.*
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId
import net.mamoe.mirai.qqandroid.utils.read
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.debug

internal class OnlinePush {
    /**
     * 接受群消息
     */
    internal object PbPushGroupMsg : IncomingPacketFactory<Packet?>("OnlinePush.PbPushGroupMsg") {
        internal class SendGroupMessageReceipt(
            val messageRandom: Int,
            val sequenceId: Int
        ) : Packet, Event, Packet.NoLog {
            override fun toString(): String {
                return "OnlinePush.PbPushGroupMsg.SendGroupMessageReceipt(messageRandom=$messageRandom, sequenceId=$sequenceId)"
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
            // 00 00 02 E4 0A D5 05 0A 4F 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 52 20 00 28 BC 3D 30 8C 82 AB F1 05 38 D2 80 E0 8C 80 80 80 80 02 4A 21 08 E7 C1 AD B8 02 10 01 18 BA 05 22 09 48 69 6D 31 38 38 6D 6F 65 30 06 38 02 42 05 4D 69 72 61 69 50 01 58 01 60 00 88 01 08 12 06 08 01 10 00 18 00 1A F9 04 0A F6 04 0A 26 08 00 10 87 82 AB F1 05 18 B7 B4 BF 30 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 E6 03 42 E3 03 12 2A 7B 34 45 31 38 35 38 32 32 2D 30 45 37 42 2D 46 38 30 46 2D 43 35 42 31 2D 33 34 34 38 38 33 37 34 44 33 39 43 7D 2E 6A 70 67 22 00 2A 04 03 00 00 00 32 60 15 36 20 39 36 6B 45 31 41 38 35 32 32 39 64 63 36 39 38 34 37 39 37 37 62 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 34 45 31 38 35 38 32 32 2D 30 45 37 42 2D 46 38 30 46 2D 43 35 42 31 2D 33 34 34 38 38 33 37 34 44 33 39 43 7D 2E 6A 70 67 31 32 31 32 41 38 C6 BB 8A A9 08 40 FB AE 9E C2 09 48 50 50 41 5A 00 60 01 6A 10 4E 18 58 22 0E 7B F8 0F C5 B1 34 48 83 74 D3 9C 72 59 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 31 39 38 3F 74 65 72 6D 3D 32 82 01 57 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 30 3F 74 65 72 6D 3D 32 B0 01 4D B8 01 2E C8 01 FF 05 D8 01 4D E0 01 2E FA 01 59 2F 67 63 68 61 74 70 69 63 5F 6E 65 77 2F 31 30 34 30 34 30 30 32 39 30 2F 36 35 35 30 35 37 31 32 37 2D 32 32 33 33 36 33 38 33 34 32 2D 34 45 31 38 35 38 32 32 30 45 37 42 46 38 30 46 43 35 42 31 33 34 34 38 38 33 37 34 44 33 39 43 2F 34 30 30 3F 74 65 72 6D 3D 32 80 02 4D 88 02 2E 12 45 AA 02 42 50 03 60 00 68 00 9A 01 39 08 09 20 BF 50 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 04 08 02 08 01 90 04 80 80 80 10 B8 04 00 C0 04 00 12 06 4A 04 08 00 40 01 12 14 82 01 11 0A 09 48 69 6D 31 38 38 6D 6F 65 18 06 20 08 28 03 10 8A CA 9D A1 07 1A 00
            if (!bot.firstLoginSucceed) return null
            val pbPushMsg = readProtoBuf(MsgOnlinePush.PbPushMsg.serializer())

            val extraInfo: ImMsgBody.ExtraInfo? =
                pbPushMsg.msg.msgBody.richText.elems.firstOrNull { it.extraInfo != null }?.extraInfo

            if (pbPushMsg.msg.msgHead.fromUin == bot.id) {
                return SendGroupMessageReceipt(
                    pbPushMsg.msg.msgBody.richText.attr!!.random,
                    pbPushMsg.msg.msgHead.msgSeq
                )
            }

            val group = bot.getGroup(pbPushMsg.msg.msgHead.groupInfo!!.groupCode)
            val sender = group[pbPushMsg.msg.msgHead.fromUin] as MemberImpl
            val name = extraInfo?.groupCard?.run {
                try {
                    loadAs(Oidb0x8fc.CommCardNameBuf.serializer()).richCardName!!.first { it.text.isNotEmpty() }
                        .text.encodeToString()
                } catch (e: Exception) {
                    encodeToString()
                }
            } ?: pbPushMsg.msg.msgHead.groupInfo.groupCard // 没有 extraInfo 就从 head 里取

            val flags = extraInfo?.flags ?: 0
            return GroupMessage(
                senderName = name.also {
                    if (it != sender.nameCard) {
                        val origin = sender._nameCard
                        sender._nameCard = name
                        MemberCardChangeEvent(origin, name, sender, sender).broadcast() // 不知道operator
                    }
                },
                sender = sender,
                message = pbPushMsg.msg.toMessageChain(bot, groupIdOrZero = group.id, onlineSource = true),
                permission = when {
                    flags and 16 != 0 -> MemberPermission.ADMINISTRATOR
                    flags and 8 != 0 -> MemberPermission.OWNER
                    flags == 0 -> MemberPermission.MEMBER
                    else -> {
                        bot.logger.warning("判断群员权限失败: ${pbPushMsg.msg.msgHead._miraiContentToString()}. 请完整截图或复制此日志发送给 mirai 维护者以帮助解决问题.")
                        MemberPermission.MEMBER
                    }
                }
            )
        }
    }

    internal object PbPushTransMsg :
        IncomingPacketFactory<Packet?>("OnlinePush.PbPushTransMsg", "OnlinePush.RespPush") {

        @OptIn(MiraiInternalAPI::class)
        @ExperimentalUnsignedTypes
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
            val content = this.readProtoBuf(OnlinePushTrans.PbMsgInfo.serializer())
            content.msgData.read<Unit> {
                when (content.msgType) {
                    44 -> {
                        this.discardExact(5)
                        val var4 = readByte().toInt()
                        var var5 = 0L
                        val target = readUInt().toLong()
                        if (var4 != 0 && var4 != 1) {
                            var5 = readUInt().toLong()
                        }

                        val group = bot.getGroupByUin(content.fromUin) as GroupImpl
                        if (var5 == 0L && this.remaining == 1L) {//管理员变更
                            val newPermission =
                                if (this.readByte()
                                        .toInt() == 1
                                ) MemberPermission.ADMINISTRATOR else MemberPermission.MEMBER

                            return if (target == bot.id) {
                                BotGroupPermissionChangeEvent(
                                    group,
                                    group.botPermission.also { group.botPermission = newPermission },
                                    newPermission
                                )
                            } else {
                                val member = group[target] as MemberImpl
                                MemberPermissionChangeEvent(
                                    member,
                                    member.permission.also { member.permission = newPermission },
                                    newPermission
                                )
                            }
                        }
                    }
                    34 -> {
                        /* quit
                        27 0B 60 E7
                        01
                        2F 55 7C B8
                        82
                        00 30 42 33 32 46 30 38 33 32 39 32 35 30 31 39 33 45 46 32 45 30 36 35 41 35 41 33 42 37 35 43 41 34 46 37 42 38 42 38 42 44 43 35 35 34 35 44 38 30
                         */
                        /* kick
                        27 0B 60 E7
                        01
                        A8 32 51 A1
                        83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 39 32 46 45 30 36 31 41 33 37 36 43 44 35 37 35 37 39 45 37 32 34 44 37 37 30 36 46 39 39 43 35 35 33 33 31 34 44 32 44 46 35 45 42 43 31 31 36
                         */
                        readUInt().toLong() // group, uin or code ?

                        discardExact(1)
                        val target = readUInt().toLong()
                        val type = readUByte().toInt()
                        val operator = readUInt().toLong()
                        val groupUin = content.fromUin

                        when (type) {
                            0x82 -> { // 2020/4/8: 在这里拿到了一个 Group xxx not found
                                bot.getGroupByUinOrNull(groupUin)?.let { group ->
                                    val member = group.getOrNull(target) as? MemberImpl ?: return null
                                    return MemberLeaveEvent.Quit(member.also {
                                        group.members.delegate.remove(member)
                                    })
                                }
                            }
                            0x83 -> {
                                bot.getGroupByUin(groupUin).let { group ->
                                    val member = group.getOrNull(target) as? MemberImpl ?: return null
                                    return MemberLeaveEvent.Kick(member.also {
                                        group.members.delegate.remove(member)
                                    }, group.members[operator])
                                }
                            }
                        }
                    }
                }
            }
            return null
        }

        override suspend fun QQAndroidBot.handle(packet: Packet?, sequenceId: Int): OutgoingPacket? {
            return buildResponseUniPacket(client, sequenceId = sequenceId) {}
        }

    }

    //0C 01 B1 89 BE 09 5E 3D 72 A6 00 01 73 68 FC 06 00 00 00 3C
    internal object ReqPush : IncomingPacketFactory<ReqPush.Response>(
        "OnlinePush.ReqPush",
        "OnlinePush.RespPush"
    ) {
        // to reduce nesting depth
        private fun List<MsgInfo>.deco(mapper: ByteReadPacket.(msgInfo: MsgInfo) -> Sequence<Packet>): Sequence<Packet> {
            return asSequence().flatMap { it.vMsg.read { mapper(it) } }
        }

        private fun lambda(block: ByteReadPacket.(group: GroupImpl, bot: QQAndroidBot) -> Sequence<Packet>):
                ByteReadPacket.(group: GroupImpl, bot: QQAndroidBot) -> Sequence<Packet> {
            return block
        }

        object Transformers732 : Map<Int, ByteReadPacket.(GroupImpl, QQAndroidBot) -> Sequence<Packet>> by mapOf(
            0x0c to lambda { group: GroupImpl, bot: QQAndroidBot ->
                val operatorUin = readUInt().toLong()
                if (operatorUin == bot.id) {
                    return@lambda emptySequence()
                }
                val operator = group.getOrNull(operatorUin) ?: return@lambda emptySequence()
                readUInt().toLong() // time
                this.discardExact(2)
                val target = readUInt().toLong()
                val time = readInt()

                if (target == 0L) {
                    val new = time != 0
                    if (group.settings.isMuteAll == new) {
                        return@lambda emptySequence()
                    }
                    group._muteAll = new
                    return@lambda sequenceOf(GroupMuteAllEvent(!new, new, group, operator))
                }

                if (target == bot.id) {
                    return@lambda when {
                        group._botMuteTimestamp == time -> emptySequence()
                        time == 0 -> {
                            group._botMuteTimestamp = 0
                            sequenceOf(BotUnmuteEvent(operator))
                        }
                        else -> {
                            group._botMuteTimestamp = time
                            sequenceOf(BotMuteEvent(time, operator))
                        }
                    }
                }

                val member = group.getOrNull(target) ?: return@lambda emptySequence()
                member.checkIsMemberImpl()

                if (member._muteTimestamp == time) {
                    return@lambda emptySequence()
                }

                member._muteTimestamp = time
                return@lambda if (time == 0) sequenceOf(MemberUnmuteEvent(member, operator))
                else sequenceOf(MemberMuteEvent(member, time, operator))
            },

            0x0e to lambda { group: GroupImpl, _: QQAndroidBot ->
                // 匿名
                val operator = group.getOrNull(readUInt().toLong()) ?: return@lambda emptySequence()
                val new = readInt() == 0
                if (group.settings.isAnonymousChatEnabled == new) {
                    return@lambda emptySequence()
                }

                group._anonymousChat = new
                return@lambda sequenceOf(GroupAllowAnonymousChatEvent(!new, new, group, operator))
            },

            0x10 to lambda { group: GroupImpl, bot: QQAndroidBot ->
                val dataBytes = readBytes(26)
                val size = readByte().toInt() // orthodox, don't `readUByte`
                if (size < 0) {
                    // java.lang.IllegalStateException: negative array size: -100, remaining bytes=B0 E6 99 90 D8 E8 02 98 06 01
                    // java.lang.IllegalStateException: negative array size: -121, remaining bytes=03 10 D9 F7 A2 93 0D 18 E0 DB E8 CA 0B 32 22 61 34 64 31 34 64 61 64 65 65 38 32 32 34 62 64 32 35 34 65 63 37 62 62 30 33 30 66 61 36 66 61 6D 6A 38 0E 48 00 58 01 70 C8 E8 9B 07 7A AD 02 3C 7B 22 69 63 6F 6E 22 3A 22 71 71 77 61 6C 6C 65 74 5F 63 75 73 74 6F 6D 5F 74 69 70 73 5F 69 64 69 6F 6D 5F 69 63 6F 6E 2E 70 6E 67 22 2C 22 61 6C 74 22 3A 22 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 38 37 38 42 39 39 22 2C 22 74 65 78 74 22 3A 22 E6 8E A5 E9 BE 99 E7 BA A2 E5 8C 85 E4 B8 8B E4 B8 80 E4 B8 AA E6 8B BC E9 9F B3 EF BC 9A 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 45 36 32 35 35 35 22 2C 22 74 65 78 74 22 3A 22 64 69 6E 67 22 7D 3E 82 01 0C E8 80 81 E5 83 A7 E5 85 A5 E5 AE 9A 88 01 03 92 01 04 64 69 6E 67 A0 01 00
                    // negative array size: -40, remaining bytes=D6 94 C3 8C D8 E8 02 98 06 01
                    error("negative array size: $size, remaining bytes=${readBytes().toUHexString()}")
                }

                val message = readString(size)
                // println(dataBytes.toUHexString())
                //println(message + ":" + dataBytes.toUHexString())

                when (dataBytes[0].toInt()) {
                    59 -> { // confess
                        val new = when (message) {
                            "管理员已关闭群聊坦白说" -> false
                            "管理员已开启群聊坦白说" -> true
                            else -> {
                                bot.network.logger.debug { "Unknown server messages $message" }
                                return@lambda emptySequence()
                            }
                        }

                        if (group.settings.isConfessTalkEnabled == new) {
                            return@lambda emptySequence()
                        }

                        return@lambda sequenceOf(
                            GroupAllowConfessTalkEvent(
                                new,
                                false,
                                group,
                                false
                            )
                        )
                    }

                    else -> { // TODO SHOULD BE SPECIFIED TYPE
                        if (group.name == message) {
                            return@lambda emptySequence()
                        }

                        return@lambda sequenceOf(
                            GroupNameChangeEvent(
                                group.name.also { group._name = message },
                                message, group, false
                            )
                        )
                    }
                }
            },

            // recall
            0x11 to lambda { group: GroupImpl, bot: QQAndroidBot ->
                discardExact(1)
                val proto = readProtoBuf(TroopTips0x857.NotifyMsgBody.serializer())

                val recallReminder = proto.optMsgRecall ?: return@lambda emptySequence()

                val operator =
                    if (recallReminder.uin == bot.id) group.botAsMember
                    else group.getOrNull(recallReminder.uin) ?: return@lambda emptySequence()

                return@lambda recallReminder.recalledMsgList.asSequence().mapNotNull { pkg ->
                    when {
                        pkg.authorUin == bot.id && operator.id == bot.id -> null
                        group.lastRecalledMessageRandoms.remove(pkg.msgRandom) -> null
                        else -> {
                            group.lastRecalledMessageRandoms.addLast(pkg.msgRandom)
                            MessageRecallEvent.GroupRecall(bot, pkg.authorUin, pkg.msgRandom, pkg.time, operator, group)
                        }
                    }
                }
            }
        )

        @ExperimentalUnsignedTypes
        @OptIn(ExperimentalStdlibApi::class)
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Response {
            val reqPushMsg = decodeUniPacket(OnlinePushPack.SvcReqPushMsg.serializer(), "req")

            val packets: Sequence<Packet> = reqPushMsg.vMsgInfos.deco { msgInfo ->
                when (msgInfo.shMsgType.toInt()) {
                    732 -> {
                        val group = bot.getGroup(readUInt().toLong())
                        GroupImpl.checkIsInstance(group)

                        val internalType = readByte().toInt()
                        discardExact(1)

                        Transformers732[internalType]
                            ?.let { it(this@deco, group, bot) }
                            ?: kotlin.run {
                                bot.network.logger.debug {
                                    "unknown group internal type $internalType , data: " + readBytes()
                                        .toUHexString() + " "
                                }
                                return@deco emptySequence()
                            }
                    }

//                            528 -> {
//                                val notifyMsgBody = msgInfo.vMsg.loadAs(MsgType0x210.serializer())
//                                OnlinePush0x210Factory.solve()
//                                return@flatMap emptySequence()
//                            }
                    else -> {
                        bot.network.logger.debug { "unknown sh type ${msgInfo.shMsgType.toInt()}" }
                        return@deco emptySequence()
                    }
                }
            }
            return Response(reqPushMsg.uin, reqPushMsg.svrip ?: 0, packets)
        }

        @Suppress("SpellCheckingInspection")
        internal class Response(val uin: Long, val svrip: Int, sequence: Sequence<Packet>) :
            MultiPacketBySequence<Packet>(sequence)

        @Serializable
        private class Resp(
            @JceId(0) val var1: Long,
            @JceId(2) val var2: Int
        ) : JceStruct

        override suspend fun QQAndroidBot.handle(packet: Response, sequenceId: Int): OutgoingPacket? {
            return buildResponseUniPacket(client, sequenceId = sequenceId) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sServantName = "OnlinePush.RespPush",
                        sFuncName = "SvcRespPushMsg",
                        iRequestId = 0,
                        sBuffer = jceRequestSBuffer(
                            "resp",
                            Resp.serializer(),
                            Resp(packet.uin, packet.svrip)
                        )
                    )
                )
            }
        }
    }
}