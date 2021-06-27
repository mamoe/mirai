/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUShort
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.toMiraiFriendInfo
import net.mamoe.mirai.internal.network.components.MixedNoticeProcessor
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgType0x210
import net.mamoe.mirai.internal.network.protocol.data.proto.FrdSysMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x115.SubMsgType0x115
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x27.SubMsgType0x27.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0x44.Submsgtype0x44
import net.mamoe.mirai.internal.network.protocol.data.proto.Submsgtype0xb3.SubMsgType0xb3
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList.GetFriendGroupList
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.internal.utils._miraiContentToString
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
    override suspend fun PipelineContext.processImpl(data: MsgComm.Msg) = data.context {
        if (msgHead.msgType != 191) return

        var fromGroup = 0L
        var pbNick = ""
        msgBody.msgContent.read {
            readUByte() // version
            discardExact(readUByte().toInt()) //skip
            readUShort() //source id
            readUShort() //SourceSubID
            discardExact(readUShort().toLong()) //skip size
            if (readUShort().toInt() != 0) { //hasExtraInfo
                discardExact(readUShort().toInt()) //mail address info, skip
            }
            discardExact(4 + readUShort().toInt()) //skip
            for (i in 1..readUByte().toInt()) { //pb size
                val type = readUShort().toInt()
                val pbArray = ByteArray(readUShort().toInt() and 0xFF)
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
            if (fromUin == authUin) {
                logger.error { "Could not determine uin since `fromUin` = `authUin` = $fromUin" }
                return
            }
            val id = fromUin or authUin // 对方 qq
            if (bot.getStranger(id) != null) return

            val nick = fromNick.ifEmpty { authNick }.ifEmpty { pbNick }
            collect(StrangerAddEvent(bot.addNewStranger(StrangerInfoImpl(id, nick, fromGroup)) ?: return))
        }

    }

    override suspend fun PipelineContext.processImpl(data: MsgType0x210) = data.context {
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
            else -> return
        }
        markAsConsumed()
    }

    private fun PipelineContext.handleInputStatusChanged(body: SubMsgType0x115.MsgBody) {
        val friend = bot.getFriend(body.fromUin) ?: return
        val item = body.msgNotifyItem ?: return
        collect(FriendInputStatusChangedEvent(friend, item.eventType == 1))
    }

    private fun PipelineContext.handleProfileChanged(body: ModProfile) {
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
                        val from = bot.nick
                        if (from == to) continue
                        collect(FriendNickChangedEvent(friend, from, to))
                        friend.info.nick = to
                    }
                }
                else -> containsUnknown = true
            }
        }
        if (body.msgProfileInfos.isEmpty() || containsUnknown) {
            logger.debug { "Transformers528 0x27L: ProfileChanged new data: ${body._miraiContentToString()}" }
        }
    }

    private fun PipelineContext.handleRemarkChanged(body: ModFriendRemark) {
        for (new in body.msgFrdRmk) {
            val friend = bot.getFriend(new.fuin)?.impl() ?: continue

            // TODO: 2020/4/10 ADD REMARK QUERY
            collect(FriendRemarkChangeEvent(friend, friend.remark, new.rmkName))
            friend.info.remark = new.rmkName
        }
    }

    private fun PipelineContext.handleAvatarChanged(body: ModCustomFace) {
        if (body.uin == bot.id) {
            collect(BotAvatarChangedEvent(bot))
        }
        collect(FriendAvatarChangedEvent(bot.getFriend(body.uin) ?: return))
    }

    private fun PipelineContext.handleFriendDeleted(body: DelFriend) {
        for (id in body.uint64Uins) {
            collect(FriendDeleteEvent(bot.removeFriend(id) ?: continue))
        }
    }

    private suspend fun PipelineContext.handleFriendAddedA(
        body: Submsgtype0x44.MsgBody,
    ) = body.msgFriendMsgSync.context {
        if (this == null) return

        when (processtype) {
            3, 9, 10 -> {
                if (bot.getFriend(fuin) != null) return

                val response = GetFriendGroupList.forSingleFriend(bot.client, fuin).sendAndExpect(bot)
                val info = response.friendList.firstOrNull() ?: return
                collect(
                    FriendAddEvent(bot.addNewFriendAndRemoveStranger(info.toMiraiFriendInfo()) ?: return),
                )

            }
        }
    }

    private fun PipelineContext.handleFriendAddedB(data: MsgType0x210, body: SubMsgType0xb3.MsgBody) = data.context {
        val info = FriendInfoImpl(
            uin = body.msgAddFrdNotify.fuin,
            nick = body.msgAddFrdNotify.fuinNick,
            remark = "",
        )

        val removed = bot.removeStranger(info.uin)
        val added = bot.addNewFriendAndRemoveStranger(info) ?: return
        collect(FriendAddEvent(added))
        if (removed != null) collect(StrangerRelationChangeEvent.Friended(removed, added))
    }
}