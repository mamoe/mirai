/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.contact.SendMessageHandler
import net.mamoe.mirai.internal.contact.takeSingleContent
import net.mamoe.mirai.internal.network.QQAndroidClient
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
import kotlin.math.absoluteValue
import kotlin.random.Random

internal open class MultiMsgUploader(
    val client: QQAndroidClient,
    val isLong: Boolean,
    val handler: SendMessageHandler<*>,
    val tmpRand: Random = Random.Default,
) {

    protected open fun newUploader(): MultiMsgUploader = MultiMsgUploader(
        isLong = isLong,
        handler = handler,
        client = client,
        tmpRand = tmpRand,
    )

    val mainMsg = mutableListOf<MsgComm.Msg>()
    val nestedMsgs = mutableMapOf<String, MutableList<MsgComm.Msg>>()

    init {
        nestedMsgs["MultiMsg"] = mainMsg
    }

    protected open fun newNid(): String {
        var nid: String
        do {
            nid = "${tmpRand.nextInt().absoluteValue}"
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
            nestedMsgs.putIfAbsent(id, tmp) ?: tmp
        }

        val existsIds = mutableSetOf<Long>()

        msgs.forEach { msg ->
            var msgChain = msg.messageChain
            msgChain.takeSingleContent<ForwardMessage>()?.let { nestedForward ->
                msgChain = convertNestedForwardMessage(nestedForward, msgChain)
            }

            msgChain = handler.conversionMessageChain(msgChain)

            var seq: Int = -1
            var uid: Int = -1
            msg.messageChain.sourceOrNull?.let { source ->
                source as MessageSourceInternal

                seq = source.sequenceIds.first()
                uid = source.internalIds.first()
            }
            while (true) {
                if (seq != -1 && uid != -1) {
                    if (existsIds.add(seq.concatAsLong(uid))) break
                }
                seq = tmpRand.nextInt().absoluteValue
                uid = tmpRand.nextInt().absoluteValue
            }

            val msg0 = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = msg.senderId,
                    toUin = if (isLong) {
                        handler.targetUserUin ?: 0
                    } else 0,
                    msgSeq = seq,
                    msgTime = msg.time,
                    msgUid = 0x01000000000000000L or uid.toLongUnsigned(),
                    mutiltransHead = MsgComm.MutilTransHead(
                        status = 0,
                        msgId = 1,
                    ),
                    msgType = 82, // troop,
                    groupInfo = handler.run { msg.groupInfo },
                    isSrcMsg = false,
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = msgChain.toRichTextElems(
                            handler.contact,
                            withGeneralFlags = false,
                            isForward = true,
                        ).toMutableList()
                    )
                )
            )
            nds.add(msg0)
        }
    }

    open fun toMessageValidationData(): MessageValidationData {
        val msgTransmit = MsgTransmit.PbMultiMsgTransmit(
            msg = mainMsg,
            pbItemList = nestedMsgs.asSequence()
                .map { (name, msgList) ->
                    MsgTransmit.PbMultiMsgItem(
                        fileName = name,
                        buffer = MsgTransmit.PbMultiMsgNew(msgList).toByteArray(MsgTransmit.PbMultiMsgNew.serializer())
                    )
                }
                .toList()
        )
        val bytes = msgTransmit.toByteArray(MsgTransmit.PbMultiMsgTransmit.serializer())

        return MessageValidationData(bytes.gzip())
    }

    open suspend fun uploadAndReturnResId(): String {
        val data = toMessageValidationData()

        val response = client.bot.network.run {
            MultiMsg.ApplyUp.createForGroup(
                buType = if (isLong) 1 else 2,
                client = client,
                messageData = data,
                dstUin = handler.targetUin
            ).sendAndExpect()
        }

        val resId: String
        when (response) {
            is MultiMsg.ApplyUp.Response.MessageTooLarge ->
                error(
                    "Internal error: message is too large, but this should be handled before sending. "
                )
            is MultiMsg.ApplyUp.Response.RequireUpload -> {
                resId = response.proto.msgResid

                val body = LongMsg.ReqBody(
                    subcmd = 1,
                    platformType = 9,
                    termType = 5,
                    msgUpReq = listOf(
                        LongMsg.MsgUpReq(
                            msgType = 3, // group
                            dstUin = handler.targetUin,
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
                        bot = client.bot,
                        resource = resource,
                        kind = when (isLong) {
                            true -> ResourceKind.LONG_MESSAGE
                            false -> ResourceKind.FORWARD_MESSAGE
                        },
                        commandId = 27,
                        initialTicket = response.proto.msgSig
                    )
                }
            }
        }

        return resId
    }
}
