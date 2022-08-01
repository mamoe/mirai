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
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toByteArray

internal class FaceProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder())
        add(Type1Decoder())
        add(Type2Decoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(
                MessageSerializer(
                    Face::class,
                    Face.serializer()
                )
            )
        }
    }

    private class Encoder : MessageEncoder<Face> {
        override suspend fun MessageEncoderContext.process(data: Face) {
            markAsConsumed()
            collect(
                if (data.id >= 260) {
                    ImMsgBody.Elem(commonElem = data.toCommData())
                } else {
                    ImMsgBody.Elem(face = data.toJceData())
                }
            )
        }

        private companion object {
            private val FACE_BUF = "00 01 00 04 52 CC F5 D0".hexToBytes()

            fun Face.toJceData(): ImMsgBody.Face {
                return ImMsgBody.Face(
                    index = this.id,
                    old = (0x1445 - 4 + this.id).toShort().toByteArray(),
                    buf = FACE_BUF
                )
            }

            fun Face.toCommData(): ImMsgBody.CommonElem {
                return ImMsgBody.CommonElem(
                    serviceType = 33,
                    pbElem = HummerCommelem.MsgElemInfoServtype33(
                        index = this.id,
                        name = "/${this.name}".toByteArray(),
                        compat = "/${this.name}".toByteArray()
                    ).toByteArray(HummerCommelem.MsgElemInfoServtype33.serializer()),
                    businessType = 1
                )

            }
        }
    }

    private class Type1Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val commonElem = data.commonElem ?: return
            if (commonElem.serviceType != 33) return
            markAsConsumed()

            val proto =
                commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype33.serializer())
            collect(Face(proto.index))
        }

    }

    private class Type2Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            val face = data.face ?: return
            markAsConsumed()
            collect(Face(face.index))
        }
    }
}