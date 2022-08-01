/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.message.data.ForwardMessageInternal
import net.mamoe.mirai.internal.message.data.LightAppInternal
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.message.runWithBugReport
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*

/**
 * Handles:
 * - [RichMessage]
 * - [LongMessageInternal]
 * - [ServiceMessage]
 * - [ForwardMessage]
 */
internal class RichMessageProtocol : MessageProtocol() {
    companion object {
        val UNSUPPORTED_MERGED_MESSAGE_PLAIN = PlainText("你的QQ暂不支持查看[转发多条消息]，请期待后续版本。")
    }

    override fun ProcessorCollector.collectProcessorsImpl() {
        add(RichMsgDecoder())
        add(LightAppDecoder())

        add(Encoder())

        MessageSerializer.superclassesScope(
            ServiceMessage::class,
            RichMessage::class,
            MessageContent::class,
            SingleMessage::class
        ) {
            add(MessageSerializer(SimpleServiceMessage::class, SimpleServiceMessage.serializer()))
        }

        MessageSerializer.superclassesScope(RichMessage::class, MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(LightApp::class, LightApp.serializer()))
        }

        add(MessageSerializer(MessageOriginKind::class, MessageOriginKind.serializer(), emptyArray()))
    }

    private class Encoder : MessageEncoder<RichMessage> {
        override suspend fun MessageEncoderContext.process(data: RichMessage) {
            markAsConsumed()
            val content = data.content.toByteArray().deflate()
            var longTextResId: String? = null
            when (data) {
                is ForwardMessageInternal -> {
                    collect(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = data.serviceId, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    // transformOneMessage(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                }
                is LongMessageInternal -> {
                    collect(
                        ImMsgBody.Elem(
                            richMsg = ImMsgBody.RichMsg(
                                serviceId = data.serviceId, // ok
                                template1 = byteArrayOf(1) + content
                            )
                        )
                    )
                    processAlso(UNSUPPORTED_MERGED_MESSAGE_PLAIN)
                    longTextResId = data.resId
                }
                is LightApp -> collect(
                    ImMsgBody.Elem(
                        lightApp = ImMsgBody.LightAppElem(
                            data = byteArrayOf(1) + content
                        )
                    )
                )
                else -> collect(
                    ImMsgBody.Elem(
                        richMsg = ImMsgBody.RichMsg(
                            serviceId = when (data) {
                                is ServiceMessage -> data.serviceId
                                else -> error("unsupported RichMessage: ${data::class.simpleName}")
                            },
                            template1 = byteArrayOf(1) + content
                        )
                    )
                )
            }

            collectGeneralFlags {
                if (longTextResId != null) {
                    ImMsgBody.Elem(
                        generalFlags = ImMsgBody.GeneralFlags(
                            longTextFlag = 1,
                            longTextResid = longTextResId,
                            pbReserve = "78 00 F8 01 00 C8 02 00".hexToBytes()
                        )
                    )
                } else {
                    // 08 09 78 00 A0 01 81 DC 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00
                    ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_RICH_MESSAGE))
                }
            }
        }

        private companion object {
            private val PB_RESERVE_FOR_RICH_MESSAGE =
                "08 09 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 08 03 90 04 80 80 80 10 B8 04 00 C0 04 00".hexToBytes()
        }
    }

    private class LightAppDecoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val lightApp = data.lightApp ?: return
            markAsConsumed()

            val content = runWithBugReport("解析 lightApp",
                { "resId=" + lightApp.msgResid + "data=" + lightApp.data.toUHexString() }) {
                when (lightApp.data[0].toInt()) {
                    0 -> lightApp.data.decodeToString(startIndex = 1)
                    1 -> lightApp.data.toReadPacket(offset = 1).inflateInput().readAllText()
                    else -> error("unknown compression flag=${lightApp.data[0]}")
                }
            }

            collect(LightAppInternal(content))
        }

    }

    private class RichMsgDecoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.richMsg == null) return

            val richMsg = data.richMsg

            val content = runWithBugReport("解析 richMsg", { richMsg.template1.toUHexString() }) {
                when (richMsg.template1[0].toInt()) {
                    0 -> richMsg.template1.decodeToString(startIndex = 1)
                    1 -> richMsg.template1.toReadPacket(offset = 1).inflateInput().readAllText()
                    else -> error("unknown compression flag=${richMsg.template1[0]}")
                }
            }

            fun findStringProperty(name: String): String {
                return content.substringAfter("$name=\"", "").substringBefore("\"", "")
            }

            val serviceId = when (val sid = richMsg.serviceId) {
                0 -> {
                    val serviceIdStr = findStringProperty("serviceID")
                    if (serviceIdStr.isEmpty() || serviceIdStr.isBlank()) {
                        0
                    } else {
                        serviceIdStr.toIntOrNull() ?: 0
                    }
                }
                else -> sid
            }
            when (serviceId) {
                // 5: 使用微博长图转换功能分享到QQ群
                /*
                            <?xml version="1.0" encoding="utf-8"?><msg serviceID="5" templateID="12345" brief="[分享]想要沐浴阳光，就别钻进
        阴影。 ???" ><item layout="0"><image uuid="{E5F68BD5-05F8-148B-9DA7-FECD026D30AD}.jpg" md5="E5F68BD505F8148B9DA7FECD026D
        30AD" GroupFiledid="2167263882" minWidth="120" minHeight="120" maxWidth="180" maxHeight="180" /></item><source name="新
        浪微博" icon="http://i.gtimg.cn/open/app_icon/00/73/69/03//100736903_100_m.png" appid="100736903" action="" i_actionData
        ="" a_actionData="" url=""/></msg>
                             */
                /**
                 * json?
                 */
                1 -> @Suppress("DEPRECATION_ERROR")
                collect(SimpleServiceMessage(1, content))
                /**
                 * [LongMessageInternal], [ForwardMessage]
                 */
                35 -> {

                    val resId = findStringProperty("m_resid")
                    val fileName = findStringProperty("m_fileName").takeIf { it.isNotEmpty() }

                    val msg = if (resId.isEmpty()) {
                        // Nested ForwardMessage
                        if (fileName != null && findStringProperty("action") == "viewMultiMsg") {
                            ForwardMessageInternal(content, null, fileName)
                        } else {
                            SimpleServiceMessage(35, content)
                        }
                    } else when (findStringProperty("multiMsgFlag").toIntOrNull()) {
                        1 -> LongMessageInternal(content, resId)
                        0 -> ForwardMessageInternal(content, resId, fileName)
                        else -> {
                            // from PC QQ
                            if (findStringProperty("action") == "viewMultiMsg") {
                                ForwardMessageInternal(content, resId, fileName)
                            } else {
                                SimpleServiceMessage(35, content)
                            }
                        }
                    }

                    collect(msg)
                }

                // 104 新群员入群的消息
                else -> {
                    collect(SimpleServiceMessage(serviceId, content))
                }
            }
        }

    }
}