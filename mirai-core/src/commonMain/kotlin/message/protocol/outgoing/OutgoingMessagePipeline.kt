/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.outgoing

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.MessageTooLargeException
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.SendMessageStep
import net.mamoe.mirai.internal.message.source.ensureSequenceIdAvailable
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.pipeline.*
import net.mamoe.mirai.internal.utils.estimateLength
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*

///////////////////////////////////////////////////////////////////////////
// Infrastructure for a ProcessorPipeline
///////////////////////////////////////////////////////////////////////////

// Just to change this easily — you'd know it's actually Unit — a placeholder.
internal typealias OutgoingMessagePipelineInput = MessageChain


internal interface OutgoingMessagePipeline :
    ProcessorPipeline<OutgoingMessagePipelineProcessor, OutgoingMessagePipelineContext, OutgoingMessagePipelineInput, MessageReceipt<*>>

internal open class OutgoingMessagePipelineImpl :
    AbstractProcessorPipeline<OutgoingMessagePipelineProcessor, OutgoingMessagePipelineContext, OutgoingMessagePipelineInput, MessageReceipt<*>>(
        PipelineConfiguration(stopWhenConsumed = true), @OptIn(TestOnly::class) defaultTraceLogging
    ), OutgoingMessagePipeline {

    inner class OutgoingMessagePipelineContextImpl(
        attributes: TypeSafeMap, override var currentMessageChain: MessageChain
    ) : OutgoingMessagePipelineContext, BaseContextImpl(attributes) {
        /**
         * Calls super [AbstractProcessorPipeline.BaseContextImpl.processAlso], Also updates [currentMessageChain].
         */
        override suspend fun processAlso(
            data: OutgoingMessagePipelineInput,
            extraAttributes: TypeSafeMap
        ): ProcessResult<out ProcessorPipelineContext<OutgoingMessagePipelineInput, MessageReceipt<*>>, MessageReceipt<*>> {
            return super.processAlso(data, extraAttributes).also { (context, _) ->
                this.currentMessageChain = (context as OutgoingMessagePipelineContext).currentMessageChain
            }
        }
    }

    override fun createContext(
        data: OutgoingMessagePipelineInput, attributes: TypeSafeMap
    ): OutgoingMessagePipelineContext = OutgoingMessagePipelineContextImpl(attributes, data)

    companion object {
        @TestOnly
        val defaultTraceLogging: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.Factory.create(OutgoingMessagePipelineImpl::class, "OutgoingMessagePipeline")
                .withSwitch(systemProp("mirai.message.outgoing.pipeline.log.full", false))
        }
    }
}


internal interface OutgoingMessagePipelineContext :
    ProcessorPipelineContext<OutgoingMessagePipelineInput, MessageReceipt<*>> {
    /**
     * Current message chain updated throughout the process. Will be updated from the [sub-processes][processAlso].
     */
    var currentMessageChain: MessageChain

    suspend fun MessageSource.tryEnsureSequenceIdAvailable() {
        val contact = attributes[CONTACT]
        val bot = contact.bot
        try {
            ensureSequenceIdAvailable()
        } catch (e: Exception) {
            bot.network.logger.warning(
                "Timeout awaiting sequenceId for message(${currentMessageChain.content.take(10)}). Some features may not work properly.",
                e
            )
        }
    }

    fun Iterable<SingleMessage>.countImages(): Int = this.count { it is Image }

    fun Iterable<SingleMessage>.verifyLength(
        originalMessage: Message, target: Contact,
    ): Int {
        val chain = this
        val length = estimateLength(target, 15001)
        if (length > 15000 || countImages() > 50) {
            throw MessageTooLargeException(
                target, originalMessage, this.toMessageChain(),
                "message(${
                    chain.joinToString("", limit = 10).let { rsp ->
                        if (rsp.length > 100) {
                            rsp.take(100) + "..."
                        } else rsp
                    }
                }) is too large. Allow up to 50 images or 5000 chars"
            )
        }
        return length
    }


    companion object {
        /**
         * Original
         */
        val ORIGINAL_MESSAGE = TypeKey<Message>("originalMessage")

        /**
         * You should only use [ORIGINAL_MESSAGE_AS_CHAIN] if you can't use [ORIGINAL_MESSAGE]
         */
        val ORIGINAL_MESSAGE_AS_CHAIN = TypeKey<MessageChain>("originalMessageAsChain")

        /**
         * Message chain used when retrying with next [step][SendMessageStep]s.
         */
        val MESSAGE_TO_RETRY = TypeKey<MessageChain>("messageToRetry")

        /**
         * Message target
         */
        val CONTACT = TypeKey<AbstractContact>("contact")

        val STEP = TypeKey<SendMessageStep>("step")

        val COMPONENTS = TypeKey<ComponentStorage>("components")
        val OutgoingMessagePipelineContext.components: ComponentStorage get() = attributes[COMPONENTS]
    }
}

