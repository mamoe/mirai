/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.groupCode
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.QQAndroidClient.MessageSvcSyncData.PendingGroupMessageReceiptSyncId
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgCtrl
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.currentTimeSeconds
import java.util.concurrent.atomic.AtomicReference
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

    internal fun MessageChain.fragmented(): List<MessageChain> {
        val results = mutableListOf<MessageChain>()
        var txtAdd = false
        val last = mutableListOf<Message>()
        fun flush() {
            txtAdd = false
            if (last.isNotEmpty()) {
                results.add(ArrayList(last).asMessageChain())
                last.clear()
            }
        }
        forEach { element ->
            if (last.size >= 4) {
                flush()
            }
            if (element is PlainText) {
                if (txtAdd) {
                    flush()
                }
                if (element.content.length < 80) {
                    txtAdd = true
                    last.add(element)
                } else {
                    val splitted = element.content.chunked(80)
                    flush()
                    splitted.forEach { results.add(PlainText(it).asMessageChain()) }
                }
            } else {
                last.add(element)
            }
        }
        flush()
        return results
    }

    internal inline fun buildOutgoingMessageCommon(
        client: QQAndroidClient,
        message: MessageChain,
        fragmentTranslator: (MessageChain) -> ImMsgBody.MsgBody,
        pbSendMsgReq: (
            msgBody: ImMsgBody.MsgBody,
            msgSeq: Int,
            msgRand: Int,
            contentHead: MsgComm.ContentHead
        ) -> MsgSvc.PbSendMsgReq,
        sequenceIds: AtomicReference<IntArray>,
        sequenceIdsInitializer: (Int) -> IntArray,
        randIds: AtomicReference<IntArray>,
        postInit: () -> Unit
    ): List<OutgoingPacket> {
        val fragmented = message.fragmented()
        val response = mutableListOf<OutgoingPacket>()
        val div = if (fragmented.size == 1) 0 else Random.nextInt().absoluteValue
        val pkgNum = fragmented.size

        val seqIds = sequenceIdsInitializer(pkgNum)
        val randIds0 = IntArray(pkgNum) { Random.nextInt().absoluteValue }
        sequenceIds.set(seqIds)
        randIds.set(randIds0)
        postInit()
        fragmented.forEachIndexed { pkgIndex, fMsg ->
            response.add(buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    MsgSvc.PbSendMsgReq.serializer(),
                    pbSendMsgReq(
                        fragmentTranslator(fMsg),
                        seqIds[pkgIndex],
                        randIds0[pkgIndex],
                        MsgComm.ContentHead(
                            pkgNum = pkgNum,
                            divSeq = div,
                            pkgIndex = pkgIndex
                        )
                    )
                )
            })
        }
        return response
    }

    /**
     * 发送陌生人消息
     */
    @Suppress("FunctionName")
    internal inline fun createToStrangerImpl(
        client: QQAndroidClient,
        target: Stranger,
        message: MessageChain,
        source: MessageSourceToStrangerImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

        ///return@buildOutgoingUniPacket
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(c2c = MsgSvc.C2C(toUin = target.uin)),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(messageTarget = target, withGeneralFlags = true)
                    )
                ),
                msgSeq = source.sequenceIds.single(),
                msgRand = source.internalIds.single(),
                syncCookie = client.syncingController.syncCookie ?: byteArrayOf()
                // msgVia = 1
            )
        )
    }

    /**
     * 发送好友消息
     */
    @Suppress("FunctionName")
    internal inline fun createToFriendImpl(
        client: QQAndroidClient,
        targetFriend: Friend,
        message: MessageChain,
        crossinline sourceCallback: (MessageSourceToFriendImpl) -> Unit
    ): List<OutgoingPacket> {
        contract {
            callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
        }

        val sequenceIds = AtomicReference<IntArray>()
        val randIds = AtomicReference<IntArray>()
        return buildOutgoingMessageCommon(
            client = client,
            message = message,
            fragmentTranslator = {
                ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = it.toRichTextElems(messageTarget = targetFriend, withGeneralFlags = true)
                    )
                )
            },
            pbSendMsgReq = { msgBody, msgSeq, msgRand, contentHead ->
                MsgSvc.PbSendMsgReq(
                    routingHead = MsgSvc.RoutingHead(c2c = MsgSvc.C2C(toUin = targetFriend.uin)),
                    contentHead = contentHead,
                    msgBody = msgBody,
                    msgSeq = msgSeq,
                    msgRand = msgRand,
                    syncCookie = client.syncingController.syncCookie ?: byteArrayOf()
                    // msgVia = 1
                )
            },
            sequenceIds = sequenceIds,
            randIds = randIds,
            sequenceIdsInitializer = { size ->
                IntArray(size) { client.nextFriendSeq() }
            },
            postInit = {
                sourceCallback(
                    MessageSourceToFriendImpl(
                        internalIds = randIds.get(),
                        sender = client.bot,
                        target = targetFriend,
                        time = currentTimeSeconds().toInt(),
                        sequenceIds = sequenceIds.get(),
                        originalMessage = message
                    )
                )
            }
        )
    }
    /*= buildOutgoingUniPacket(client) {
        ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

        val rand = Random.nextInt().absoluteValue
        val source = MessageSourceToFriendImpl(
            internalIds = intArrayOf(rand),
            sender = client.bot,
            target = qq,
            time = currentTimeSeconds().toInt(),
            sequenceIds = intArrayOf(client.nextFriendSeq()),
            originalMessage = message
        )
        sourceCallback(source)

        ///return@buildOutgoingUniPacket
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(c2c = MsgSvc.C2C(toUin = targetFriend.uin)),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(messageTarget = targetFriend, withGeneralFlags = true)
                    )
                ),
                msgSeq = source.sequenceIds.single(),
                msgRand = source.internalIds.single(),
                syncCookie = client.syncingController.syncCookie ?: byteArrayOf()
                // msgVia = 1
            )
        )
    }
    */


    /**
     * 发送临时消息
     */
    internal fun createToTempImpl(
        client: QQAndroidClient,
        targetMember: Member,
        message: MessageChain,
        source: MessageSourceToTempImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(
                    grpTmp = MsgSvc.GrpTmp(targetMember.group.uin, targetMember.id)
                ),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(messageTarget = targetMember, withGeneralFlags = true)
                    )
                ),
                msgSeq = source.sequenceIds.single(),
                msgRand = source.internalIds.single(),
                syncCookie = client.syncingController.syncCookie ?: byteArrayOf()
            )
        )
    }


    /**
     * 发送群消息
     */
    @Suppress("FunctionName")
    internal fun createToGroupImpl(
        client: QQAndroidClient,
        targetGroup: Group,
        message: MessageChain,
        isForward: Boolean,
        source: MessageSourceToGroupImpl
    ): OutgoingPacket = buildOutgoingUniPacket(client) {
        ///writeFully("0A 08 0A 06 08 89 FC A6 8C 0B 12 06 08 01 10 00 18 00 1A 1F 0A 1D 12 08 0A 06 0A 04 F0 9F 92 A9 12 11 AA 02 0E 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 9B 7A 28 F4 CA 9B B8 03 32 34 08 92 C2 C4 F1 05 10 92 C2 C4 F1 05 18 E6 ED B9 C3 02 20 89 FE BE A4 06 28 89 84 F9 A2 06 48 DE 8C EA E5 0E 58 D9 BD BB A0 09 60 1D 68 92 C2 C4 F1 05 70 00 40 01".hexToBytes())

        // DebugLogger.debug("sending group message: " + message.toRichTextElems().contentToString())

        ///return@buildOutgoingUniPacket
        writeProtoBuf(
            MsgSvc.PbSendMsgReq.serializer(), MsgSvc.PbSendMsgReq(
                routingHead = MsgSvc.RoutingHead(grp = MsgSvc.Grp(groupCode = targetGroup.groupCode)),
                contentHead = MsgComm.ContentHead(pkgNum = 1),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = message.toRichTextElems(messageTarget = targetGroup, withGeneralFlags = true),
                        ptt = message[PttMessage]?.run {
                            ImMsgBody.Ptt(
                                fileName = fileName.toByteArray(),
                                fileMd5 = md5,
                                boolValid = true,
                                fileSize = fileSize.toInt(),
                                fileType = 4,
                                pbReserve = byteArrayOf(0),
                                format = let {
                                    if (it is Voice) {
                                        it.codec
                                    } else {
                                        0
                                    }
                                }
                            )
                        }
                    )
                ),
                msgSeq = client.atomicNextMessageSequenceId(),
                msgRand = source.internalIds.single(),
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
        internalIds = intArrayOf(Random.nextInt().absoluteValue),
        sender = client.bot,
        target = member,
        time = currentTimeSeconds().toInt(),
        sequenceIds = intArrayOf(client.atomicNextMessageSequenceId()),
        originalMessage = message
    )
    sourceCallback(source)
    return createToTempImpl(
        client,
        member,
        message,
        source
    )
}

internal inline fun MessageSvcPbSendMsg.createToStranger(
    client: QQAndroidClient,
    stranger: Stranger,
    message: MessageChain,
    crossinline sourceCallback: (MessageSourceToStrangerImpl) -> Unit
): OutgoingPacket {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    val source = MessageSourceToStrangerImpl(
        internalIds = intArrayOf(Random.nextInt().absoluteValue),
        sender = client.bot,
        target = stranger,
        time = currentTimeSeconds().toInt(),
        sequenceIds = intArrayOf(client.atomicNextMessageSequenceId()),
        originalMessage = message
    )
    sourceCallback(source)
    return createToStrangerImpl(
        client,
        stranger,
        message,
        source
    )
}

internal inline fun MessageSvcPbSendMsg.createToFriend(
    client: QQAndroidClient,
    qq: Friend,
    message: MessageChain,
    crossinline sourceCallback: (MessageSourceToFriendImpl) -> Unit
): List<OutgoingPacket> {
    contract {
        callsInPlace(sourceCallback, InvocationKind.EXACTLY_ONCE)
    }
    return createToFriendImpl(
        client,
        qq,
        message,
        sourceCallback
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
    val messageRandom = Random.nextInt().absoluteValue
    val source = MessageSourceToGroupImpl(
        group,
        internalIds = intArrayOf(messageRandom),
        sender = client.bot,
        target = group,
        time = currentTimeSeconds().toInt(),
        originalMessage = message//,
        //   sourceMessage = message
    )

    sourceCallback(source)

    client.syncingController.pendingGroupMessageReceiptCacheList.addCache(
        PendingGroupMessageReceiptSyncId(
            messageRandom = messageRandom,
        )
    )
    return createToGroupImpl(
        client,
        group,
        message,
        isForward,
        source
    )
}