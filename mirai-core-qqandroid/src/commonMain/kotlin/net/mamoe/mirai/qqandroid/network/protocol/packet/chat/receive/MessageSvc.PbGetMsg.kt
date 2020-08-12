/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.atomicfu.loop
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotJoinGroupEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.TempMessageEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.contact.GroupImpl
import net.mamoe.mirai.qqandroid.contact.checkIsFriendImpl
import net.mamoe.mirai.qqandroid.contact.checkIsMemberImpl
import net.mamoe.mirai.qqandroid.message.toMessageChain
import net.mamoe.mirai.qqandroid.network.MultiPacket
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SyncCookie
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.GroupInfoImpl
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.NewContact
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList
import net.mamoe.mirai.qqandroid.utils._miraiContentToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.utils.read
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.warning


/**
 * 获取好友消息和消息记录
 */
internal object MessageSvcPbGetMsg : OutgoingPacketFactory<MessageSvcPbGetMsg.Response>("MessageSvc.PbGetMsg") {
    @Suppress("SpellCheckingInspection")
    operator fun invoke(
        client: QQAndroidClient,
        syncFlag: MsgSvc.SyncFlag = MsgSvc.SyncFlag.START,
        msgTime: Long //PbPushMsg.msg.msgHead.msgTime
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
                syncCookie = client.c2cMessageSync.syncCookie
                    ?: SyncCookie(time = msgTime).toByteArray(SyncCookie.serializer())//.also { client.c2cMessageSync.syncCookie = it },
                // syncFlag = client.c2cMessageSync.syncFlag,
                //msgCtrlBuf = client.c2cMessageSync.msgCtrlBuf,
                //pubaccountCookie = client.c2cMessageSync.pubAccountCookie
            )
        )
    }

    open class GetMsgSuccess(delegate: List<Packet>) : Response(MsgSvc.SyncFlag.STOP, delegate), Event,
        Packet.NoLog {
        override fun toString(): String = "MessageSvcPbGetMsg.GetMsgSuccess(messages=<Iterable>))"
    }

    /**
     * 不要直接 expect 这个 class. 它可能还没同步完成
     */
    open class Response(internal val syncFlagFromServer: MsgSvc.SyncFlag, delegate: List<Packet>) :
        AbstractEvent(),
        MultiPacket<Packet>,
        Iterable<Packet> by (delegate) {

        override fun toString(): String =
            "MessageSvcPbGetMsg.Response(syncFlagFromServer=$syncFlagFromServer, messages=<Iterable>))"
    }

    object EmptyResponse : GetMsgSuccess(emptyList())

    private fun MsgComm.Msg.getNewMemberInfo(): MemberInfo {
        return object : MemberInfo {
            override val nameCard: String get() = ""
            override val permission: MemberPermission get() = MemberPermission.MEMBER
            override val specialTitle: String get() = ""
            override val muteTimestamp: Int get() = 0
            override val uin: Long get() = msgHead.authUin
            override val nick: String = msgHead.authNick.takeIf { it.isNotEmpty() }
                ?: msgHead.fromNick
        }
    }

    @OptIn(FlowPreview::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        // 00 00 01 0F 08 00 12 00 1A 34 08 FF C1 C4 F1 05 10 FF C1 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 8A CA 91 D1 0C 48 9B A5 BD 9B 0A 58 DE 9D 99 F8 08 60 1D 68 FF C1 C4 F1 05 70 00 20 02 2A 9D 01 08 F3 C1 C4 F1 05 10 A2 FF 8C F0 03 18 01 22 8A 01 0A 2A 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 A6 01 20 0B 28 AE F9 01 30 F4 C1 C4 F1 05 38 A7 E3 D8 D4 84 80 80 80 01 B8 01 CD B5 01 12 08 08 01 10 00 18 00 20 00 1A 52 0A 50 0A 27 08 00 10 F4 C1 C4 F1 05 18 A7 E3 D8 D4 04 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 08 0A 06 0A 04 4E 4D 53 4C 12 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 12 04 4A 02 08 00 30 01 2A 15 08 97 A2 C1 F1 05 10 95 A6 F5 E5 0C 18 01 30 01 40 01 48 81 01 2A 10 08 D3 F7 B5 F1 05 10 DD F1 92 B7 07 18 01 30 01 38 00 42 00 48 00
        val resp = readProtoBuf(MsgSvc.PbGetMsgResp.serializer())

        if (resp.result != 0) {
            bot.network.logger
                .warning { "MessageSvcPushNotify: result != 0, result = ${resp.result}, errorMsg=${resp.errmsg}" }
            return EmptyResponse
        }

        bot.client.c2cMessageSync.syncCookie = resp.syncCookie
        bot.client.c2cMessageSync.pubAccountCookie = resp.pubAccountCookie
        bot.client.c2cMessageSync.msgCtrlBuf = resp.msgCtrlBuf

        if (resp.uinPairMsgs == null) {
            return EmptyResponse
        }

        val messages = resp.uinPairMsgs.asFlow()
            .filterNot { it.msg == null }
            .flatMapConcat { it.msg!!.asFlow() }
            .also {
                MessageSvcPbDeleteMsg.delete(bot, it)
            } // 删除消息
            .mapNotNull<MsgComm.Msg, Packet> { msg ->

                when (msg.msgHead.msgType) {
                    33 -> bot.groupListModifyLock.withLock { // 邀请入群
                        val group = bot.getGroupByUinOrNull(msg.msgHead.fromUin)
                        if (msg.msgHead.authUin == bot.id) {
                            if (group != null) {
                                return@mapNotNull null
                            }
                            // 新群

                            val newGroup = bot.getNewGroup(Group.calculateGroupCodeByGroupUin(msg.msgHead.fromUin))
                                ?: return@mapNotNull null
                            bot.groups.delegate.addLast(newGroup)
                            return@mapNotNull BotJoinGroupEvent.Active(newGroup)
                        } else {
                            group ?: return@mapNotNull null

                            // 主动入群, 直接加入: msgContent=27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 42 39 41 30 33 45 38 34 30 39 34 42 46 30 45 32 45 38 42 31 43 43 41 34 32 42 38 42 44 42 35 34 44 42 31 44 32 32 30 46 30 38 39 46 46 35 41 38
                            // 主动直接加入                  27 0B 60 E7 01 76 E4 B8 DD 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 33 30 45 38 42 31 33 46 41 41 31 33 46 38 31 35 34 41 38 33 32 37 31 43 34 34 38 35 33 35 46 45 31 38 32 43 39 42 43 46 46 32 44 39 39 46 41 37

                            // 有人被邀请(经过同意后)加入      27 0B 60 E7 01 76 E4 B8 DD 83 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 34 30 34 38 32 33 38 35 37 41 37 38 46 33 45 37 35 38 42 39 38 46 43 45 44 43 32 41 30 31 36 36 30 34 31 36 39 35 39 30 38 39 30 39 45 31 34 34
                            // 搜索到群, 直接加入             27 0B 60 E7 01 07 6E 47 BA 82 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 30 39 39 42 39 41 46 32 39 41 35 42 33 46 34 32 30 44 36 44 36 39 35 44 38 45 34 35 30 46 30 45 30 38 45 31 41 39 42 46 46 45 32 30 32 34 35

                            // msg.msgBody.msgContent.soutv("33类型的content")

                            if (group.members.contains(msg.msgHead.authUin)) {
                                return@mapNotNull null
                            }

                            if (msg.msgBody.msgContent.read {
                                    discardExact(9)
                                    readByte().toInt().and(0xff)
                                } == 0x83) {
                                return@mapNotNull MemberJoinEvent.Invite(group.newMember(msg.getNewMemberInfo())
                                    .also { group.members.delegate.addLast(it) })
                            }

                            return@mapNotNull MemberJoinEvent.Active(group.newMember(msg.getNewMemberInfo())
                                .also { group.members.delegate.addLast(it) })
                        }
                    }

                    34 -> { // 与 33 重复
                        return@mapNotNull null
                    }

                    85 -> bot.groupListModifyLock.withLock { // 其他客户端入群
                        val group = bot.getGroupByUinOrNull(msg.msgHead.fromUin)
                        if (msg.msgHead.toUin == bot.id && group == null) {

                            val newGroup = bot.getNewGroup(Group.calculateGroupCodeByGroupUin(msg.msgHead.fromUin))
                                ?: return@mapNotNull null
                            bot.groups.delegate.addLast(newGroup)
                            return@mapNotNull BotJoinGroupEvent.Active(newGroup)
                        } else {
                            // unknown
                            return@mapNotNull null
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

                        val group = bot.getGroupByUinOrNull(msg.msgHead.fromUin)
                        group ?: return@mapNotNull null

                        msg.msgBody.msgContent.soutv("主动入群content")

                        if (msg.msgBody.msgContent.read {
                                discardExact(4) // group code
                                discardExact(1) // 1
                                discardExact(4) // requester uin
                                readByte().toInt().and(0xff)
                                // 0x02: 回答正确问题直接加入
                                // 0x82: 回答了问题, 或者有验证消息, 需要管理员审核
                                // 0x83: 回答正确问题直接加入
                            } != 0x82) {

                            if (group.members.contains(msg.msgHead.authUin)) {
                                return@mapNotNull null
                            }
                            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                            return@mapNotNull MemberJoinEvent.Active(group.newMember(msg.getNewMemberInfo())
                                .also { group.members.delegate.addLast(it) })
                        } else return@mapNotNull null
                    }
                    */

                    166 -> {

                        if (msg.msgHead.fromUin == bot.id) {
                            loop@ while (true) {
                                val instance = bot.client.getFriendSeq()
                                if (instance < msg.msgHead.msgSeq) {
                                    if (bot.client.setFriendSeq(instance, msg.msgHead.msgSeq)) {
                                        break@loop
                                    }
                                } else break@loop
                            }
                            return@mapNotNull null
                        }
                        val friend = bot.getFriendOrNull(msg.msgHead.fromUin) ?: return@mapNotNull null
                        friend.checkIsFriendImpl()

                        if (!bot.firstLoginSucceed) {
                            return@mapNotNull null
                        }

                        friend.lastMessageSequence.loop { instant ->
                            if (msg.msgHead.msgSeq > instant) {
                                if (friend.lastMessageSequence.compareAndSet(instant, msg.msgHead.msgSeq)) {
                                    return@mapNotNull FriendMessageEvent(
                                        friend,
                                        msg.toMessageChain(bot, groupIdOrZero = 0, onlineSource = true),
                                        msg.msgHead.msgTime
                                    )
                                }
                            } else return@mapNotNull null
                        }
                    }
                    208 -> {
                        // friend ptt
                        return@mapNotNull null
                    }
                    529 -> {
                        // 好友文件
                        return@mapNotNull null
                    }
                    141 -> {
                        val tmpHead = msg.msgHead.c2cTmpMsgHead ?: return@mapNotNull null
                        val member = bot.getGroupByUinOrNull(tmpHead.groupUin)?.getOrNull(msg.msgHead.fromUin)
                            ?: return@mapNotNull null

                        member.checkIsMemberImpl()

                        if (msg.msgHead.fromUin == bot.id || !bot.firstLoginSucceed) {
                            return@mapNotNull null
                        }

                        member.lastMessageSequence.loop { instant ->
                            if (msg.msgHead.msgSeq > instant) {
                                if (member.lastMessageSequence.compareAndSet(instant, msg.msgHead.msgSeq)) {
                                    return@mapNotNull TempMessageEvent(
                                        member,
                                        msg.toMessageChain(
                                            bot,
                                            groupIdOrZero = 0,
                                            onlineSource = true,
                                            isTemp = true
                                        ),
                                        msg.msgHead.msgTime
                                    )
                                }
                            } else return@mapNotNull null
                        }
                    }
                    84, 87 -> { // 请求入群验证 和 被要求入群
                        bot.network.run {
                            NewContact.SystemMsgNewGroup(bot.client).sendWithoutExpect()
                        }
                        return@mapNotNull null
                    }
                    187 -> { // 请求加好友验证
                        bot.network.run {
                            NewContact.SystemMsgNewFriend(bot.client).sendWithoutExpect()
                        }
                        return@mapNotNull null
                    }

                    732 -> {
                        // unknown
                        return@mapNotNull null
                    }
                    // 732:  27 0B 60 E7 0C 01 3E 03 3F A2 5E 90 60 E2 00 01 44 71 47 90 00 00 02 58
                    // 732:  27 0B 60 E7 11 00 40 08 07 20 E7 C1 AD B8 02 5A 36 08 B4 E7 E0 F0 09 1A 1A 08 9C D4 16 10 F7 D2 D8 F5 05 18 D0 E2 85 F4 06 20 00 28 00 30 B4 E7 E0 F0 09 2A 0E 08 00 12 0A 08 9C D4 16 10 00 18 01 20 00 30 00 38 00
                    // 732:  27 0B 60 E7 11 00 33 08 07 20 E7 C1 AD B8 02 5A 29 08 EE 97 85 E9 01 1A 19 08 EE D6 16 10 FF F2 D8 F5 05 18 E9 E7 A3 05 20 00 28 00 30 EE 97 85 E9 01 2A 02 08 00 30 00 38 00
                    else -> {
                        bot.network.logger.debug { "unknown PbGetMsg type ${msg.msgHead.msgType}, data=${msg.msgBody.msgContent.toUHexString()}" }
                        return@mapNotNull null
                    }
                }
            }

        val list: List<Packet> = messages.toList()
        if (resp.syncFlag == MsgSvc.SyncFlag.STOP) {
            return GetMsgSuccess(list)
        }
        return Response(resp.syncFlag, list)
    }

    override suspend fun QQAndroidBot.handle(packet: Response) {
        when (packet.syncFlagFromServer) {
            MsgSvc.SyncFlag.STOP -> return
            MsgSvc.SyncFlag.START -> {
                network.run {
                    MessageSvcPbGetMsg(client, MsgSvc.SyncFlag.CONTINUE, currentTimeSeconds).sendAndExpect<Packet>()
                }
                return
            }

            MsgSvc.SyncFlag.CONTINUE -> {
                network.run {
                    MessageSvcPbGetMsg(client, MsgSvc.SyncFlag.CONTINUE, currentTimeSeconds).sendAndExpect<Packet>()
                }
                return
            }
        }
    }
}


internal suspend fun QQAndroidBot.getNewGroup(groupCode: Long): Group? {
    val troopNum = network.run {
        FriendList.GetTroopListSimplify(client)
            .sendAndExpect<FriendList.GetTroopListSimplify.Response>(timeoutMillis = 10_000, retry = 5)
    }.groups.firstOrNull { it.groupCode == groupCode } ?: return null

    @Suppress("DuplicatedCode")
    return GroupImpl(
        bot = this,
        coroutineContext = coroutineContext,
        id = groupCode,
        groupInfo = _lowLevelQueryGroupInfo(troopNum.groupCode).apply {
            this as GroupInfoImpl

            if (this.delegate.groupName == null) {
                this.delegate.groupName = troopNum.groupName
            }

            if (this.delegate.groupMemo == null) {
                this.delegate.groupMemo = troopNum.groupMemo
            }

            if (this.delegate.groupUin == null) {
                this.delegate.groupUin = troopNum.groupUin
            }

            this.delegate.groupCode = troopNum.groupCode
        },
        members = _lowLevelQueryGroupMemberList(
            troopNum.groupUin,
            troopNum.groupCode,
            troopNum.dwGroupOwnerUin
        )
    )
}
