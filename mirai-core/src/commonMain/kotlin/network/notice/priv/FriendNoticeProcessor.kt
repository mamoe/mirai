/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.notice.priv

import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.toMiraiFriendInfo
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.NoticePipelineContext
import net.mamoe.mirai.internal.network.components.NoticePipelineContext.Companion.msgInfo
import net.mamoe.mirai.internal.network.notice.NewContactSupport
import net.mamoe.mirai.internal.network.notice.group.get
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.FrdSysMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x115.SubMsgType0x115
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x122
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27.SubMsgType0x27.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x44.Submsgtype0x44
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0xb3.SubMsgType0xb3
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList.GetFriendGroupList
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.utils.*

/**
 * All [FriendEvent] except [FriendMessageEvent]
 *
 * @see FriendInputStatusChangedEvent
 * @see FriendAddEvent
 * @see StrangerRelationChangeEvent.Friended
 */
internal class FriendNoticeProcessor(
    private val logger: MiraiLogger,
) : MixedNoticeProcessor(), NewContactSupport {
    override suspend fun NoticePipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        if (msgHead.msgType != 191) return

        var fromGroup = 0L
        var pbNick = ""
        msgBody.msgContent.read {
            readByte().toUByte() // version
            discardExact(readByte().toUByte().toInt()) //skip
            readShort().toUShort() //source id
            readShort().toUShort() //SourceSubID
            discardExact(readShort().toUShort().toLong()) //skip size
            if (readShort().toUShort().toInt() != 0) { //hasExtraInfo
                discardExact(readShort().toUShort().toInt()) //mail address info, skip
            }
            discardExact(4 + readShort().toUShort().toInt()) //skip
            for (i in 1..readByte().toUByte().toInt()) { //pb size
                val type = readShort().toUShort().toInt()
                val pbArray = ByteArray(readShort().toUShort().toInt() and 0xFF)
                readAvailable(pbArray)
                when (type) {
                    1000 -> pbArray.loadAs(FrdSysMsg.GroupInfo.serializer()).let { fromGroup = it.groupUin }
                    1002 -> pbArray.loadAs(FrdSysMsg.FriendMiscInfo.serializer())
                        .let { pbNick = it.fromuinNick }

                    else -> {
                    } //ignore
                }
            }
        }

        msgHead.context {
            // 对方 qq
            val id = longArrayOf(fromUin, authUin).firstOrNull { it != 0L && it != bot.id }
            if (id == null) {
                logger.error { "Could not determine uin for new stranger" }
                return
            }
            if (bot.getStranger(id) != null) return

            val nick = fromNick.ifEmpty { authNick }.ifEmpty { pbNick }
            collect(StrangerAddEvent(bot.addNewStranger(StrangerInfoImpl(id, nick, fromGroup)) ?: return))
            //同时需要请求好友验证消息（有新请求需要同意）
            bot.network.sendWithoutExpect(NewContact.SystemMsgNewFriend(bot.client))
        }

    }

    override suspend fun NoticePipelineContext.processImpl(data: MsgType0x210) = data.context {
        markAsConsumed()
        when (data.uSubMsgType) {
            0xB3L -> {
                // 08 01 12 52 08 A2 FF 8C F0 03 10 00 1D 15 3D 90 5E 22 2E E6 88 91 E4 BB AC E5 B7 B2 E7 BB 8F E6 98 AF E5 A5 BD E5 8F 8B E5 95 A6 EF BC 8C E4 B8 80 E8 B5 B7 E6 9D A5 E8 81 8A E5 A4 A9 E5 90 A7 21 2A 09 48 69 6D 31 38 38 6D 6F 65 30 07 38 03 48 DD F1 92 B7 07
                val body: SubMsgType0xb3.MsgBody = vProtobuf.loadAs(SubMsgType0xb3.MsgBody.serializer())
                handleFriendAddedB(data, body)
            }

            0x44L -> {
                val body = vProtobuf.loadAs(Submsgtype0x44.MsgBody.serializer())
                handleFriendAddedA(body)
            }

            0x27L -> {
                val body = vProtobuf.loadAs(SubMsgType0x27MsgBody.serializer())
                for (msgModInfo in body.msgModInfos) {
                    when {
                        msgModInfo.msgModFriendRemark != null -> handleRemarkChanged(msgModInfo.msgModFriendRemark)
                        msgModInfo.msgDelFriend != null -> handleFriendDeleted(msgModInfo.msgDelFriend)
                        msgModInfo.msgModCustomFace != null -> handleAvatarChanged(msgModInfo.msgModCustomFace)
                        msgModInfo.msgModProfile != null -> handleProfileChanged(msgModInfo.msgModProfile)
                    }
                }
            }

            0x115L -> {
                val body = vProtobuf.loadAs(SubMsgType0x115.MsgBody.serializer())
                handleInputStatusChanged(body)
            }

            0x122L -> {
                val body = vProtobuf.loadAs(Submsgtype0x122.Submsgtype0x122.MsgBody.serializer())
                when (body.templId) {
                    //戳一戳
                    1132L, 1133L, 1134L, 1135L, 1136L, 10043L -> handlePrivateNudge(body)
                }
            }

            0x8AL -> {
                val body = vProtobuf.loadAs(Sub8A.serializer())
                processFriendRecall(body)
            }

            else -> markNotConsumed()
        }
    }


    @Serializable
    private class Wording(
        @ProtoNumber(1) val itemID: Int = 0,
        @ProtoNumber(2) val itemName: String = "",
    ) : ProtoBuf

    @Serializable
    private class Sub8AMsgInfo(
        @ProtoNumber(1) val fromUin: Long,
        @ProtoNumber(2) val botUin: Long,
        @ProtoNumber(3) val srcId: Int,
        @ProtoNumber(4) val srcInternalId: Long,
        @ProtoNumber(5) val time: Long,
        // see #2784
//        @ProtoNumber(6) val random: Int,
//        @ProtoNumber(7) val pkgNum: Int, // 1
//        @ProtoNumber(8) val pkgIndex: Int, // 0
//        @ProtoNumber(9) val devSeq: Int, // 0
//        @ProtoNumber(12) val flag: Int, // 1
//        @ProtoNumber(13) val wording: Wording,
    ) : ProtoBuf

    @Serializable
    private class Sub8A(
        @ProtoNumber(1) val msgInfo: List<Sub8AMsgInfo>,
        @ProtoNumber(2) val appId: Int, // 1
        @ProtoNumber(3) val instId: Int, // 1
        @ProtoNumber(4) val longMessageFlag: Int, // 0
        @ProtoNumber(5) val reserved: ByteArray? = null, // struct{ boolean(1), boolean(2) }
    ) : ProtoBuf

    private fun NoticePipelineContext.processFriendRecall(body: Sub8A) {
        for (info in body.msgInfo) {
            if (info.botUin != bot.id) continue
            collected += MessageRecallEvent.FriendRecall(
                bot = bot,
                messageIds = intArrayOf(info.srcId),
                messageInternalIds = intArrayOf(info.srcInternalId.toInt()),
                messageTime = info.time.toInt(),
                operatorId = info.fromUin,
                operator = bot.getFriend(info.fromUin) ?: continue,
            )
        }
    }


    private fun NoticePipelineContext.handleInputStatusChanged(body: SubMsgType0x115.MsgBody) {
        val friend = bot.getFriend(body.fromUin) ?: return
        val item = body.msgNotifyItem ?: return
        collect(FriendInputStatusChangedEvent(friend, item.eventType == 1))
    }

    private fun NoticePipelineContext.handleProfileChanged(body: ModProfile) {
        var containsUnknown = false
        for (profileInfo in body.msgProfileInfos) {
            when (profileInfo.field) {
                20002 -> { // 昵称修改
                    val to = profileInfo.value
                    if (body.uin == bot.id) {
                        val from = bot.nick
                        if (from == to) continue
                        collect(BotNickChangedEvent(bot, from, to))
                        bot.nick = to
                    } else {
                        val friend = bot.getFriend(body.uin)?.impl() ?: continue
                        val from = friend.nick
                        if (from == to) continue
                        collect(FriendNickChangedEvent(friend, from, to))
                        friend.info.nick = to
                    }
                }

                else -> containsUnknown = true
            }
        }
        if (body.msgProfileInfos.isEmpty() || containsUnknown) {
            logger.debug { "Transformers528 0x27L: ProfileChanged new data: ${body.structureToString()}" }
        }
    }

    private fun NoticePipelineContext.handleRemarkChanged(body: ModFriendRemark) {
        for (new in body.msgFrdRmk) {
            val friend = bot.getFriend(new.fuin)?.impl() ?: continue

            collect(FriendRemarkChangeEvent(friend, friend.remark, new.rmkName))
            friend.info.remark = new.rmkName
        }
    }

    private fun NoticePipelineContext.handleAvatarChanged(body: ModCustomFace) {
        if (body.uin == bot.id) {
            collect(BotAvatarChangedEvent(bot))
        } else {
            collect(FriendAvatarChangedEvent(bot.getFriend(body.uin) ?: return))
        }
    }

    private fun NoticePipelineContext.handleFriendDeleted(body: DelFriend) {
        for (id in body.uint64Uins) {
            collect(FriendDeleteEvent(bot.removeFriend(id) ?: continue))
        }
    }

    private suspend fun NoticePipelineContext.handleFriendAddedA(
        body: Submsgtype0x44.MsgBody,
    ) = body.msgFriendMsgSync.context {
        if (this == null) return

        when (processtype) {
            3, 9, 10 -> {
                if (bot.getFriend(fuin) != null) return


                val response = bot.network.sendAndExpect(GetFriendGroupList.forSingleFriend(bot.client, fuin))
                val info = response.friendList.firstOrNull() ?: return
                collect(
                    FriendAddEvent(bot.addNewFriendAndRemoveStranger(info.toMiraiFriendInfo()) ?: return),
                )

            }
        }
    }

    private fun NoticePipelineContext.handleFriendAddedB(data: MsgType0x210, body: SubMsgType0xb3.MsgBody) =
        data.context {
            val info = FriendInfoImpl(
                uin = body.msgAddFrdNotify.fuin,
                nick = body.msgAddFrdNotify.fuinNick,
                remark = "",
                friendGroupId = 0,
            )

            val removed = bot.removeStranger(info.uin)
            val added = bot.addNewFriendAndRemoveStranger(info) ?: return
            collect(FriendAddEvent(added))
            if (removed != null) collect(StrangerRelationChangeEvent.Friended(removed, added))
        }

    private fun NoticePipelineContext.handlePrivateNudge(body: Submsgtype0x122.Submsgtype0x122.MsgBody) {
        val action = body.msgTemplParam["action_str"].orEmpty()
        val from = body.msgTemplParam["uin_str1"]?.findFriendOrStranger() ?: bot.asFriend
        val target = body.msgTemplParam["uin_str2"]?.findFriendOrStranger() ?: bot.asFriend
        val suffix = body.msgTemplParam["suffix_str"].orEmpty()

        val subject: User = bot.getFriend(msgInfo.lFromUin)
            ?: bot.getStranger(msgInfo.lFromUin)
            ?: return

        collected += NudgeEvent(
            from = if (from.id == bot.id) bot else from,
            target = if (target.id == bot.id) bot else target,
            action = action,
            suffix = suffix,
            subject = subject,
        )
    }
}