/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(
    JavaFriendlyAPI::class
)

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendInfoImpl
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.network.MultiPacketBySequence
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.jce.OnlinePushPack
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27.SubMsgType0x27.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x44.Submsgtype0x44
import net.mamoe.mirai.internal.network.protocol.data.proto.TroopTips0x857
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils.*
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.*
import net.mamoe.mirai.utils.*


//0C 01 B1 89 BE 09 5E 3D 72 A6 00 01 73 68 FC 06 00 00 00 3C
internal object OnlinePushReqPush : IncomingPacketFactory<OnlinePushReqPush.ReqPushDecoded>(
    "OnlinePush.ReqPush",
    "OnlinePush.RespPush"
) {
    // to reduce nesting depth
    private fun List<MsgInfo>.deco(
        client: QQAndroidClient,
        mapper: ByteReadPacket.(msgInfo: MsgInfo) -> Sequence<Packet>
    ): Sequence<Packet> {
        return asSequence().filter { msg ->
            client.syncingController.onlinePushReqPushCacheList.addCache(
                QQAndroidClient.MessageSvcSyncData.OnlinePushReqPushSyncId(
                    uid = msg.lMsgUid ?: 0, sequence = msg.shMsgSeq, time = msg.uMsgTime
                )
            )
        }.flatMap { it.vMsg.read { mapper(it) } }
    }


    @ExperimentalUnsignedTypes
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): ReqPushDecoded {
        val reqPushMsg = readUniPacket(OnlinePushPack.SvcReqPushMsg.serializer(), "req")
        // bot.network.logger.debug { reqPushMsg._miraiContentToString() }

        val packets: Sequence<Packet> = reqPushMsg.vMsgInfos.deco(bot.client) { msgInfo ->
            when (msgInfo.shMsgType.toInt()) {
                732 -> {
                    val group = bot.getGroup(readUInt().toLong())
                        ?: return@deco emptySequence() // group has not been initialized

                    group.checkIsGroupImpl()

                    val internalType = readByte().toInt()
                    discardExact(1)

                    Transformers732[internalType]
                        ?.let { it(this@deco, group, bot) }
                        ?: kotlin.run {
                            bot.network.logger.debug {
                                "unknown group 732 type $internalType, data: " + readBytes().toUHexString()
                            }
                            return@deco emptySequence()
                        }
                }

                // 00 27 1A 0C 1C 2C 3C 4C 5D 00 0C 6D 00 0C 7D 00 0C 8D 00 0C 9C AC BC CC DD 00 0C EC FC 0F 0B 2A 0C 1C 2C 3C 4C 5C 6C 0B 3A 0C 1C 2C 3C 4C 5C 6C 7C 8D 00 0C 9D 00 0C AC BD 00 0C CD 00 0C DC ED 00 0C FC 0F FC 10 0B 4A 0C 1C 2C 3C 4C 5C 6C 7C 8C 96 00 0B 5A 0C 1C 2C 3C 4C 5C 6C 7C 8C 9D 00 0C 0B 6A 0C 1A 0C 1C 26 00 0B 2A 0C 0B 3A 0C 16 00 0B 4A 09 0C 0B 5A 09 0C 0B 0B 7A 0C 1C 2C 36 00 0B 8A 0C 1C 2C 36 00 0B 9A 09 0C 0B AD 00 00 1E 0A 1C 10 28 4A 18 0A 16 08 00 10 A2 FF 8C F0 03 1A 0C E6 BD 9C E6 B1 9F E7 BE A4 E5 8F 8B
                528 -> {
                    val notifyMsgBody = readJceStruct(MsgType0x210.serializer())
                    Transformers528[notifyMsgBody.uSubMsgType]
                        ?.let { processor -> processor(notifyMsgBody, bot, msgInfo) }
                        ?: kotlin.run {
                            bot.network.logger.debug {
                                "unknown group 528 type 0x${notifyMsgBody.uSubMsgType.toUHexString("")}, data: " + notifyMsgBody.vProtobuf.toUHexString()
                            }
                            return@deco emptySequence()
                        }
                }
                else -> {
                    bot.network.logger.debug { "unknown sh type ${msgInfo.shMsgType.toInt()}" }
                    bot.network.logger.debug { "data=${readBytes().toUHexString()}" }
                    return@deco emptySequence()
                }
            }
        }
        return ReqPushDecoded(reqPushMsg, packets)
    }

    @Suppress("SpellCheckingInspection")
    internal data class ReqPushDecoded(val request: OnlinePushPack.SvcReqPushMsg, val sequence: Sequence<Packet>) :
        MultiPacketBySequence<Packet>(sequence), Packet.NoLog {
        override fun toString(): String {
            return "OnlinePush.ReqPush.ReqPushDecoded"
        }
    }

    override suspend fun QQAndroidBot.handle(packet: ReqPushDecoded, sequenceId: Int): OutgoingPacket {
        return buildResponseUniPacket(client) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    servantName = "OnlinePush",
                    funcName = "SvcRespPushMsg",
                    requestId = sequenceId,
                    sBuffer = jceRequestSBuffer(
                        "resp",
                        OnlinePushPack.SvcRespPushMsg.serializer(),
                        OnlinePushPack.SvcRespPushMsg(
                            packet.request.uin,
                            packet.request.vMsgInfos.map { msg ->
                                OnlinePushPack.DelMsgInfo(
                                    fromUin = msg.lFromUin,
                                    shMsgSeq = msg.shMsgSeq,
                                    vMsgCookies = msg.vMsgCookies,
                                    uMsgTime = msg.uMsgTime // captured 0
                                )
                            }
                        )
                    )
                )
            )
        }
    }
}


internal interface Lambda732 {
    operator fun invoke(pk: ByteReadPacket, group: GroupImpl, bot: QQAndroidBot): Sequence<Packet>
}

internal inline fun lambda732(crossinline block: ByteReadPacket.(GroupImpl, QQAndroidBot) -> Sequence<Packet>): Lambda732 {
    return object : Lambda732 {
        override fun invoke(pk: ByteReadPacket, group: GroupImpl, bot: QQAndroidBot): Sequence<Packet> {
            return block(pk, group, bot)
        }
    }
}

private object Transformers732 : Map<Int, Lambda732> by mapOf(
    // mute
    0x0c to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        val operatorUin = readUInt().toLong()
        if (operatorUin == bot.id) {
            return@lambda732 emptySequence()
        }
        val operator = group[operatorUin] ?: return@lambda732 emptySequence()
        readUInt().toLong() // time
        this.discardExact(2)
        val target = readUInt().toLong()
        val timeSeconds = readInt()

        if (target == 0L) {
            val new = timeSeconds != 0
            if (group.settings.isMuteAllField == new) {
                return@lambda732 emptySequence()
            }
            group.settings.isMuteAllField = new
            return@lambda732 sequenceOf(GroupMuteAllEvent(!new, new, group, operator))
        }

        if (target == bot.id) {
            return@lambda732 when {
                group.botMuteRemaining == timeSeconds -> emptySequence()
                timeSeconds == 0 || timeSeconds == 0xFFFF_FFFF.toInt() -> {
                    group.botAsMember.checkIsMemberImpl()._muteTimestamp = 0
                    sequenceOf(BotUnmuteEvent(operator))
                }
                else -> {
                    group.botAsMember.checkIsMemberImpl()._muteTimestamp =
                        currentTimeSeconds().toInt() + timeSeconds
                    sequenceOf(BotMuteEvent(timeSeconds, operator))
                }
            }
        }

        val member = group[target] ?: return@lambda732 emptySequence()
        member.checkIsMemberImpl()

        if (member.muteTimeRemaining == timeSeconds) {
            return@lambda732 emptySequence()
        }

        member._muteTimestamp = currentTimeSeconds().toInt() + timeSeconds
        return@lambda732 if (timeSeconds == 0) sequenceOf(MemberUnmuteEvent(member, operator))
        else sequenceOf(MemberMuteEvent(member, timeSeconds, operator))
    },

    // anonymous
    0x0e to lambda732 { group: GroupImpl, _: QQAndroidBot ->
        // 匿名
        val operator = group[readUInt().toLong()] ?: return@lambda732 emptySequence()
        val new = readInt() == 0
        if (group.settings.isAnonymousChatEnabledField == new) {
            return@lambda732 emptySequence()
        }

        group.settings.isAnonymousChatEnabledField = new
        return@lambda732 sequenceOf(GroupAllowAnonymousChatEvent(!new, new, group, operator))
    },

    //系统提示
    0x14 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->

        discardExact(1)
        val grayTip = readProtoBuf(TroopTips0x857.NotifyMsgBody.serializer()).optGeneralGrayTip
        when (grayTip?.templId) {
            //戳一戳
            10043L, 1133L, 1132L, 1134L, 1135L, 1136L -> {
                //预置数据，服务器将不会提供己方已知消息
                var action = ""
                var from: Member = group.botAsMember
                var target: Member = group.botAsMember
                var suffix = ""
                grayTip.msgTemplParam.map {
                    Pair(it.name.decodeToString(), it.value.decodeToString())
                }.asSequence().forEach { (key, value) ->
                    run {
                        when (key) {
                            "action_str" -> action = value
                            "uin_str1" -> from = group[value.toLong()] ?: return@lambda732 emptySequence()
                            "uin_str2" -> target = group[value.toLong()] ?: return@lambda732 emptySequence()
                            "suffix_str" -> suffix = value
                        }
                    }
                }
                if (target.id == bot.id) {
                    return@lambda732 sequenceOf(
                        if (from.id == bot.id)
                            BotNudgedEvent.InGroup.ByBot(action, suffix, group)
                        else
                            BotNudgedEvent.InGroup.ByMember(action, suffix, from)
                    )
                }
                return@lambda732 sequenceOf(MemberNudgedEvent(from, target, action, suffix))
            }
            //龙王
            10093L, 1053L, 1054L -> {
                var now: NormalMember = group.botAsMember
                var previous: NormalMember? = null
                grayTip.msgTemplParam.asSequence().map {
                    it.name.decodeToString() to it.value.decodeToString()
                }.forEach { (key, value) ->
                    when (key) {
                        "uin" -> now = group[value.toLong()] ?: return@lambda732 emptySequence()
                        "uin_last" -> previous = group[value.toLong()] ?: return@lambda732 emptySequence()
                    }
                }
                return@lambda732 previous?.let {
                    sequenceOf(
                        GroupTalkativeChangeEvent(group, now, it),
                        MemberHonorChangeEvent.Lose(it, GroupHonorType.TALKATIVE),
                        MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE)
                    )
                } ?: sequenceOf(MemberHonorChangeEvent.Achieve(now, GroupHonorType.TALKATIVE))
            }
            else -> {
                bot.network.logger.debug {
                    "Unknown Transformers528 0x14 template\ntemplId=${grayTip?.templId}\nPermList=${grayTip?.msgTemplParam?._miraiContentToString()}"
                }
                return@lambda732 emptySequence()
            }
        }
    },
    // 传字符串信息
    0x10 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        val dataBytes = readBytes(26)

        when (dataBytes[0].toInt() and 0xFF) {
            59 -> { // TODO 应该在 Transformers528 处理
                val size = readByte().toInt() // orthodox, don't `readUByte`
                if (size < 0) {
                    // java.lang.IllegalStateException: negative array size: -100, remaining bytes=B0 E6 99 90 D8 E8 02 98 06 01
                    // java.lang.IllegalStateException: negative array size: -121, remaining bytes=03 10 D9 F7 A2 93 0D 18 E0 DB E8 CA 0B 32 22 61 34 64 31 34 64 61 64 65 65 38 32 32 34 62 64 32 35 34 65 63 37 62 62 30 33 30 66 61 36 66 61 6D 6A 38 0E 48 00 58 01 70 C8 E8 9B 07 7A AD 02 3C 7B 22 69 63 6F 6E 22 3A 22 71 71 77 61 6C 6C 65 74 5F 63 75 73 74 6F 6D 5F 74 69 70 73 5F 69 64 69 6F 6D 5F 69 63 6F 6E 2E 70 6E 67 22 2C 22 61 6C 74 22 3A 22 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 38 37 38 42 39 39 22 2C 22 74 65 78 74 22 3A 22 E6 8E A5 E9 BE 99 E7 BA A2 E5 8C 85 E4 B8 8B E4 B8 80 E4 B8 AA E6 8B BC E9 9F B3 EF BC 9A 22 7D 3E 3C 7B 22 63 6D 64 22 3A 31 2C 22 64 61 74 61 22 3A 22 6C 69 73 74 69 64 3D 31 30 30 30 30 34 35 32 30 31 32 30 30 34 30 38 31 32 30 30 31 30 39 36 31 32 33 31 34 35 30 30 26 67 72 6F 75 70 74 79 70 65 3D 31 22 2C 22 74 65 78 74 43 6F 6C 6F 72 22 3A 22 30 78 45 36 32 35 35 35 22 2C 22 74 65 78 74 22 3A 22 64 69 6E 67 22 7D 3E 82 01 0C E8 80 81 E5 83 A7 E5 85 A5 E5 AE 9A 88 01 03 92 01 04 64 69 6E 67 A0 01 00
                    // negative array size: -40, remaining bytes=D6 94 C3 8C D8 E8 02 98 06 01
                    error("negative array size: $size, remaining bytes=${readBytes().toUHexString()}")
                }

                // println(dataBytes.toUHexString())
                //println(message + ":" + dataBytes.toUHexString())

                val new = when (val message = readString(size)) {
                    "管理员已关闭群聊坦白说" -> false
                    "管理员已开启群聊坦白说" -> true
                    else -> {
                        bot.network.logger.debug { "Unknown server messages $message" }
                        return@lambda732 emptySequence()
                    }
                }

                // @Suppress("DEPRECATION")
                // if (group.settings.isConfessTalkEnabled == new) {
                //     return@lambda732 emptySequence()
                // }

                return@lambda732 sequenceOf(
                    GroupAllowConfessTalkEvent(
                        new,
                        false,
                        group,
                        false
                    )
                )
            }

            0x2D -> {
                // 修改群名. 在 Transformers528 0x27L 处理
                return@lambda732 emptySequence()
            }
            else -> {
                /*
                bot.network.logger.debug("unknown Transformer732 0xunknown type: ${dataBytes[0].toString(16)
                    .toUpperCase()}")
                bot.network.logger.debug("unknown Transformer732 0xdata= ${readBytes().toUHexString()}")
                */
                return@lambda732 emptySequence()

                /*
                if (group.name == message) {
                    return@lambda732 emptySequence()
                }

                return@lambda732 sequenceOf(
                    GroupNameChangeEvent(
                        group.name.also { group._name = message },
                        message, group, false
                    )
                )*/
            }
        }
    },

    // recall
    0x11 to lambda732 { group: GroupImpl, bot: QQAndroidBot ->
        discardExact(1)
        val proto = readProtoBuf(TroopTips0x857.NotifyMsgBody.serializer())

        val recallReminder = proto.optMsgRecall ?: return@lambda732 emptySequence()

        val operator =
            if (recallReminder.uin == bot.id) group.botAsMember
            else group[recallReminder.uin] ?: return@lambda732 emptySequence()
        val firstPkg = recallReminder.recalledMsgList.firstOrNull() ?: return@lambda732 emptySequence()

        return@lambda732 when {
            firstPkg.authorUin == bot.id && operator.id == bot.id -> emptySequence()
            else -> sequenceOf(
                MessageRecallEvent.GroupRecall(
                    bot,
                    firstPkg.authorUin,
                    recallReminder.recalledMsgList.mapToIntArray { it.seq },
                    recallReminder.recalledMsgList.mapToIntArray { it.msgRandom },
                    firstPkg.time,
                    operator,
                    group
                )
            )
        }
    }
)

internal val ignoredLambda528: Lambda528 = lambda528 { _, _ -> emptySequence() }

internal interface Lambda528 {
    operator fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet>
}

@kotlin.internal.LowPriorityInOverloadResolution
internal inline fun lambda528(crossinline block: MsgType0x210.(QQAndroidBot) -> Sequence<Packet>): Lambda528 {
    return object : Lambda528 {
        override fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet> {
            return block(msg, bot)
        }

    }
}

internal inline fun lambda528(crossinline block: MsgType0x210.(QQAndroidBot, MsgInfo) -> Sequence<Packet>): Lambda528 {
    return object : Lambda528 {
        override fun invoke(msg: MsgType0x210, bot: QQAndroidBot, msgInfo: MsgInfo): Sequence<Packet> {
            return block(msg, bot, msgInfo)
        }

    }
}

// uSubMsgType to vProtobuf
// 138 or 139: top_package/akln.java:1568
// 66: top_package/nhz.java:269
/**
 * @see MsgType0x210
 */
@OptIn(ExperimentalStdlibApi::class)
internal object Transformers528 : Map<Long, Lambda528> by mapOf(

    0x8AL to lambda528 { bot ->
        @Serializable
        class Wording(
            @ProtoNumber(1) val itemID: Int = 0,
            @ProtoNumber(2) val itemName: String = ""
        ) : ProtoBuf

        @Serializable
        class Sub8AMsgInfo(
            @ProtoNumber(1) val fromUin: Long,
            @ProtoNumber(2) val botUin: Long,
            @ProtoNumber(3) val srcId: Int,
            @ProtoNumber(4) val srcInternalId: Long,
            @ProtoNumber(5) val time: Long,
            @ProtoNumber(6) val random: Int,
            @ProtoNumber(7) val pkgNum: Int, // 1
            @ProtoNumber(8) val pkgIndex: Int, // 0
            @ProtoNumber(9) val devSeq: Int, // 0
            @ProtoNumber(12) val flag: Int, // 1
            @ProtoNumber(13) val wording: Wording
        ) : ProtoBuf

        @Serializable
        class Sub8A(
            @ProtoNumber(1) val msgInfo: List<Sub8AMsgInfo>,
            @ProtoNumber(2) val appId: Int, // 1
            @ProtoNumber(3) val instId: Int, // 1
            @ProtoNumber(4) val longMessageFlag: Int, // 0
            @ProtoNumber(5) val reserved: ByteArray? = null // struct{ boolean(1), boolean(2) }
        ) : ProtoBuf

        return@lambda528 vProtobuf.loadAs(Sub8A.serializer()).msgInfo.asSequence()
            .filter { it.botUin == bot.id }.map {
                MessageRecallEvent.FriendRecall(
                    bot = bot,
                    messageIds = intArrayOf(it.srcId),
                    messageInternalIds = intArrayOf(it.srcInternalId.toInt()),
                    messageTime = it.time.toInt(),
                    operatorId = it.fromUin
                )
            }
    },

    // Network(1994701021) 16:03:54 : unknown group 528 type 0x0000000000000026, data: 08 01 12 40 0A 06 08 F4 EF BB 8F 04 10 E7 C1 AD B8 02 18 01 22 2C 10 01 1A 1A 18 B4 DC F8 9B 0C 20 E7 C1 AD B8 02 28 06 30 02 A2 01 04 08 93 D6 03 A8 01 08 20 00 28 00 32 08 18 01 20 FE AF AF F5 05 28 00
    // VIP 进群提示
    0x26L to ignoredLambda528,
    // 提示共同好友
    0x111L to ignoredLambda528,
    // 新好友
    0xB3L to lambda528 { bot ->
        // 08 01 12 52 08 A2 FF 8C F0 03 10 00 1D 15 3D 90 5E 22 2E E6 88 91 E4 BB AC E5 B7 B2 E7 BB 8F E6 98 AF E5 A5 BD E5 8F 8B E5 95 A6 EF BC 8C E4 B8 80 E8 B5 B7 E6 9D A5 E8 81 8A E5 A4 A9 E5 90 A7 21 2A 09 48 69 6D 31 38 38 6D 6F 65 30 07 38 03 48 DD F1 92 B7 07
        val body = vProtobuf.loadAs(Submsgtype0xb3.SubMsgType0xb3.MsgBody.serializer())
        val new = Mirai._lowLevelNewFriend(
            bot, FriendInfoImpl(
                uin = body.msgAddFrdNotify.fuin,
                nick = body.msgAddFrdNotify.fuinNick,
                remark = "",
            )
        )
        bot.friends.delegate.add(new)
        return@lambda528 bot.getStranger(new.id)?.let {
            bot.strangers.remove(new.id)
            sequenceOf(StrangerRelationChangeEvent.Friended(it, new), FriendAddEvent(new))
        } ?: sequenceOf(FriendAddEvent(new))
    },
    0xE2L to lambda528 { _ ->
        // TODO: unknown. maybe messages.
        // 0A 35 08 00 10 A2 FF 8C F0 03 1A 1B E5 90 8C E6 84 8F E4 BD A0 E7 9A 84 E5 8A A0 E5 A5 BD E5 8F 8B E8 AF B7 E6 B1 82 22 0C E6 BD 9C E6 B1 9F E7 BE A4 E5 8F 8B 28 01
        // vProtobuf.loadAs(Msgtype0x210.serializer())

        return@lambda528 emptySequence()
    },
    0x44L to lambda528 { bot ->
        val msg = vProtobuf.loadAs(Submsgtype0x44.MsgBody.serializer())
        when {
            msg.msgCleanCountMsg != null -> {

            }
            msg.msgFriendMsgSync != null -> {

            }
            else -> {
                bot.network.logger.debug { "OnlinePush528 0x44L: " + msg._miraiContentToString() }
            }
        }
        return@lambda528 emptySequence()
    },
    // bot 在其他客户端被踢或主动退出而同步情况
    0xD4L to lambda528 { _ ->
        // this.soutv("0x210")
        /* @Serializable
         data class SubD4(
             // ok
             val uin: Long
         ) : ProtoBuf

         val uin = vProtobuf.loadAs(SubD4.serializer()).uin
         val group = bot.getGroupByUinOrNull(uin) ?: bot.getGroupOrNull(uin)
         return@lambda528 if (group != null && bot.groups.delegate.remove(group)) {
             group.cancel(CancellationException("Being kicked"))
             sequenceOf(BotLeaveEvent.Active(group))
         } else emptySequence()*/

        //ignore
        return@lambda528 emptySequence()
    },
    //戳一戳信息等
    0x122L to lambda528 { bot, msgInfo ->
        val body = vProtobuf.loadAs(Submsgtype0x122.Submsgtype0x122.MsgBody.serializer())
        when (body.templId) {
            //戳一戳
            1132L, 1133L, 1134L, 1135L, 1136L, 10043L -> {
                //预置数据，服务器将不会提供己方已知消息
                var from: User? = null
                var action = ""
                var target: User? = null
                var suffix = ""
                body.msgTemplParam.asSequence().map {
                    it.name.decodeToString() to it.value.decodeToString()
                }.forEach { (key, value) ->
                    when (key) {
                        "action_str" -> action = value
                        "uin_str1" -> from = bot.getFriend(value.toLong()) ?: bot.getStranger(value.toLong())
                                ?: return@lambda528 emptySequence()
                        "uin_str2" -> target = bot.getFriend(value.toLong()) ?: bot.getStranger(value.toLong())
                                ?: return@lambda528 emptySequence()
                        "suffix_str" -> suffix = value
                    }
                }
                val subject: User = bot.getFriend(msgInfo.lFromUin) ?: bot.getStranger(msgInfo.lFromUin)
                ?: return@lambda528 emptySequence()
                //机器人自己戳自己
                if ((target == null && from == null) || (target?.id == from?.id && from?.id == bot.id)) {
                    sequenceOf(BotNudgedEvent.InPrivateSession.ByBot(subject, action, suffix))
                } else sequenceOf(
                    when (subject) {
                        is Friend -> when {
                            //机器人自身为目标
                            target == null || target!!.id == bot.id -> BotNudgedEvent.InPrivateSession.ByFriend(
                                subject,
                                action,
                                suffix
                            )
                            //机器人自身为发起者
                            from == null || from!!.id == bot.id -> FriendNudgedEvent.NudgedByBot(
                                subject,
                                action,
                                suffix
                            )
                            else -> FriendNudgedEvent.NudgedByHimself(subject, action, suffix)
                        }
                        is Stranger -> when {
                            //机器人自身为目标
                            target == null || target!!.id == bot.id -> BotNudgedEvent.InPrivateSession.ByStranger(
                                subject,
                                action,
                                suffix
                            )
                            //机器人自身为发起者
                            from == null || from!!.id == bot.id -> StrangerNudgedEvent.NudgedByBot(
                                subject,
                                action,
                                suffix
                            )
                            else -> StrangerNudgedEvent.NudgedByHimself(subject, action, suffix)
                        }
                        else -> error("Internal Error: Unable to find nudge type")
                    }
                )
            }
            else -> {
                bot.logger.debug {
                    "Unknown Transformers528 0x122L template\ntemplId=${body.templId}\nPermList=${body.msgTemplParam._miraiContentToString()}"
                }
                return@lambda528 emptySequence()
            }
        }
    },
    //好友输入状态
    0x115L to lambda528 { bot ->
        val body = vProtobuf.loadAs(Submsgtype0x115.SubMsgType0x115.MsgBody.serializer())
        val friend = bot.getFriend(body.fromUin)
        val item = body.msgNotifyItem
        return@lambda528 if (friend != null && item != null) {
            sequenceOf(FriendInputStatusChangedEvent(friend, item.eventType == 1))
        } else {
            emptySequence()
        }
    },
    // 群相关,  ModFriendRemark, DelFriend, ModGroupProfile
    0x27L to lambda528 { bot ->
        fun ModFriendRemark.transform(bot: QQAndroidBot): Sequence<Packet> {
            return this.msgFrdRmk.asSequence().mapNotNull {
                val friend = bot.getFriend(it.fuin) ?: return@mapNotNull null
                val old: String
                friend.checkIsFriendImpl().friendInfo.checkIsInfoImpl()
                    .also { info -> old = info.remark }
                    .remark = it.rmkName
                // TODO: 2020/4/10 ADD REMARK QUERY
                FriendRemarkChangeEvent(friend, old, it.rmkName)
            }
        }

        fun DelFriend.transform(bot: QQAndroidBot): Sequence<Packet> {
            return this.uint64Uins.asSequence().mapNotNull {

                val friend = bot.getFriend(it) ?: return@mapNotNull null
                if (bot.friends.delegate.remove(friend)) {
                    FriendDeleteEvent(friend)
                } else null
            }
        }

        fun ModGroupProfile.transform(bot: QQAndroidBot): Sequence<Packet> {
            return this.msgGroupProfileInfos.asSequence().mapNotNull { info ->
                when (info.field) {
                    1 -> {
                        // 群名
                        val new = info.value.encodeToString()

                        val group = bot.getGroup(this.groupCode) ?: return@mapNotNull null
                        group.checkIsGroupImpl()
                        val old = group.name

                        if (new == old) return@mapNotNull null

                        val operator = if (this.cmdUin == bot.id) null
                        else group[this.cmdUin] ?: return@mapNotNull null

                        group.settings.nameField = new

                        return@mapNotNull GroupNameChangeEvent(old, new, group, operator)
                    }
                    2 -> {
                        // 头像
                        // top_package/akkz.java:3446
                        /*
                        var4 = var82.byteAt(0);
                           short var3 = (short) (var82.byteAt(1) | var4 << 8);
                           var85 = var18.method_77927(var7 + "");
                           var85.troopface = var3;
                           var85.hasSetNewTroopHead = true;
                         */
//                        bot.logger.debug(
//                            contextualBugReportException(
//                                "解析 Transformers528 0x27L ModGroupProfile 群头像修改",
//                                forDebug = "this=${this._miraiContentToString()}"
//                            )
//                        )
                        null
                    }
                    3 -> { // troop.credit.data
                        // top_package/akkz.java:3475
                        // top_package/akkz.java:3498
//                        bot.logger.debug(
//                            contextualBugReportException(
//                                "解析 Transformers528 0x27L ModGroupProfile 群 troop.credit.data",
//                                forDebug = "this=${this._miraiContentToString()}"
//                            )
//                        )
                        null
                    }

                    else -> null
                }
            }
        }

        fun ModGroupMemberProfile.transform(bot: QQAndroidBot): Sequence<Packet> {
            return this.msgGroupMemberProfileInfos.asSequence().mapNotNull { info ->
                when (info.field) {
                    1 -> { // name card
                        val new = info.value
                        val group = bot.getGroup(this.groupCode) ?: return@mapNotNull null
                        group.checkIsGroupImpl()
                        val member = group[this.uin] ?: return@mapNotNull null
                        member.checkIsMemberImpl()

                        val old = member.nameCard

                        if (new == old) return@mapNotNull null
                        member._nameCard = new

                        return@mapNotNull MemberCardChangeEvent(old, new, member)
                    }
                    2 -> {
                        if (info.value.singleOrNull()?.toInt() != 0) {
                            bot.logger.debug {
                                "Unknown Transformers528 0x27L ModGroupMemberProfile, field=${info.field}, value=${info.value}"
                            }
                        }
                        return@mapNotNull null
                    }
                    else -> {
                        bot.logger.debug {
                            "Unknown Transformers528 0x27L ModGroupMemberProfile, field=${info.field}, value=${info.value}"
                        }
                        return@mapNotNull null
                    }
                }
            }
        }

        fun ModCustomFace.transform(bot: QQAndroidBot): Sequence<Packet> {
            if (uin == bot.id) {
                return sequenceOf(BotAvatarChangedEvent(bot))
            }
            val friend = bot.getFriend(uin) ?: return emptySequence()
            return sequenceOf(FriendAvatarChangedEvent(friend))
        }

        fun ModProfile.transform(bot: QQAndroidBot): Sequence<Packet> = buildList<Packet> {
            var containsUnknown = false
            msgProfileInfos.forEach { modified ->
                when (modified.field) {
                    20002 -> { // 昵称修改
                        val value = modified.value
                        val to = value.encodeToString()
                        if (uin == bot.id) {
                            val from = bot.nick
                            if (from != to) {
                                bot.nick = to
                                bot.asFriend.checkIsFriendImpl().nick = to
                                add(BotNickChangedEvent(bot, from, to))
                            }
                        } else {
                            val friend = (bot.getFriend(uin) ?: return@forEach) as FriendImpl
                            val info = friend.friendInfo
                            val from = info.nick
                            when (info) {
                                is FriendInfoImpl -> info.nick = to
                                else -> {
                                    bot.network.logger.debug {
                                        "Unknown how to update nick for $info"
                                    }
                                }
                            }
                            add(FriendNickChangedEvent(friend, from, to))
                        }
                    }
                    else -> {
                        containsUnknown = true
                    }
                }
            }
            if (msgProfileInfos.isEmpty() || containsUnknown) {
                bot.network.logger.debug {
                    "Transformers528 0x27L: new data: ${_miraiContentToString()}"
                }
            }
        }.asSequence()

        return@lambda528 vProtobuf.loadAs(SubMsgType0x27MsgBody.serializer()).msgModInfos.asSequence()
            .flatMap {
                when {
                    it.msgModFriendRemark != null -> it.msgModFriendRemark.transform(bot)
                    it.msgDelFriend != null -> it.msgDelFriend.transform(bot)
                    it.msgModGroupProfile != null -> it.msgModGroupProfile.transform(bot)
                    it.msgModGroupMemberProfile != null -> it.msgModGroupMemberProfile.transform(bot)
                    it.msgModCustomFace != null -> it.msgModCustomFace.transform(bot)
                    it.msgModProfile != null -> it.msgModProfile.transform(bot)
                    else -> {
                        bot.network.logger.debug {
                            "Transformers528 0x27L: new data: ${it._miraiContentToString()}"
                        }
                        emptySequence()
                    }
                }
            }
        // 0A 1C 10 28 4A 18 0A 16 08 00 10 A2 FF 8C F0 03 1A 0C E6 BD 9C E6 B1 9F E7 BE A4 E5 8F 8B
    }
)
