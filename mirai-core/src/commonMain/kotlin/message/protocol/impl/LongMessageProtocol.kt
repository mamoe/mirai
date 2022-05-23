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
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.flags.DontAsLongMessage
import net.mamoe.mirai.internal.message.flags.ForceAsLongMessage
import net.mamoe.mirai.internal.message.flags.IgnoreLengthCheck
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.outgoing.HighwayUploader
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.STEP
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.components
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageTransformer
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.truncated

internal class LongMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(OutgoingMessageTransformer {
            currentMessageChain =
                convertToLongMessageIfNeeded(
                    currentMessageChain,
                    attributes[STEP],
                    attributes[CONTACT]
                )
        })
    }

    /**
     * Convert to [LongMessageInternal] iff [SendMessageStep.FIRST] has failed.
     */
    private suspend fun OutgoingMessagePipelineContext.convertToLongMessageIfNeeded(
        chain: MessageChain,
        step: SendMessageStep,
        contact: AbstractContact,
    ): MessageChain {
        val uploader = components[HighwayUploader]

        suspend fun sendLongImpl(): MessageChain {
            val time = components[ClockHolder].local.currentTimeSeconds()
            val resId = uploader.uploadLongMessage(contact, components, chain, time.toInt())
            return chain + createLongMessage(
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

    private fun createLongMessage(brief: String, resId: String, timeSeconds: Long): LongMessageInternal {
        val limited: String = brief.truncated(30)
        val template = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <msg serviceID="35" templateID="1" action="viewMultiMsg"
                     brief="$limited"
                     m_resid="$resId"
                     m_fileName="$timeSeconds" sourceMsgId="0" url=""
                     flag="3" adverSign="0" multiMsgFlag="1">
                    <item layout="1">
                        <title>$limited</title>
                        <hr hidden="false" style="0"/>
                        <summary>点击查看完整消息</summary>
                    </item>
                    <source name="聊天记录" icon="" action="" appid="-1"/>
                </msg>
            """.trimIndent().trim()

        return LongMessageInternal(template, resId)
    }

}