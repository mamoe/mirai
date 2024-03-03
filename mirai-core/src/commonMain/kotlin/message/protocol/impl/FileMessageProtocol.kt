/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import io.ktor.utils.io.core.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.contact.impl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.data.FriendFileMessageImpl
import net.mamoe.mirai.internal.message.data.GroupFileMessageImpl
import net.mamoe.mirai.internal.message.flags.AllowSendFileMessage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE_AS_CHAIN
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.components
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageSender
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageTransformer
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.message.source.OnlineMessageSourceToFriendImpl
import net.mamoe.mirai.internal.message.source.createMessageReceipt
import net.mamoe.mirai.internal.message.visitor.MessageVisitorEx
import net.mamoe.mirai.internal.network.components.ClockHolder.Companion.clock
import net.mamoe.mirai.internal.network.components.SyncController.Companion.syncCookie
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.data.proto.ObjMsg
import net.mamoe.mirai.internal.network.protocol.data.proto.SubMsgType0x4
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.acceptChildren
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.getRandomUnsignedInt
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.systemProp

internal class FileMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        // no encoder
        add(Decoder())

        add(OutgoingMessageTransformer {
            if (attributes[OutgoingMessagePipelineContext.STEP] == SendMessageStep.FIRST
            ) {
                verifyFileMessage(currentMessageChain)
            }
        })

        add(FileMessageSender())

        MessageSerializer.superclassesScope(FileMessage::class, MessageContent::class, SingleMessage::class) {
            add(
                MessageSerializer(
                    GroupFileMessageImpl::class,
                    GroupFileMessageImpl.serializer()
                )
            )
        }
    }

    companion object {
        private val ALLOW_SENDING_FILE_MESSAGE = systemProp("mirai.message.allow.sending.file.message", false)

        fun verifyFileMessage(message: MessageChain) {
            var hasFileMessage = false
            var hasAllowSendFileMessage = false
            message.acceptChildren(object : RecursiveMessageVisitor<Unit>(), MessageVisitorEx<Unit, Unit> {
                override fun isFinished(): Boolean {
                    return hasAllowSendFileMessage // finish early if allow send
                }

                override fun visitAllowSendFileMessage(message: AllowSendFileMessage, data: Unit) {
                    hasAllowSendFileMessage = true
                }

                override fun visitFileMessage(message: FileMessage, data: Unit) {
                    if (ALLOW_SENDING_FILE_MESSAGE) return
                    // #1715
                    if (message !is GroupFileMessageImpl && message !is FriendFileMessageImpl) {
                        error("Customized FileMessage cannot be send")
                    }
                    if ((message is GroupFileMessageImpl && !message.allowSend) ||
                        (message is FriendFileMessageImpl && !message.allowSend)) {
                        hasFileMessage = true
                    }
                }
            })
            if (hasAllowSendFileMessage) {
                // allowing
                return
            }
            if (hasFileMessage) {
                throw IllegalStateException(
                    "Sending FileMessage is not allowed, as it may cause unexpected results. " +
                            "Add JVM argument `-Dmirai.message.allow.sending.file.message=true` to disable this check. " +
                            "Do this only for compatibility!"
                )
            }
        }
    }

    private class FileMessageSender : OutgoingMessageSender {
        override suspend fun OutgoingMessagePipelineContext.process() {
            val file = currentMessageChain[FileMessage] ?: return

            val contact = attributes[CONTACT]
            val bot = contact.bot

            val strategy = components[MessageProtocolStrategy]

            if (file is FriendFileMessageImpl) {
                markAsConsumed()

                val msgRand = getRandomUnsignedInt()
                val msgSeq = bot.client.sendFriendMessageSeq.next()

                var source: OnlineMessageSourceToFriendImpl
                val msgSvcPbSendMsgResp = bot.network.sendAndExpect<MessageSvcPbSendMsg.Response>(
                    MessageSvcPbSendMsg.createToFriendImpl(
                        bot.client,
                        contact.cast(),
                        currentMessageChain,
                        currentMessageChain,
                        false
                    ) { source = it }.single()
                )

                if (msgSvcPbSendMsgResp is MessageSvcPbSendMsg.Response.SUCCESS) {
                    collect(source.createMessageReceipt(contact, false))
                    return
                } else {
                    error("Failed to send FileMessage to contact $contact: MessageSvcPbSendMsg failed. $msgSvcPbSendMsgResp")
                }
            }

            if (file is GroupFileMessageImpl) {
                markAsConsumed()

                val source = coroutineScope {
                    val source = async {
                        strategy.constructSourceForSpecialMessage(attributes[ORIGINAL_MESSAGE_AS_CHAIN], 2021)
                    }

                    bot.network.sendAndExpect(FileManagement.Feed(bot.client, contact.id, file.busId, file.id))

                    source.await()
                }

                collect(source.createMessageReceipt(contact, true))
                return
            }

            error("FileMessage must not be implemented manually.")
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.transElemInfo != null && data.transElemInfo.elemType == 24) {
                markAsConsumed()
                data.transElemInfo.elemValue.read {
                    // group file feed
                    // 01 00 77 08 06 12 0A 61 61 61 61 61 61 2E 74 78 74 1A 06 31 35 42 79 74 65 3A 5F 12 5D 08 66 12 25 2F 64 37 34 62 62 66 33 61 2D 37 62 32 35 2D 31 31 65 62 2D 38 34 66 38 2D 35 34 35 32 30 30 37 62 35 64 39 66 18 0F 22 0A 61 61 61 61 61 61 2E 74 78 74 28 00 3A 00 42 20 61 33 32 35 66 36 33 34 33 30 65 37 61 30 31 31 66 37 64 30 38 37 66 63 33 32 34 37 35 34 39 63
                    //                fun getFileRsrvAttr(file: ObjMsg.MsgContentInfo.MsgFile): HummerResv21.ResvAttr? {
                    //                    if (file.ext.isEmpty()) return null
                    //                    val element = kotlin.runCatching {
                    //                        jsonForFileDecode.parseToJsonElement(file.ext) as? JsonObject
                    //                    }.getOrNull() ?: return null
                    //                    val extInfo = element["ExtInfo"]?.toString()?.decodeBase64() ?: return null
                    //                    return extInfo.loadAs(HummerResv21.ResvAttr.serializer())
                    //                }

                    val var7 = readByte()
                    if (var7 == 1.toByte()) {
                        while (remaining > 2) {
                            val proto = readProtoBuf(ObjMsg.ObjMsg.serializer(), readShort().toUShort().toInt())
                            // proto.msgType=6

                            val file = proto.msgContentInfo.firstOrNull()?.msgFile ?: continue // officially get(0) only.
                            //                        val attr = getFileRsrvAttr(file) ?: continue
                            //                        val info = attr.forwardExtFileInfo ?: continue

                            collect(
                                GroupFileMessageImpl(
                                    id = file.filePath,
                                    busId = file.busId, // path i.e. /a99e95fa-7b2d-11eb-adae-5452007b698a
                                    name = file.fileName,
                                    size = file.fileSize
                                )
                            )
                        }
                    }
                }
                return
            }

            val originalMsg = runCatching { attributes[MessageDecoderContext.CONTAINING_MSG] }.getOrNull()
            if (originalMsg != null && originalMsg.msgHead.msgType == 529) {
                markAsConsumed()
                val sub0x4 = originalMsg.msgBody.msgContent.loadAs(SubMsgType0x4.MsgBody.serializer())
                val msgNotOnline = sub0x4.msgNotOnlineFile ?: return

                // notify that friend had downloaded the file.
                if (msgNotOnline.subcmd == 2) return

                collect(
                    FriendFileMessageImpl(
                        sub0x4.msgNotOnlineFile.fileUuid.decodeToString(),
                        sub0x4.msgNotOnlineFile.fileName.decodeToString(),
                        sub0x4.msgNotOnlineFile.fileSize,
                        sub0x4.msgNotOnlineFile.fileMd5
                    )
                )
                return
            }
        }
    }
}