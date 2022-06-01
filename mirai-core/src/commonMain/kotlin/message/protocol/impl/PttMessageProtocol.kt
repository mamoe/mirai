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
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoder
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext
import net.mamoe.mirai.internal.message.protocol.encode.MessageEncoderContext.Companion.collectGeneralFlags
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.hexToBytes

internal class PttMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {

        add(Encoder())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(At::class, At.serializer()))
            add(MessageSerializer(AtAll::class, AtAll.serializer()))
            add(MessageSerializer(PlainText::class, PlainText.serializer()))
        }
    }

    private class Encoder : MessageEncoder<PttMessage> {
        override suspend fun MessageEncoderContext.process(data: PttMessage) {
            markAsConsumed()
            collect(
                ImMsgBody.Elem(
                    extraInfo = ImMsgBody.ExtraInfo(flags = 16, groupMask = 1)
                )
            )
            collect(
                ImMsgBody.Elem(
                    elemFlags2 = ImMsgBody.ElemFlags2(
                        vipStatus = 1
                    )
                )
            )

            collectGeneralFlags {
                ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_PTT))
            }
        }

        private companion object {
            private val PB_RESERVE_FOR_PTT =
                "78 00 F8 01 00 C8 02 00 AA 03 26 08 22 12 22 41 20 41 3B 25 3E 16 45 3F 43 2F 29 3E 44 24 14 18 46 3D 2B 4A 44 3A 18 2E 19 29 1B 26 32 31 31 29 43".hexToBytes()
        }
    }
}