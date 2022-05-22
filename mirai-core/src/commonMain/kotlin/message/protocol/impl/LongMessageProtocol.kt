/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.contact.takeContent
import net.mamoe.mirai.internal.message.data.longMessage
import net.mamoe.mirai.internal.message.flags.DontAsLongMessage
import net.mamoe.mirai.internal.message.flags.ForceAsLongMessage
import net.mamoe.mirai.internal.message.flags.IgnoreLengthCheck
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.HIGHWAY_UPLOADER
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.PROTOCOL_STRATEGY
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.STEP
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageTransformer
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.utils.currentTimeSeconds

internal class LongMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(OutgoingMessageTransformer {
            currentMessageChain =
                convertToLongMessageIfNeeded(
                    currentMessageChain,
                    attributes[STEP],
                    attributes[CONTACT],
                    attributes[PROTOCOL_STRATEGY]
                )
        })
    }

    private suspend fun OutgoingMessagePipelineContext.convertToLongMessageIfNeeded(
        chain: MessageChain,
        step: SendMessageStep,
        contact: AbstractContact,
        strategy: MessageProtocolStrategy<*>
    ): MessageChain {
        val uploader = attributes[HIGHWAY_UPLOADER]

        suspend fun sendLongImpl(): MessageChain {
            val time = currentTimeSeconds()
            val resId = uploader.uploadLongMessage(contact, strategy, chain, time.toInt())
            return chain + RichMessage.longMessage(
                brief = chain.takeContent(27),
                resId = resId,
                timeSeconds = time
            ) // LongMessageInternal replaces all contents and preserves metadata
        }

        return when (step) {
            SendMessageStep.FIRST -> {
                // 只需要在第一次发送的时候验证长度
                // 后续重试直接跳过
                if (chain.contains(ForceAsLongMessage)) {
                    return sendLongImpl()
                }

                if (!chain.contains(IgnoreLengthCheck)) {
                    chain.verifyLength(chain, contact)
                }

                chain
            }
            SendMessageStep.LONG_MESSAGE -> {
                if (chain.contains(DontAsLongMessage)) chain // fragmented
                else sendLongImpl()
            }
            SendMessageStep.FRAGMENTED -> chain
        }
    }
}