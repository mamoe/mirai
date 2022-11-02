/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.contact.MessageTooLargeException
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.data.forwardMessage
import net.mamoe.mirai.internal.message.flags.IgnoreLengthCheck
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.outgoing.HighwayUploader
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.components
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePreprocessor
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.message.data.*

internal class ForwardMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(ForwardMessageUploader())

        MessageSerializer.superclassesScope(MessageContent::class, SingleMessage::class) {
            add(MessageSerializer(ForwardMessage::class, ForwardMessage.serializer()))
        }
    }

    class ForwardMessageUploader : OutgoingMessagePreprocessor {
        override suspend fun OutgoingMessagePipelineContext.process() {
            val forward = currentMessageChain[ForwardMessage] ?: return

            val contact = attributes[CONTACT]
            if (!currentMessageChain.contains(IgnoreLengthCheck)) {
                checkLength(forward, contact)
                sequence {
                    forward.nodeList.forEach { yieldAll(it.messageChain) }
                }.asIterable().verifyLength(forward, contact)
            }

            val resId = components[HighwayUploader].uploadMessages(
                contact,
                components,
                forward.nodeList,
                false
            )

            currentMessageChain += RichMessage.forwardMessage(
                resId = resId,
                fileName = components[ClockHolder].local.currentTimeSeconds().toString(),
                forwardMessage = forward,
            ).toMessageChain()
        }

        private fun checkLength(
            forward: ForwardMessage,
            contact: AbstractContact
        ) {
            check(forward.nodeList.size <= 200) {
                throw MessageTooLargeException(
                    contact, forward, forward,
                    "ForwardMessage allows up to 200 nodes, but found ${forward.nodeList.size}"
                )
            }
        }
    }
}