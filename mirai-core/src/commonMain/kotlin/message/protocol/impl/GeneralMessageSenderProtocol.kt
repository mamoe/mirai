/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.coroutines.Deferred
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.PROTOCOL_STRATEGY
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.STEP
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageSender
import net.mamoe.mirai.internal.message.source.createMessageReceipt
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.buildTypeSafeMap
import net.mamoe.mirai.utils.truncated

internal class GeneralMessageSenderProtocol : MessageProtocol(PRIORITY_GENERAL_SENDER) {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(GeneralMessageSender())
    }


    class GeneralMessageSender : OutgoingMessageSender {
        override suspend fun OutgoingMessagePipelineContext.process() {
            markAsConsumed()

            val strategy = attributes[PROTOCOL_STRATEGY]
            val step = attributes[STEP]
            val contact = attributes[CONTACT]
            val bot = contact.bot

            var source: Deferred<OnlineMessageSource.Outgoing>? = null

            val packets = strategy.createPacketsForGeneralMessage(
                client = bot.client,
                contact = contact,
                message = currentMessageChain,
                fragmented = step == SendMessageStep.FRAGMENTED,
                sourceCallback = { source = it }
            )

            sendAllPackets(bot, step, contact, packets)

            val sourceAwait = source?.await() ?: error("Internal error: source is not initialized")
            sourceAwait.tryEnsureSequenceIdAvailable()
            collect(sourceAwait.createMessageReceipt(contact, true))
        }

        private suspend fun OutgoingMessagePipelineContext.sendAllPackets(
            bot: AbstractBot,
            step: SendMessageStep,
            contact: Contact,
            packets: List<OutgoingPacket>
        ) = packets.forEach { packet ->
            val originalMessage = attributes[ORIGINAL_MESSAGE]
            val protocolStrategy = attributes[PROTOCOL_STRATEGY]
            val finalMessage = currentMessageChain

            val resp = protocolStrategy.sendPacket(bot, packet) as MessageSvcPbSendMsg.Response
            if (resp is MessageSvcPbSendMsg.Response.MessageTooLarge) {
                val next = step.nextStepOrNull()
                    ?: throw MessageTooLargeException(
                        contact,
                        originalMessage,
                        finalMessage,
                        "Message '${finalMessage.content.truncated(10)}' is too large."
                    )

                // retry with next step
                processAlso(
                    originalMessage.toMessageChain(),
                    extraAttributes = buildTypeSafeMap {
                        set(STEP, next)
                    },
                ) // We expect to get a Receipt from processAlso
                return@forEach
            }
            if (resp is MessageSvcPbSendMsg.Response.ServiceUnavailable) {
                throw IllegalStateException("Send message to $contact failed, server service is unavailable.")
            }
            if (resp is MessageSvcPbSendMsg.Response.Failed) {
                when (resp.resultType) {
                    120 -> if (contact is Group) throw BotIsBeingMutedException(contact, originalMessage)
                    121 -> if (AtAll in currentMessageChain) throw SendMessageFailedException(
                        contact,
                        SendMessageFailedException.Reason.AT_ALL_LIMITED,
                        originalMessage
                    )
                    299 -> if (contact is Group) throw SendMessageFailedException(
                        contact,
                        SendMessageFailedException.Reason.GROUP_CHAT_LIMITED,
                        originalMessage
                    )
                }
            }
            check(resp is MessageSvcPbSendMsg.Response.SUCCESS) {
                "Send message failed: $resp"
            }

        }

    }
}