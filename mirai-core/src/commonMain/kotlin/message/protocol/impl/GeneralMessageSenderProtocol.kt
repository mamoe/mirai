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
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.message.flags.ForceAsFragmentedMessage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.ProcessorCollector
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.CONTACT
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.MESSAGE_TO_RETRY
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.ORIGINAL_MESSAGE_AS_CHAIN
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.STEP
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessagePipelineContext.Companion.components
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageSender
import net.mamoe.mirai.internal.message.protocol.serialization.MessageSerializer
import net.mamoe.mirai.internal.message.source.createMessageReceipt
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.guild.send.MsgProxySendMsg
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.buildTypeSafeMap
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.truncated

internal class GeneralMessageSenderProtocol : MessageProtocol(PRIORITY_GENERAL_SENDER) {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(GeneralMessageSender(logger))

        add(MessageSerializer(MessageChain::class, MessageChain.serializer(), emptyArray()))
    }


    class GeneralMessageSender(
        private val logger: MiraiLogger,
    ) : OutgoingMessageSender {
        override suspend fun OutgoingMessagePipelineContext.process() {
            markAsConsumed()

            @Suppress("UNCHECKED_CAST")
            val strategy = components[MessageProtocolStrategy] as MessageProtocolStrategy<AbstractContact>
            val step = attributes[STEP]
            val contact = attributes[CONTACT]
            val bot = contact.bot

            var source: Deferred<OnlineMessageSource.Outgoing>? = null

            val packets = strategy.createPacketsForGeneralMessage(
                client = bot.client,
                contact = contact,
                message = currentMessageChain,
                originalMessage = attributes[ORIGINAL_MESSAGE_AS_CHAIN],
                fragmented = step == SendMessageStep.FRAGMENTED || currentMessageChain.contains(ForceAsFragmentedMessage)
            ) { source = it }

            if (sendAllPackets(bot, step, contact, packets)) {
                val sourceAwait = source?.await() ?: error("Internal error: source is not initialized")
                sourceAwait.tryEnsureSequenceIdAvailable()
                collect(sourceAwait.createMessageReceipt(contact, true))
            }
        }

        /**
         * @return `true`, if source needs to be added
         */
        private suspend fun OutgoingMessagePipelineContext.sendAllPackets(
            bot: AbstractBot,
            step: SendMessageStep,
            contact: Contact,
            packets: List<OutgoingPacket>
        ): Boolean {
            if (!step.allowMultiplePackets && packets.size != 1) {
                throw IllegalStateException("Internal error: step $step doesn't allow multiple packets while found ${packets.size} ones.")
            }

            packets.forEach { packet ->
                if (!sendSinglePacket(bot, packet, step, contact)) return@sendAllPackets false
            }

            return true
        }

        private suspend fun OutgoingMessagePipelineContext.sendSinglePacket(
            bot: AbstractBot,
            packet: OutgoingPacket,
            step: SendMessageStep,
            contact: Contact,
        ): Boolean {
            val originalMessage = attributes[ORIGINAL_MESSAGE]
            val protocolStrategy = components[MessageProtocolStrategy]
            val finalMessage = currentMessageChain

            try {
                val resp = protocolStrategy.sendPacket(bot, packet) as MessageSvcPbSendMsg.Response
                if (resp is MessageSvcPbSendMsg.Response.MessageTooLarge) {
                    logger.info { "STEP $step: message too large." }
                    val next = step.nextStepOrNull()
                        ?: throw MessageTooLargeException(
                            contact,
                            originalMessage,
                            finalMessage,
                            "Message '${finalMessage.content.truncated(10)}' is too large."
                        )

                    // retry with next step
                    logger.info { "Retrying with STEP $next" }
                    val (_, receipts) = processAlso(
                        attributes[MESSAGE_TO_RETRY],
                        extraAttributes = buildTypeSafeMap {
                            set(STEP, next)
                        },
                    )
                    check(receipts.size == 1) { "Internal error: expected exactly one receipt collected from sub-process, but found ${receipts.size}." }
                    // We expect to get a Receipt from processAlso
                    return false
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
                return true
            } catch (e: Exception) {
                val resp = protocolStrategy.sendPacket(bot, packet) as MsgProxySendMsg.Response
                if (resp is MsgProxySendMsg.Response.Failed) {
                    when (resp.resultCode) {
                        (-31072).toShort() -> return true // MSG_REPEATED 不知道是什么问题，可以正常发送
                        else -> error("${resp.resultCode}, reason: ${resp.message}")
                    }
                }
                check(resp is MsgProxySendMsg.Response.Success) {
                    "Send message failed: $resp"
                }
                return true
            }
        }

    }
}