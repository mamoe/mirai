/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.flags.InternalFlagOnlyMessage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoder
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.ShowImageFlag
import net.mamoe.mirai.message.data.SingleMessage

internal class IgnoredMessagesProtocol : MessageProtocol(PRIORITY_IGNORE) {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder())
        add(Decoder())

        // 所有未处理的 Elem 都会变成 UnsupportedMessage 所有不用在这里处理
    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            when {
                data.elemFlags2 != null
                        || data.extraInfo != null
                        || data.generalFlags != null
                        || data.anonGroupMsg != null
                -> markAsConsumed()
            }
        }
    }

    private class Encoder : MessageEncoder<SingleMessage> {
        override suspend fun MessageEncoderContext.process(data: SingleMessage) {
            when (data) {
                is ForwardMessage,
                is MessageSource, // mirai metadata only
                -> {
                    markAsConsumed()
                }
                is InternalFlagOnlyMessage, is ShowImageFlag -> {
                    // ignored
                    markAsConsumed()
                }
            }

        }

    }
}