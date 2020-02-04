package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.data.MultiPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.event.ForceOfflineEvent
import net.mamoe.mirai.qqandroid.io.serialization.decodeUniPacket
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPushNotify
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.SyncCookie
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.toMessageChain
import net.mamoe.mirai.qqandroid.utils.toRichTextElems
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class MessageSvc {
    /**
     * 告知要刷新好友消息
     */
    internal object PushNotify : IncomingPacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): RequestPushNotify {
            discardExact(4) // don't remove

            return decodeUniPacket(RequestPushNotify.serializer())
        }

        override suspend fun QQAndroidBot.handle(packet: RequestPushNotify, sequenceId: Int): OutgoingPacket? {
            network.run {
                return PbGetMsg(client, MsgSvc.SyncFlag.START, packet.stMsgInfo?.uMsgTime ?: currentTimeSeconds)
            }
        }
    }


    /**
     * 获取好友消息和消息记录
     */
    @UseExperimental(MiraiInternalAPI::class)
    internal object PbGetMsg : OutgoingPacketFactory<PbGetMsg.Response>("MessageSvc.PbGetMsg") {
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
                        ?: SyncCookie(time = msgTime + client.timeDifference).toByteArray(SyncCookie.serializer())//.also { client.c2cMessageSync.syncCookie = it },
                    // syncFlag = client.c2cMessageSync.syncFlag,
                    //msgCtrlBuf = client.c2cMessageSync.msgCtrlBuf,
                    //pubaccountCookie = client.c2cMessageSync.pubAccountCookie
                )
            )
        }

        @UseExperimental(MiraiInternalAPI::class)
        internal class GetMsgSuccess(delegate: MutableList<FriendMessage>) : Response(MsgSvc.SyncFlag.STOP, delegate)

        /**
         * 不要直接 expect 这个 class. 它可能
         */
        @MiraiInternalAPI
        open class Response(internal val syncFlagFromServer: MsgSvc.SyncFlag, delegate: MutableList<FriendMessage>) : MultiPacket<FriendMessage>(delegate),
            BroadcastControllable {
            override val shouldBroadcast: Boolean
                get() = syncFlagFromServer == MsgSvc.SyncFlag.STOP

            override fun toString(): String {
                return "MessageSvc.PbGetMsg.Response($syncFlagFromServer=$syncFlagFromServer, messages=List(size=${this.size}))"
            }
        }

        @UseExperimental(MiraiInternalAPI::class)
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            // 00 00 01 0F 08 00 12 00 1A 34 08 FF C1 C4 F1 05 10 FF C1 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 8A CA 91 D1 0C 48 9B A5 BD 9B 0A 58 DE 9D 99 F8 08 60 1D 68 FF C1 C4 F1 05 70 00 20 02 2A 9D 01 08 F3 C1 C4 F1 05 10 A2 FF 8C F0 03 18 01 22 8A 01 0A 2A 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 18 A6 01 20 0B 28 AE F9 01 30 F4 C1 C4 F1 05 38 A7 E3 D8 D4 84 80 80 80 01 B8 01 CD B5 01 12 08 08 01 10 00 18 00 20 00 1A 52 0A 50 0A 27 08 00 10 F4 C1 C4 F1 05 18 A7 E3 D8 D4 04 20 00 28 0C 30 00 38 86 01 40 22 4A 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91 12 08 0A 06 0A 04 4E 4D 53 4C 12 15 AA 02 12 9A 01 0F 80 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 12 04 4A 02 08 00 30 01 2A 15 08 97 A2 C1 F1 05 10 95 A6 F5 E5 0C 18 01 30 01 40 01 48 81 01 2A 10 08 D3 F7 B5 F1 05 10 DD F1 92 B7 07 18 01 30 01 38 00 42 00 48 00
            val resp = readProtoBuf(MsgSvc.PbGetMsgResp.serializer())

            if (resp.result != 0) {
                println("!!! Result=${resp.result} !!!: " + resp.contentToString())
                return GetMsgSuccess(mutableListOf())
            }

            bot.client.c2cMessageSync.syncCookie = resp.syncCookie
            bot.client.c2cMessageSync.pubAccountCookie = resp.pubAccountCookie
            bot.client.c2cMessageSync.msgCtrlBuf = resp.msgCtrlBuf

            if (resp.uinPairMsgs == null) {
                return GetMsgSuccess(mutableListOf())
            }

            val messages = resp.uinPairMsgs.asSequence().filterNot { it.msg == null }.flatMap { it.msg!!.asSequence() }.mapNotNull {
                when (it.msgHead.msgType) {
                    166 -> FriendMessage(
                        bot,
                        bot.getFriend(it.msgHead.fromUin),
                        it.msgBody.richText.toMessageChain()
                    )
                    else -> null
                }
            }.toMutableList()
            if (resp.syncFlag == MsgSvc.SyncFlag.STOP) {
                messages.ifEmpty {
                    return GetMsgSuccess(messages)
                }
                return GetMsgSuccess(mutableListOf(messages.last()))
            }
            return Response(resp.syncFlag, messages)
        }

        override suspend fun QQAndroidBot.handle(packet: Response) {
            when (packet.syncFlagFromServer) {
                MsgSvc.SyncFlag.STOP,
                MsgSvc.SyncFlag.START -> return

                MsgSvc.SyncFlag.CONTINUE -> {
                    network.run {
                        PbGetMsg(client, MsgSvc.SyncFlag.CONTINUE, currentTimeSeconds).sendWithoutExpect()
                    }
                    return
                }
            }
        }
    }


    /**
     * 被挤下线
     */
    internal object PushForceOffline : OutgoingPacketFactory<ForceOfflineEvent>("MessageSvc.PushForceOffline") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ForceOfflineEvent {
            val struct = this.decodeUniPacket(RequestPushForceOffline.serializer())
            return ForceOfflineEvent(bot, title = struct.title ?: "", tips = struct.tips ?: "")
        }
    }

    internal object PbSendMsg : OutgoingPacketFactory<PbSendMsg.Response>("MessageSvc.PbSendMsg") {
        sealed class Response : Packet {
            object SUCCESS : Response() {
                override fun toString(): String = "MessageSvc.PbSendMsg.Response.SUCCESS"
            }

            data class Failed(val errorCode: Int, val errorMessage: String) : Response() {
                override fun toString(): String = "MessageSvc.PbSendMsg.Response.Failed(errorCode=$errorCode, errorMessage=$errorMessage"
            }
        }

        /**
         * 发送好友消息
         */
        fun ToFriend(
            client: QQAndroidClient,
            toUin: Long,
            message: MessageChain
        ): OutgoingPacket = buildOutgoingUniPacket(client) {

            ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

            ///return@buildOutgoingUniPacket
            writeProtoBuf(
                MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                    routingHead = MsgSvc.RoutingHead(c2c = MsgSvc.C2C(toUin = toUin)),
                    contentHead = MsgComm.ContentHead(pkgNum = 1),
                    msgBody = ImMsgBody.MsgBody(
                        richText = ImMsgBody.RichText(
                            elems = message.toRichTextElems()
                        )
                    ),
                    msgSeq = client.atomicNextMessageSequenceId(),
                    msgRand = Random.nextInt().absoluteValue,
                    syncCookie = SyncCookie(time = currentTimeSeconds).toByteArray(SyncCookie.serializer())
                    // msgVia = 1
                )
            )
        }

        /**
         * 发送群消息
         */
        fun ToGroup(
            client: QQAndroidClient,
            groupCode: Long,
            message: MessageChain
        ): OutgoingPacket = buildOutgoingUniPacket(client) {

            ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

            val seq = client.atomicNextMessageSequenceId()
            ///return@buildOutgoingUniPacket
            writeProtoBuf(
                MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                    routingHead = MsgSvc.RoutingHead(grp = MsgSvc.Grp(groupCode = groupCode)),
                    contentHead = MsgComm.ContentHead(pkgNum = 1, divSeq = seq),
                    msgBody = ImMsgBody.MsgBody(
                        richText = ImMsgBody.RichText(
                            elems = message.toRichTextElems()
                        )
                    ),

                    //
                    //
                    //
                    msgSeq = seq,
                    msgRand = Random.nextInt().absoluteValue//,
                    //      syncCookie = ByteArray(0)
                    //  ?: SyncCookie(time = currentTimeSeconds + client.timeDifference).toByteArray(SyncCookie.serializer()),
                    , msgVia = 1
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val response = readProtoBuf(MsgSvc.PbSendMsgResp.serializer())
            return if (response.result == 0) {
                Response.SUCCESS
            } else {
                Response.Failed(response.errtype, response.errmsg)
            }
        }
    }
}

