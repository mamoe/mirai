/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PttMessage
import net.mamoe.mirai.message.data.firstOrNull
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.contact.GroupImpl
import net.mamoe.mirai.qqandroid.message.MessageSourceToFriendImpl
import net.mamoe.mirai.qqandroid.message.MessageSourceToGroupImpl
import net.mamoe.mirai.qqandroid.message.MessageSourceToTempImpl
import net.mamoe.mirai.qqandroid.message.toRichTextElems
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.absoluteValue
import kotlin.random.Random

internal object MessageSvcPbSendMsg : OutgoingPacketFactory<MessageSvcPbSendMsg.Response>("MessageSvc.PbSendMsg") {
    sealed class Response : Packet {
        object SUCCESS : Response() {
            override fun toString(): String = "MessageSvcPbSendMsg.Response.SUCCESS"
        }

        /**
         * 121: 被限制? 个别号才不能发
         */
        data class Failed(val resultType: Int, val errorCode: Int, val errorMessage: String) : Response() {
            override fun toString(): String =
                "MessageSvcPbSendMsg.Response.Failed(resultType=$resultType, errorCode=$errorCode, errorMessage=$errorMessage)"
        }
    }

    /**
     * 发送好友消息
     */
    @Suppress("FunctionName")
    internal fun createToFriendImpl(
        client: QQAndroidClient,
        toUin: Long,
        message: MessageChain,
        source: MessageSourceToFriendImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

        ///return@buildOutgoingUniPacket
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(c2c = MsgSvc.C2C(toUin = toUin)),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(forGroup = false, withGeneralFlags = true)
                    )
                ),
                msgSeq = source.sequenceId,
                msgRand = source.internalId,
                syncCookie = SyncCookie(time = source.time.toLong()).toByteArray(SyncCookie.serializer())
                // msgVia = 1
            )
        )
    }


    /**
     * 发送临时消息
     */
    internal fun createToTempImpl(
        client: QQAndroidClient,
        groupUin: Long,
        toUin: Long,
        message: MessageChain,
        source: MessageSourceToTempImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(
                    grpTmp = MsgSvc.GrpTmp(groupUin, toUin)
                ),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(forGroup = false, withGeneralFlags = true)
                    )
                ),
                msgSeq = source.sequenceId,
                msgRand = source.internalId,
                syncCookie = SyncCookie(time = source.time.toLong()).toByteArray(SyncCookie.serializer())
            )
        )
    }


    /**
     * 发送群消息
     */
    @Suppress("FunctionName")
    internal fun createToGroupImpl(
        client: QQAndroidClient,
        groupCode: Long,
        message: MessageChain,
        isForward: Boolean,
        source: MessageSourceToGroupImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

        // DebugLogger.debug("sending group message: " + message.toRichTextElems().contentToString())

        ///return@buildOutgoingUniPacket
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(grp = MsgSvc.Grp(groupCode = groupCode)),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(forGroup = true, withGeneralFlags = true),
                        ptt = message.firstOrNull(PttMessage)?.run {
                            ImMsgBody.Ptt(fileName = fileName.toByteArray(), fileMd5 = md5)
                        }
                    )
                ),
                msgSeq = client.atomicNextMessageSequenceId(),
                msgRand = source.internalId,
                syncCookie = EMPTY_BYTE_ARRAY,
                msgVia = 1,
                msgCtrl = if (isForward) MsgCtrl.MsgCtrl(
                    msgFlag = 4
                ) else null
            )
        )
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        val response = readProtoBuf(MsgSvc.PbSendMsgResp.serializer())
        return if (response.result == 0) {
            Response.SUCCESS
        } else {
            Response.Failed(
                response.result,
                response.errtype,
                response.errmsg
            )
        }
    }
}

internal inline fun MessageSvcPbSendMsg.createToTemp(
    client: QQAndroidClient,
    member: Member,
    message: MessageChain,
    crossinline sourceCallback: (MessageSourceToTempImpl) -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    val source = MessageSourceToTempImpl(
        internalId = Random.nextInt().absoluteValue,
        sender = client.bot,
        target = member,
        time = currentTimeSeconds.toInt(),
        sequenceId = client.atomicNextMessageSequenceId(),
        originalMessage = message
    )
    sourceCallback(source)
    return createToTempImpl(
        client,
        (member.group as GroupImpl).uin,
        member.id,
        message,
        source
    )
}

internal inline fun MessageSvcPbSendMsg.createToFriend(
    client: QQAndroidClient,
    qq: Friend,
    message: MessageChain,
    crossinline sourceCallback: (MessageSourceToFriendImpl) -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    val rand = Random.nextInt().absoluteValue
    val source = MessageSourceToFriendImpl(
        internalId = rand,
        sender = client.bot,
        target = qq,
        time = currentTimeSeconds.toInt(),
        sequenceId = client.nextFriendSeq(),
        originalMessage = message
    )
    sourceCallback(source)
    return createToFriendImpl(
        client,
        qq.id,
        message,
        source
    )
}

internal inline fun MessageSvcPbSendMsg.createToGroup(
    client: QQAndroidClient,
    group: Group,
    message: MessageChain,
    isForward: Boolean,
    crossinline sourceCallback: (MessageSourceToGroupImpl) -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    val source = MessageSourceToGroupImpl(
        group,
        internalId = Random.nextInt().absoluteValue,
        sender = client.bot,
        target = group,
        time = currentTimeSeconds.toInt(),
        originalMessage = message//,
        //   sourceMessage = message
    )
    sourceCallback(source)
    return createToGroupImpl(
        client,
        group.id,
        message,
        isForward,
        source
    )
}