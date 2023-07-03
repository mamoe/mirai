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
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import kotlin.text.toByteArray

internal class AnimatedStickerProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(AnimatedSticker::class, AnimatedSticker.serializer()))
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.commonElem == null) return
            if (data.commonElem.serviceType != 37) return

            markAsConsumed()

            val proto = data.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype37.serializer())
            collect(AnimatedSticker(proto.qsId))
        }
    }

    private class Encoder : MessageEncoder<AnimatedSticker> {
        override suspend fun MessageEncoderContext.process(data: AnimatedSticker) {
            markAsConsumed()

            val businessType = when (data.id) {
                114 -> 2
                else -> 1
            }

            val pbElem = HummerCommelem.MsgElemInfoServtype37(
                packId = "1".encodeToByteArray(),
                stickerId = byteArrayOf(), // TODO
                qsId = data.id,
                sourceType = 1,
                stickerType = businessType,
                text = "/${data.name}".toByteArray(),
                randomType = 1
            ).toByteArray(HummerCommelem.MsgElemInfoServtype37.serializer())

            val commonElem = ImMsgBody.CommonElem(
                serviceType = 37,
                pbElem = pbElem,
                businessType = businessType
            )

            collect(ImMsgBody.Elem(commonElem = commonElem))
        }
    }
}