/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.CustomMessage
import net.mamoe.mirai.utils.read
import net.mamoe.mirai.utils.toUHexString

internal class CustomMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder())
        add(Decoder())
    }

    private companion object {
        private const val MIRAI_CUSTOM_ELEM_TYPE = 103904510 // "mirai.hashCode()"
    }

    private class Encoder : MessageEncoder<CustomMessage> {
        override suspend fun MessageEncoderContext.process(data: CustomMessage) {
            markAsConsumed()

            @Suppress("UNCHECKED_CAST")
            collect(
                ImMsgBody.Elem(
                    customElem = ImMsgBody.CustomElem(
                        enumType = MIRAI_CUSTOM_ELEM_TYPE,
                        data = CustomMessage.dump(
                            data.getFactory() as CustomMessage.Factory<CustomMessage>,
                            data
                        )
                    )
                )
            )
        }

    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.customElem == null) return
            markAsConsumed()
            kotlin.runCatching {
                if (data.customElem.enumType != MIRAI_CUSTOM_ELEM_TYPE) return
                data.customElem.data.read {
                    CustomMessage.load(this)
                }
            }.fold(
                onFailure = {
                    if (it is CustomMessage.Companion.CustomMessageFullDataDeserializeInternalException) {
                        throw IllegalStateException(
                            "Internal error: " +
                                    "exception while deserializing CustomMessage head data," +
                                    " data=${data.customElem.data.toUHexString()}", it
                        )
                    } else {
                        it as CustomMessage.Companion.CustomMessageFullDataDeserializeUserException
                        throw IllegalStateException(
                            "User error: " +
                                    "exception while deserializing CustomMessage body," +
                                    " body=${it.body.toUHexString()}", it
                        )
                    }

                },
                onSuccess = {
                    if (it != null) {
                        collect(it)
                    }
                }
            )

        }

    }
}