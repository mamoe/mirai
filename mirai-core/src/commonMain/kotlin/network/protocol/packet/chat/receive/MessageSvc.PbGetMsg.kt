/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.MultiPacket
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.noticeProcessorPipeline
import net.mamoe.mirai.internal.network.notice.SystemMessageProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
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
    ) = buildOutgoingUniPacket(client) {
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

    open class GetMsgSuccess(delegate: List<Packet>, syncCookie: ByteArray?, bot: QQAndroidBot) :
        Response(MsgSvc.SyncFlag.STOP, delegate, syncCookie, bot) {

        override fun toString(): String = "MessageSvcPbGetMsg.GetMsgSuccess"
    }

    /**
     * 不要直接 expect 这个 class. 它可能还没同步完成
     */
    open class Response(
        internal val syncFlagFromServer: MsgSvc.SyncFlag,
        delegate: List<Packet>,
        val syncCookie: ByteArray?, override val bot: Bot
    ) : AbstractEvent(),
        MultiPacket,
        Collection<Packet> by delegate,
        Packet.NoEventLog,
        BotEvent {

        override fun toString(): String =
            "MessageSvcPbGetMsg.Response(flag=$syncFlagFromServer)"
    }

    class EmptyResponse(
        bot: QQAndroidBot
    ) : GetMsgSuccess(emptyList(), null, bot)

    @OptIn(FlowPreview::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        // 00 00 01 0F 08 00 12 00 1A 34 08 FF C1 C4 F1 05 10 FF C1 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 8A CA 91 D1 0C 48 9B A5 BD 9B 0A 58 DE 9D 99 F8 08 60 1D 68 FF C1 C4 F1 05 70 00 20 02 2A 9D 01 08 F3 C1 C4 F1 05 10 A2 FF 8C F0 03 18 01 22 8A 01 0A 2A 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 A6 01 20 0B 28 AE F9 01 30 F4 C1 C4 F1 05 38 A7 E3 D8 D4 84 80 80 80 01 B8 01 CD B5 01 12 08 08 01 10 00 18 00 20 00 1A 52 0A 50 0A 27 08 00 10 F4 C1 C4 F1 05 18 A7 E3 D8 D4 04 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 08 0A 06 0A 04 4E 4D 53 4C 12 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 12 04 4A 02 08 00 30 01 2A 15 08 97 A2 C1 F1 05 10 95 A6 F5 E5 0C 18 01 30 01 40 01 48 81 01 2A 10 08 D3 F7 B5 F1 05 10 DD F1 92 B7 07 18 01 30 01 38 00 42 00 48 00
        val resp = readProtoBuf(MsgSvc.PbGetMsgResp.serializer())

        if (resp.result != 0) {
            // this is normally recoverable, no need to log


//            bot.network.logger
//                .warning { "MessageSvcPushNotify: result != 0, result = ${resp.result}, errorMsg=${resp.errmsg}" }
            bot.network.launch(CoroutineName("MessageSvcPushNotify.retry")) {
                delay(500 + Random.nextLong(0, 1000))
                bot.network.run {
                    MessageSvcPbGetMsg(bot.client, syncCookie = null).sendWithoutExpect()
                }
            }
            return EmptyResponse(bot)
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
            return EmptyResponse(bot)
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
                bot.components.noticeProcessorPipeline
                    .process(bot, msg, SystemMessageProcessor.KEY_FROM_SYNC to false)
                    .asFlow()
            }

        val list: List<Packet> = messages.toList()
        if (resp.syncFlag == MsgSvc.SyncFlag.STOP) {
            return GetMsgSuccess(list, resp.syncCookie, bot)
        }
        return Response(resp.syncFlag, list, resp.syncCookie, bot)
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
                    ).sendAndExpect()
                }
                return
            }

            MsgSvc.SyncFlag.CONTINUE -> {
                network.run {
                    MessageSvcPbGetMsg(
                        client,
                        MsgSvc.SyncFlag.CONTINUE,
                        bot.client.syncingController.syncCookie
                    ).sendAndExpect()
                }
                return
            }
        }
    }
}
