/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import kotlin.text.toByteArray

internal class SuperFaceProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(SuperFace::class, SuperFace.serializer()))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.commonElem == null) return
            if (data.commonElem.serviceType != 37) return

            markAsConsumed()

            val proto = data.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype37.serializer())
            collect(SuperFace(face = proto.qsId, id = proto.stickerId.decodeToString(), type = proto.stickerType))
        }
    }

    private class Encoder : MessageEncoder<SuperFace> {
        override suspend fun MessageEncoderContext.process(data: SuperFace) {
            markAsConsumed()

            collect(ImMsgBody.Elem(commonElem = data.toCommData()))
            processAlso(PlainText("/${data.name}"))
            collectGeneralFlags {
                ImMsgBody.Elem(
                    generalFlags = ImMsgBody.GeneralFlags(
                        pbReserve = ImMsgBody.Text(str = "[${data.name}]请使用最新版手机QQ体验新功能")
                            .toByteArray(ImMsgBody.Text.serializer())
                    )
                )
            }
        }
    }

    companion object {
        fun SuperFace.toCommData(): ImMsgBody.CommonElem {
            return ImMsgBody.CommonElem(
                serviceType = 37,
                pbElem = HummerCommelem.MsgElemInfoServtype37(
                    packId = "1".encodeToByteArray(),
                    stickerId = id.encodeToByteArray(),
                    qsId = face,
                    sourceType = 1,
                    stickerType = type,
                    text = "/${name}".toByteArray(),
                    randomType = 1
                ).toByteArray(HummerCommelem.MsgElemInfoServtype37.serializer()),
                businessType = type
            )
        }
    }
}