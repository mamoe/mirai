/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.source.MessageSourceInternal
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.highway.Highway
import net.mamoe.mirai.internal.network.highway.ResourceKind
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.LongMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.internal.network.protocol.packet.chat.MessageValidationData
import net.mamoe.mirai.internal.network.protocol.packet.chat.MultiMsg
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.concatAsLong
import net.mamoe.mirai.utils.gzip
import net.mamoe.mirai.utils.toLongUnsigned
import net.mamoe.mirai.utils.use
import kotlin.math.absoluteValue
import kotlin.random.Random

internal open class MultiMsgUploader(
    val client: QQAndroidClient,
    val isLong: Boolean,
    val random: Random,
    val contact: Contact,
    val components: ComponentStorage,
    val senderName: String
) {

    protected open fun newUploader(): MultiMsgUploader = MultiMsgUploader(
        client = client,
        isLong = isLong,
        random = random,
        contact = contact,
        components = components,
        senderName = senderName
    )

    val mainMsg = mutableListOf<MsgComm.Msg>()
    val nestedMsgs = mutableMapOf<String, MutableList<MsgComm.Msg>>()

    init {
        nestedMsgs["MultiMsg"] = mainMsg
    }

    protected open fun newNid(): String {
        var nid: String
        do {
            nid = "${random.nextInt().absoluteValue}"
        } while (nestedMsgs.containsKey(nid))
        return nid
    }

    open suspend fun emitMain(
        nodes: Collection<ForwardMessage.INode>,
    ) {
        emit("MultiMsg", nodes)
    }

    open suspend fun convertNestedForwardMessage(nestedForward: ForwardMessage, msgChain: MessageChain): MessageChain {
        suspend fun convertByMessageOrigin(origin: MessageOrigin): MessageChain? {
            if (origin.kind != MessageOriginKind.FORWARD) return null
            val resId = origin.resourceId
            if (resId != null) {
                val nid = newNid()
                emit(nid, nestedForward.nodeList)
                return messageChainOf(
                    RichMessage.forwardMessage(
                        resId = resId,
                        fileName = nid,
                        forwardMessage = nestedForward,
                    )
                )
            }
            return null
        }

        suspend fun convertByReUpload(): MessageChain {
            // Upload nested and refine to service msg
            val nestedMMUploader = newUploader()
            nestedMMUploader.emitMain(nestedForward.nodeList)

            val resId = nestedMMUploader.uploadAndReturnResId()

            val mirror = nestedMMUploader.nestedMsgs
            mirror.remove("MultiMsg")
            nestedMsgs.putAll(mirror)

            val nid = newNid()
            nestedMsgs[nid] = nestedMMUploader.mainMsg
            return messageChainOf(
                RichMessage.forwardMessage(
                    resId = resId,
                    fileName = nid,
                    forwardMessage = nestedForward,
                )
            )
        }

        msgChain.firstIsInstanceOrNull<MessageOrigin>()?.let { origin ->
            convertByMessageOrigin(origin)?.let { return it }
        }

        return convertByReUpload()
    }

    open suspend fun emit(id: String, msgs: Collection<ForwardMessage.INode>) {
        val nds = mutableListOf<MsgComm.Msg>().let { tmp ->
            nestedMsgs.getOrPut(id) { tmp }
        }

        val existsIds = mutableSetOf<Long>()
        val existsSeqs = mutableSetOf<Int>()

        class PendingMessage(
            var seq: Int, var uid: Int,
            var convertedMessageChain: MessageChain,
            val msg: ForwardMessage.INode,
        )

        val pendingMessages = mutableListOf<PendingMessage>()
        var hasMsgSource = false

        // Step1: Convert message & Get message ids
        msgs.forEach { msg ->
            var msgChain = msg.messageChain
            msgChain[ForwardMessage]?.let { nestedForward ->
                msgChain = convertNestedForwardMessage(nestedForward, msgChain)
            }

            msgChain = components[MessageProtocolFacade].preprocess(contact.impl(), msgChain, components)

            var seq: Int = -1
            var uid: Int = -1
            msg.messageChain.sourceOrNull?.let { source ->
                source as MessageSourceInternal
                hasMsgSource = true

                seq = source.sequenceIds.first()
                uid = source.internalIds.first()
            }

            pendingMessages.add(
                PendingMessage(
                    seq = seq, uid = uid, convertedMessageChain = msgChain,
                    msg = msg
                )
            )
        }
        // Step2: Fix duplicated messages
        if (hasMsgSource) {
            pendingMessages.forEach { pm ->
                if (pm.seq == -1 && pm.uid == -1) return@forEach

                while (true) {
                    if (existsSeqs.add(pm.seq)) return@forEach

                    pm.seq++
                    pm.uid = random.nextInt().absoluteValue
                }
            }
        }

        // Step3: Fill custom messages.....
        val randSeqStart = random.nextInt().absoluteValue.coerceAtMost(
            Int.MAX_VALUE - pendingMessages.size - 15405
        ).coerceAtLeast(141225)

        var seqStart = if (hasMsgSource) {
            val idx = pendingMessages.indexOfFirst { it.seq != -1 }
            // Assertion: idx != -1
            pendingMessages[idx].seq - idx
        } else randSeqStart

        pendingMessages.forEach { pm ->
            if (pm.seq != -1 && pm.uid != -1) {
                seqStart = pm.seq + 1
            } else {
                pm.seq = seqStart
                seqStart++

                do { // For patch: no duplicated id
                    pm.uid = random.nextInt().absoluteValue
                } while (!existsIds.add(pm.seq.concatAsLong(pm.uid)))
            }
        }

        // Step4: Verify sequence
        existsSeqs.clear()
        var lastSeq = 0
        var needPatch = false
        for (pm in pendingMessages) {
            if (pm.seq <= lastSeq) {
                needPatch = true
                break
            }
            lastSeq = pm.seq
            if (!existsSeqs.add(lastSeq)) {
                needPatch = true
                break
            }
        }
        // Step 5: Patch
        if (needPatch) {
            existsIds.clear()
            existsSeqs.clear()

            var ranSeqStart = randSeqStart
            for (pm in pendingMessages) {
                val oldSeq = pm.seq
                val oldUid = pm.uid
                pm.seq = ranSeqStart
                ranSeqStart++
                pm.uid = random.nextInt().absoluteValue

                for (otherpms in pendingMessages) {
                    val quoteReply = otherpms.convertedMessageChain[QuoteReply] ?: continue
                    val srco = quoteReply.source
                    val src = srco as? MessageSourceInternal ?: continue
                    if (src.sequenceIds.first() == oldSeq && src.internalIds.first() == oldUid) {
                        val newSrc = MessageSourceBuilder()
                            .allFrom(srco)
                            .id(pm.seq)
                            .internalId(pm.uid)
                            .time(srco.time)
                            .build(botId = client.uin, kind = srco.kind)
                        otherpms.convertedMessageChain = otherpms.convertedMessageChain + newSrc
                    }
                }
            }
        }

        val isToGroup = contact is Group
        // Step6: Convert
        pendingMessages.forEach { pm ->
            val msg0 = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = pm.msg.senderId,
                    fromNick = pm.msg.senderName,
                    msgSeq = pm.seq,
                    msgTime = pm.msg.time,
                    msgUid = 0x0100000000000000L or pm.uid.toLongUnsigned(),
                    mutiltransHead = MsgComm.MutilTransHead(
                        status = 0,
                        msgId = 1,
                    ),
                    msgType = if (isToGroup) {
                        82 // troop
                    } else {
                        9 // c2c
                    },
                    c2cCmd = if (isToGroup) 0 else 11,
                    groupInfo = if (isToGroup) MsgComm.GroupInfo(
                        groupCode = contact.id,
                        groupCard = pm.msg.senderName, // Cinnamon
                    ) else null,
                    isSrcMsg = false,
                ), msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = MessageProtocolFacade.encode(
                            pm.convertedMessageChain,
                            messageTarget = contact, withGeneralFlags = false, isForward = true
                        )
                    )
                )
            )
            nds.add(msg0)
        }
    }

    open fun toMessageValidationData(): MessageValidationData {
        val msgTransmit =
            MsgTransmit.PbMultiMsgTransmit(msg = mainMsg, pbItemList = nestedMsgs.asSequence().map { (name, msgList) ->
                MsgTransmit.PbMultiMsgItem(
                    fileName = name,
                    buffer = MsgTransmit.PbMultiMsgNew(msgList).toByteArray(MsgTransmit.PbMultiMsgNew.serializer())
                )
            }.toList())
        val bytes = msgTransmit.toByteArray(MsgTransmit.PbMultiMsgTransmit.serializer())

        return MessageValidationData(bytes.gzip())
    }

    open suspend fun uploadAndReturnResId(): String {
        val data = toMessageValidationData()

        val response = client.bot.network.sendAndExpect(
            MultiMsg.ApplyUp.createForGroup(
                buType = if (isLong) 1 else 2, client = client, messageData = data, dstUin = contact.uin
            )
        )

        lateinit var resId: String
        when (response) {
            is MultiMsg.ApplyUp.Response.MessageTooLarge -> error(
                "Internal error: message is too large, but this should be handled before sending. "
            )
            is MultiMsg.ApplyUp.Response.RequireUpload -> {
                resId = response.proto.msgResid

                val body = LongMsg.ReqBody(
                    subcmd = 1, platformType = 9, termType = 5, msgUpReq = listOf(
                        LongMsg.MsgUpReq(
                            msgType = 3, // group
                            dstUin = contact.uin,
                            msgId = 0,
                            msgUkey = response.proto.msgUkey,
                            needCache = 0,
                            storeType = 2,
                            msgContent = data.data
                        )
                    )
                ).toByteArray(LongMsg.ReqBody.serializer())

                body.toExternalResource().use { resource ->
                    Highway.uploadResourceBdh(
                        bot = client.bot, resource = resource, kind = when (isLong) {
                            true -> ResourceKind.LONG_MESSAGE
                            false -> ResourceKind.FORWARD_MESSAGE
                        }, commandId = 27, initialTicket = response.proto.msgSig
                    )
                }
            }
        }

        return resId // this must be initialized, 'lateinit' due to IDE complaint
    }
}
