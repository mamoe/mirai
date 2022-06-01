/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.data.UnsupportedMessageImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.UnsupportedMessage

internal class UnsupportedMessageProtocol : MessageProtocol(priority = PRIORITY_UNSUPPORTED) {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Decoder())
        add(Encoder())

        MessageSerializer.superclassesScope(UnsupportedMessage::class, MessageContent::class, SingleMessage::class) {
            add(
                MessageSerializer(
                    UnsupportedMessageImpl::class,
                    UnsupportedMessageImpl.serializer()
                )
            )
        }
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            markAsConsumed()
            val struct = UnsupportedMessageImpl(data)
            if (struct.struct.isEmpty()) return
            collect(struct)
        }
    }

    private class Encoder : MessageEncoder<UnsupportedMessageImpl> {
        override suspend fun MessageEncoderContext.process(data: UnsupportedMessageImpl) {
            markAsConsumed()
            collect(data.structElem)
        }
    }
}