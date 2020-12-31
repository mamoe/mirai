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

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUShort
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.appId
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.message.MessageSourceFromFriendImpl
import net.mamoe.mirai.internal.message.toMessageChain
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.FrdSysMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.data.proto.SubMsgType0x7
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.GroupInfoImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.internal.network.protocol.packet.chat.toLongUnsigned
import net.mamoe.mirai.internal.network.protocol.packet.list.FriendList
import net.mamoe.mirai.internal.utils.*
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.*
import kotlin.random.Random


/**
 * 获取好友消息和消息记录
 */
internal object MessageSvcPbGetMsg : OutgoingPacketFactory<MessageSvcPbGetMsg.Response>("MessageSvc.PbGetMsg") {


    @Suppress("SpellCheckingInspection")
    operator fun invoke(
        client: QQAndroidClient,
        syncFlag: MsgSvc.SyncFlag = MsgSvc.SyncFlag.START,
        syncCookie: ByteArray?, //PbPushMsg.msg.msgHead.msgTime
    ): OutgoingPacket = buildOutgoingUniPacket(
        client
    ) {
        //println("syncCookie=${client.c2cMessageSync.syncCookie?.toUHexString()}")
        writeProtoBuf(
            MsgSvc.PbGetMsgReq.serializer(),
            MsgSvc.PbGetMsgReq(
                msgReqType = 1, // from.ctype.toInt()
                contextFlag = 1,
                rambleFlag = 0,
                latestRambleNumber = 20,
                otherRambleNumber = 3,
                onlineSyncFlag = 1,
                whisperSessionId = 0,
                syncFlag = syncFlag,
                //  serverBuf = from.serverBuf ?: EMPTY_BYTE_ARRAY,
                syncCookie = syncCookie ?: client.syncingController.syncCookie
                ?: byteArrayOf()//.also { client.c2cMessageSync.syncCookie = it },
                // syncFlag = client.c2cMessageSync.syncFlag,
                //msgCtrlBuf = client.c2cMessageSync.msgCtrlBuf,
                //pubaccountCookie = client.c2cMessageSync.pubAccountCookie
            )
        )
    }

    open class GetMsgSuccess(delegate: List<Packet>, syncCookie: ByteArray?) : Response(
        MsgSvc.SyncFlag.STOP, delegate,
        syncCookie
    ), Event,
        Packet.NoLog {
        override fun toString(): String = "MessageSvcPbGetMsg.GetMsgSuccess(messages=<Iterable>))"
    }

    /**
     * 不要直接 expect 这个 class. 它可能还没同步完成
     */
    open class Response(
        internal val syncFlagFromServer: MsgSvc.SyncFlag,
        delegate: List<Packet>,
        val syncCookie: ByteArray?
    ) :
        AbstractEvent(),
        MultiPacket<Packet>,
        Iterable<Packet> by (delegate),
        Packet.NoLog {

        override fun toString(): String =
            "MessageSvcPbGetMsg.Response(syncFlagFromServer=$syncFlagFromServer, messages=<Iterable>))"
    }

    object EmptyResponse : GetMsgSuccess(emptyList(), null)

    @OptIn(FlowPreview::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        // 00 00 01 0F 08 00 12 00 1A 34 08 FF C1 C4 F1 05 10 FF C1 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 8A CA 91 D1 0C 48 9B A5 BD 9B 0A 58 DE 9D 99 F8 08 60 1D 68 FF C1 C4 F1 05 70 00 20 02 2A 9D 01 08 F3 C1 C4 F1 05 10 A2 FF 8C F0 03 18 01 22 8A 01 0A 2A 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 A6 01 20 0B 28 AE F9 01 30 F4 C1 C4 F1 05 38 A7 E3 D8 D4 84 80 80 80 01 B8 01 CD B5 01 12 08 08 01 10 00 18 00 20 00 1A 52 0A 50 0A 27 08 00 10 F4 C1 C4 F1 05 18 A7 E3 D8 D4 04 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 08 0A 06 0A 04 4E 4D 53 4C 12 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 12 04 4A 02 08 00 30 01 2A 15 08 97 A2 C1 F1 05 10 95 A6 F5 E5 0C 18 01 30 01 40 01 48 81 01 2A 10 08 D3 F7 B5 F1 05 10 DD F1 92 B7 07 18 01 30 01 38 00 42 00 48 00
        val resp = readProtoBuf(MsgSvc.PbGetMsgResp.serializer())

        if (resp.result != 0) {
            bot.network.logger
                .warning { "MessageSvcPushNotify: result != 0, result = ${resp.result}, errorMsg=${resp.errmsg}" }
            bot.network.launch(CoroutineName("MessageSvcPushNotify.retry")) {
                delay(500 + Random.nextLong(0, 1000))
                bot.network.run {
                    MessageSvcPbGetMsg(bot.client, syncCookie = null).sendWithoutExpect()
                }
            }
            return EmptyResponse
        }
        when (resp.msgRspType) {
            0 -> {
                bot.client.syncingController.syncCookie = resp.syncCookie
                bot.client.syncingController.pubAccountCookie = resp.pubAccountCookie
            }
            1 -> {
                bot.client.syncingController.syncCookie = resp.syncCookie
            }
            2 -> {
                bot.client.syncingController.pubAccountCookie = resp.pubAccountCookie

            }
        }


//        bot.logger.debug(resp.msgRspType._miraiContentToString())
//        bot.logger.debug(resp.syncCookie._miraiContentToString())

        bot.client.syncingController.msgCtrlBuf = resp.msgCtrlBuf

        if (resp.uinPairMsgs.isEmpty()) {
            return EmptyResponse
        }

        val messages = resp.uinPairMsgs.asFlow()
            .filterNot { it.msg.isEmpty() }
            .flatMapConcat {
                it.msg.asFlow()
                    .filter { msg: MsgComm.Msg -> msg.msgHead.msgTime > it.lastReadTime.toLong() and 4294967295L }
            }.also {
                MessageSvcPbDeleteMsg.delete(bot, it) // 删除消息
            }
            .filter { msg ->
                bot.client.syncingController.pbGetMessageCacheList.addCache(
                    QQAndroidClient.MessageSvcSyncData.PbGetMessageSyncId(
                        uid = msg.msgHead.msgUid,
                        sequence = msg.msgHead.msgSeq,
                        time = msg.msgHead.msgTime
                    )
                )
            }
            .flatMapConcat { msg ->
                val result = msg.transform(bot)
                if (result == null) emptyFlow() else flowOf(result)
            }

        val list: List<Packet> = messages.toList()
        if (resp.syncFlag == MsgSvc.SyncFlag.STOP) {
            return GetMsgSuccess(list, resp.syncCookie)
        }
        return Response(resp.syncFlag, list, resp.syncCookie)
    }

    override suspend fun QQAndroidBot.handle(packet: Response) {
        when (packet.syncFlagFromServer) {
            MsgSvc.SyncFlag.STOP -> {

            }

            MsgSvc.SyncFlag.START -> {
                network.run {
                    MessageSvcPbGetMsg(
                        client,
                        MsgSvc.SyncFlag.CONTINUE,
                        bot.client.syncingController.syncCookie
                    ).sendAndExpect<Packet>()
                }
                return
            }

            MsgSvc.SyncFlag.CONTINUE -> {
                network.run {
                    MessageSvcPbGetMsg(
                        client,
                        MsgSvc.SyncFlag.CONTINUE,
                        bot.client.syncingController.syncCookie
                    ).sendAndExpect<Packet>()
                }
                return
            }
        }
    }
}

private suspend fun QQAndroidBot.createGroupForBot(groupUin: Long): Group? {
    val group = getGroupByUinOrNull(groupUin)
    if (group != null) {
        return null
    }

    return getNewGroup(Mirai.calculateGroupCodeByGroupUin(groupUin))?.apply { groups.delegate.add(this) }
}

private fun MsgComm.Msg.getNewMemberInfo(): MemberInfo {
    return MemberInfoImpl(
        nameCard = msgHead.authNick.ifEmpty { msgHead.fromNick },
        permission = MemberPermission.MEMBER,
        specialTitle = "",
        muteTimestamp = 0,
        uin = msgHead.authUin,
        nick = msgHead.authNick.ifEmpty { msgHead.fromNick },
        remark = "",
        anonymousId = null
    )
}

internal suspend fun MsgComm.Msg.transform(bot: QQAndroidBot): Packet? {
    when (msgHead.msgType) {
        33 -> bot.groupListModifyLock.withLock {

            if (msgHead.authUin == bot.id) {
                // 邀请入群
                return bot.createGroupForBot(msgHead.fromUin)?.let { group ->
                    // package: 27 0B 60 E7 01 CA CC 69 8B 83 44 71 47 90 06 B9 DC C0 ED D4 B1 00 30 33 44 30 42 38 46 30 39 37 32 38 35 43 34 31 38 30 33 36 41 34 36 31 36 31 35 32 37 38 46 46 43 30 41 38 30 36 30 36 45 38 31 43 39 41 34 38 37
                    // package: groupUin + 01 CA CC 69 8B 83 + invitorUin + length(06) + string + magicKey
                    val invitorUin = msgBody.msgContent.sliceArray(10..13).toInt().toLongUnsigned()
                    val invitor = group[invitorUin] ?: return@let null
                    BotJoinGroupEvent.Invite(invitor)
                }
            } else {

                // 成员申请入群
                val group = bot.getGroupByUinOrNull(msgHead.fromUin)
                    ?: return null

                // 主动入群, 直接加入: msgContent=27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 42 39 41 30 33 45 38 34 30 39 34 42 46 30 45 32 45 38 42 31 43 43 41 34 32 42 38 42 44 42 35 34 44 42 31 44 32 32 30 46 30 38 39 46 46 35 41 38
                // 主动直接加入                  27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 33 30 45 38 42 31 33 46 41 41 31 33 46 38 31 35 34 41 38 33 32 37 31 43 34 34 38 35 33 35 46 45 31 38 32 43 39 42 43 46 46 32 44 39 39 46 41 37

                // 有人被邀请(经过同意后)加入      27 0B 60 E7 01 76 E4 B8 DD 83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 34 30 34 38 32 33 38 35 37 41 37 38 46 33 45 37 35 38 42 39 38 46 43 45 44 43 32 41 30 31 36 36 30 34 31 36 39 35 39 30 38 39 30 39 45 31 34 34
                // 搜索到群, 直接加入             27 0B 60 E7 01 07 6E 47 BA 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 30 39 39 42 39 41 46 32 39 41 35 42 33 46 34 32 30 44 36 44 36 39 35 44 38 45 34 35 30 46 30 45 30 38 45 31 41 39 42 46 46 45 32 30 32 34 35

                // msgBody.msgContent.soutv("33类型的content")

                if (group.members.contains(msgHead.authUin)) {
                    return null
                }

                if (msgBody.msgContent.read {
                        discardExact(9)
                        readByte().toInt().and(0xff)
                    } == 0x83) {
                    return MemberJoinEvent.Invite(group.newMember(getNewMemberInfo())
                        .cast<NormalMember>()
                        .also { group.members.delegate.add(it) })
                }

                return MemberJoinEvent.Active(group.newMember(getNewMemberInfo())
                    .cast<NormalMember>()
                    .also { group.members.delegate.add(it) })
            }
        }

        34 -> { // 与 33 重复
            return null
        }

        38 -> bot.groupListModifyLock.withLock { // 建群
            return bot.createGroupForBot(msgHead.fromUin)
                ?.let { BotJoinGroupEvent.Active(it) }
        }

        85 -> bot.groupListModifyLock.withLock { // 其他客户端入群

            // msgHead.authUin: 处理人

            return if (msgHead.toUin == bot.id) {
                bot.createGroupForBot(msgHead.fromUin)
                    ?.let { BotJoinGroupEvent.Active(it) }
            } else {
                null
            }
        }

        /*
        34 -> { // 主动入群

            // 回答了问题, 还需要管理员审核
            // msgContent=27 0B 60 E7 01 76 E4 B8 DD 82 00 30 45 41 31 30 35 35 42 44 39 39 42 35 37 46 44 31 41 31 46 36 42 43 42 43 33 43 42 39 34 34 38 31 33 34 42 36 31 46 38 45 43 39 38 38 43 39 37 33
            // msgContent=27 0B 60 E7 01 76 E4 B8 DD 02 00 30 44 44 41 43 44 33 35 43 31 39 34 30 46 42 39 39 34 46 43 32 34 43 39 32 33 39 31 45 42 35 32 33 46 36 30 37 35 42 41 38 42 30 30 37 42 36 42 41
            // 回答正确问题, 直接加入

            //            27 0B 60 E7 01 76 E4 B8 DD 82 00 30 43 37 37 39 41 38 32 44 38 33 30 35 37 38 31 33 37 45 42 39 35 43 42 45 36 45 43 38 36 34 38 44 34 35 44 42 33 44 45 37 34 41 36 30 33 37 46 45
            // 提交验证消息加入, 需要审核

            // 被踢了??
            // msgContent=27 0B 60 E7 01 76 E4 B8 DD 83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 46 46 32 33 36 39 35 33 31 37 42 44 46 37 43 36 39 34 37 41 45 38 39 43 45 43 42 46 33 41 37 35 39 34 39 45 36 37 33 37 31 41 39 44 33 33 45 33

            /*
            // 搜索后直接加入群

            soutv 17:43:32 : 33类型的content = 27 0B 60 E7 01 07 6E 47 BA 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 30 39 39 42 39 41 46 32 39 41 35 42 33 46 34 32 30 44 36 44 36 39 35 44 38 45 34 35 30 46 30 45 30 38 45 31 41 39 42 46 46 45 32 30 32 34 35
            soutv 17:43:32 : 主动入群content = 2A 3D F5 69 01 35 D7 10 EA 83 4C EF 4F DD 06 B9 DC C0 ED D4 B1 00 30 37 41 39 31 39 34 31 41 30 37 46 38 32 31 39 39 43 34 35 46 39 30 36 31 43 37 39 37 33 39 35 43 34 44 36 31 33 43 31 35 42 37 32 45 46 43 43 36
             */

            val group = bot.getGroupByUinOrNull(msgHead.fromUin)
            group ?: return null

            msgBody.msgContent.soutv("主动入群content")

            if (msgBody.msgContent.read {
                    discardExact(4) // group code
                    discardExact(1) // 1
                    discardExact(4) // requester uin
                    readByte().toInt().and(0xff)
                    // 0x02: 回答正确问题直接加入
                    // 0x82: 回答了问题, 或者有验证消息, 需要管理员审核
                    // 0x83: 回答正确问题直接加入
                } != 0x82) {

                if (group.members.contains(msgHead.authUin)) {
                    return null
                }
                @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                return MemberJoinEvent.Active(group.newMember(getNewMemberInfo())
                    .also { group.members.delegate.addLast(it) })
            } else return null
        }
        */

        //167 单向好友
        166, 167 -> {
            if (msgHead.fromUin == bot.id) {
                loop@ while (true) {
                    val instance = bot.client.getFriendSeq()
                    if (instance < msgHead.msgSeq) {
                        if (bot.client.setFriendSeq(instance, msgHead.msgSeq)) {
                            break@loop
                        }
                    } else break@loop
                }
                return null
            }
            if (!bot.firstLoginSucceed) {
                return null
            }
            bot.getFriend(msgHead.fromUin)?.let { friend ->
                friend.checkIsFriendImpl()
                friend.lastMessageSequence.loop {
                    return if (friend.lastMessageSequence.compareAndSet(
                            it,
                            msgHead.msgSeq
                        ) && contentHead?.autoReply != 1
                    ) {
                        FriendMessageEvent(
                            friend,
                            toMessageChain(bot, groupIdOrZero = 0, onlineSource = true, MessageSourceKind.FRIEND),
                            msgHead.msgTime
                        )
                    } else null
                }
            } ?: bot.getStranger(msgHead.fromUin)?.let { stranger ->
                stranger.checkIsImpl()
                stranger.lastMessageSequence.loop {
                    return if (stranger.lastMessageSequence.compareAndSet(
                            it,
                            msgHead.msgSeq
                        ) && contentHead?.autoReply != 1
                    ) {
                        StrangerMessageEvent(
                            stranger,
                            toMessageChain(bot, groupIdOrZero = 0, onlineSource = true, MessageSourceKind.STRANGER),
                            msgHead.msgTime
                        )
                    } else null
                }
            } ?: return null
        }
        208 -> {
            // friend ptt
            return null
        }
        529 -> {

            // top_package/awbk.java:3765

            return when (msgHead.c2cCmd) {
                // other client sync
                7 -> {
                    val data = msgBody.msgContent.loadAs(SubMsgType0x7.MsgBody.serializer())

                    val textMsg =
                        data.msgSubcmd0x4Generic?.buf?.loadAs(SubMsgType0x7.MsgBody.QQDataTextMsg.serializer())
                            ?: return null

                    with(data.msgHeader ?: return null) {
                        if (srcUin != dstUin || dstUin != bot.id) return null
                        val client = bot.otherClients.find { it.appId == srcInstId }
                            ?: return null// don't compare with dstAppId. diff.

                        val chain = buildMessageChain {
                            +MessageSourceFromFriendImpl(bot, listOf(this@transform))
                            for (msgItem in textMsg.msgItems) {
                                when (msgItem.type) {
                                    1 -> +PlainText(msgItem.text)
                                    else -> {
                                    }
                                }
                            }
                        }

                        return OtherClientMessageEvent(client, chain, msgHead.msgTime)
                    }
                }

                else -> null
            }

            // 各种垃圾
            // 08 04 12 1E 08 E9 07 10 B7 F7 8B 80 02 18 E9 07 20 00 28 DD F1 92 B7 07 30 DD F1 92 B7 07 48 02 50 03 32 1E 08 88 80 F8 92 CD 84 80 80 10 10 01 18 00 20 01 2A 0C 0A 0A 08 01 12 06 E5 95 8A E5 95 8A
        }
        141 -> {
            val tmpHead = msgHead.c2cTmpMsgHead ?: return null
            val member = bot.getGroupByUinOrNull(tmpHead.groupUin)?.get(msgHead.fromUin)
                ?: return null

            member.checkIsMemberImpl()

            if (msgHead.fromUin == bot.id || !bot.firstLoginSucceed) {
                return null
            }

            member.lastMessageSequence.loop { instant ->
                if (msgHead.msgSeq > instant) {
                    if (member.lastMessageSequence.compareAndSet(instant, msgHead.msgSeq)) {
                        return TempMessageEvent(
                            member,
                            toMessageChain(
                                bot,
                                groupIdOrZero = 0,
                                onlineSource = true,
                                MessageSourceKind.TEMP
                            ),
                            msgHead.msgTime
                        )
                    }
                } else return null
            }
        }
        84, 87 -> { // 请求入群验证 和 被要求入群
            bot.network.run {
                NewContact.SystemMsgNewGroup(bot.client).sendWithoutExpect()
            }
            return null
        }
        187 -> { // 请求加好友验证
            bot.network.run {
                NewContact.SystemMsgNewFriend(bot.client).sendWithoutExpect()
            }
            return null
        }
        732 -> {
            // unknown
            // 前 4 byte 是群号
            return null
        }
        //陌生人添加信息
        191 -> {
            var fromGroup = 0L
            var pbNick = ""
            msgBody.msgContent.read {
                readUByte()// version
                discardExact(readUByte().toInt())//skip
                readUShort()//source id
                readUShort()//SourceSubID
                discardExact(readUShort().toLong())//skip size
                if (readUShort().toInt() != 0) {//hasExtraInfo
                    discardExact(readUShort().toInt())//mail address info, skip
                }
                discardExact(4 + readUShort().toInt())//skip
                for (i in 1..readUByte().toInt()) {//pb size
                    val type = readUShort().toInt()
                    val pbArray = ByteArray(readUShort().toInt() and 0xFF)
                    readAvailable(pbArray)
                    when (type) {
                        1000 -> pbArray.loadAs(FrdSysMsg.GroupInfo.serializer()).let { fromGroup = it.groupUin }
                        1002 -> pbArray.loadAs(FrdSysMsg.FriendMiscInfo.serializer()).let { pbNick = it.fromuinNick }
                        else -> {
                        }//ignore
                    }
                }
            }
            val nick = sequenceOf(msgHead.fromNick, msgHead.authNick, pbNick).filter { it.isNotEmpty() }.firstOrNull()
                ?: return null
            val id = sequenceOf(msgHead.fromUin, msgHead.authUin).filter { it != 0L }.firstOrNull() ?: return null//对方QQ
            Mirai._lowLevelNewStranger(bot, StrangerInfoImpl(id, nick, fromGroup)).let {
                bot.getStranger(id)?.let { previous ->
                    bot.strangers.remove(id)
                    StrangerRelationChangeEvent.Deleted(previous).broadcast()
                }
                bot.strangers.delegate.add(it)

                return StrangerAddEvent(it)
            }
        }
        // 732:  27 0B 60 E7 0C 01 3E 03 3F A2 5E 90 60 E2 00 01 44 71 47 90 00 00 02 58
        // 732:  27 0B 60 E7 11 00 40 08 07 20 E7 C1 AD B8 02 5A 36 08 B4 E7 E0 F0 09 1A 1A 08 9C D4 16 10 F7 D2 D8 F5 05 18 D0 E2 85 F4 06 20 00 28 00 30 B4 E7 E0 F0 09 2A 0E 08 00 12 0A 08 9C D4 16 10 00 18 01 20 00 30 00 38 00
        // 732:  27 0B 60 E7 11 00 33 08 07 20 E7 C1 AD B8 02 5A 29 08 EE 97 85 E9 01 1A 19 08 EE D6 16 10 FF F2 D8 F5 05 18 E9 E7 A3 05 20 00 28 00 30 EE 97 85 E9 01 2A 02 08 00 30 00 38 00
        else -> {
            bot.network.logger.debug { "unknown PbGetMsg type ${msgHead.msgType}, data=${msgBody.msgContent.toUHexString()}" }
            return null
        }
    }
}

// kotlin bug, don't remove
private inline fun kotlinx.atomicfu.AtomicInt.loop(action: (Int) -> Unit): Nothing {
    while (true) {
        action(value)
    }
}


internal suspend fun QQAndroidBot.getNewGroup(groupCode: Long): Group? {
    val troopNum = network.run {
        FriendList.GetTroopListSimplify(client)
            .sendAndExpect<FriendList.GetTroopListSimplify.Response>(timeoutMillis = 10_000, retry = 5)
    }.groups.firstOrNull { it.groupCode == groupCode } ?: return null

    return GroupImpl(
        bot = this,
        coroutineContext = coroutineContext,
        id = groupCode,
        groupInfo = GroupInfoImpl(troopNum),
        members = Mirai._lowLevelQueryGroupMemberList(
            this,
            troopNum.groupUin,
            troopNum.groupCode,
            troopNum.dwGroupOwnerUin
        )
    )
}
