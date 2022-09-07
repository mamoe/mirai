/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.contact.groupCode
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.contact.userIdOrNull
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
            nestedMsgs.putIfAbsent(id, tmp) ?: tmp
        }

        val existsIds = mutableSetOf<Long>()

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

                seq = source.sequenceIds.first()
                uid = source.internalIds.first()
            }
            while (true) {
                if (seq != -1 && uid != -1) {
                    if (existsIds.add(seq.concatAsLong(uid))) break
                }
                seq = random.nextInt().absoluteValue
                uid = random.nextInt().absoluteValue
            }

            val msg0 = MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = msg.senderId,
                    fromNick = msg.senderName,
                    toUin = if (isLong) {
                        contact.userIdOrNull ?: 0
                    } else 0,
                    msgSeq = seq,
                    msgTime = msg.time,
                    msgUid = 0x01000000000000000L or uid.toLongUnsigned(),
                    mutiltransHead = MsgComm.MutilTransHead(
                        status = 0,
                        msgId = 1,
                    ),
                    msgType = 82, // troop,
                    groupInfo = MsgComm.GroupInfo(
                        groupCode = if (contact is Group) contact.groupCode else 0L,
                        groupCard = msg.senderName, // Cinnamon
                    ),
                    isSrcMsg = false,
                ), msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = MessageProtocolFacade.encode(
                            msgChain, messageTarget = contact, withGeneralFlags = false, isForward = true
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
