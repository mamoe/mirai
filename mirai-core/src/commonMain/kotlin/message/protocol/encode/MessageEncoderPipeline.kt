/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.encode

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.pipeline.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.*
import kotlin.coroutines.RestrictsSuspension

internal interface MessageEncoderPipeline :
    ProcessorPipeline<MessageEncoderProcessor<*>, MessageEncoderContext, SingleMessage, ImMsgBody.Elem> {
}

/**
 * The context for a [MessageEncoder]. [RestrictsSuspension] ensures no real suspension may happen during [Processor.process].
 */
@RestrictsSuspension
internal interface MessageEncoderContext : ProcessorPipelineContext<SingleMessage, ImMsgBody.Elem> {

    /**
     * General flags that should be appended to the end of the result.
     *
     * Do not update this property directly, but call [collectGeneralFlags].
     */
    var generalFlags: ImMsgBody.Elem

    companion object {
        val ADD_GENERAL_FLAGS = TypeKey<Boolean>("addGeneralFlags")
        val MessageEncoderContext.addGeneralFlags get() = attributes[ADD_GENERAL_FLAGS]

        /**
         * Override default generalFlags if needed
         */
        inline fun MessageEncoderContext.collectGeneralFlags(block: () -> ImMsgBody.Elem) {
            if (addGeneralFlags) {
                generalFlags = block()
            }
        }

        val CONTACT = TypeKey<ContactOrBot?>("contactOrBot")
        val MessageEncoderContext.contact get() = attributes[CONTACT]

        val ORIGINAL_MESSAGE = TypeKey<MessageChain>("originalMessage")
        val MessageEncoderContext.originalMessage get() = attributes[ORIGINAL_MESSAGE]

        val IS_FORWARD = TypeKey<Boolean>("isForward")
        val MessageEncoderContext.isForward get() = attributes[IS_FORWARD]
    }
}

/**
 * This pipeline encodes [SingleMessage] into [ImMsgBody.Elem]s, in the context of [MessageEncoderContext]
 */
internal open class MessageEncoderPipelineImpl :
    AbstractProcessorPipeline<MessageEncoderProcessor<*>, MessageEncoderContext, SingleMessage, ImMsgBody.Elem>(
        PipelineConfiguration(stopWhenConsumed = true),
        @OptIn(TestOnly::class)
        defaultTraceLogging
    ),
    MessageEncoderPipeline {

    private inner class MessageEncoderContextImpl(attributes: TypeSafeMap) : MessageEncoderContext,
        BaseContextImpl(attributes) {
        override var generalFlags: ImMsgBody.Elem by lateinitMutableProperty {
            ImMsgBody.Elem(generalFlags = ImMsgBody.GeneralFlags(pbReserve = PB_RESERVE_FOR_ELSE))
        }
    }

    override fun createContext(data: SingleMessage, attributes: TypeSafeMap): MessageEncoderContext =
        MessageEncoderContextImpl(attributes)

    companion object {
        private val PB_RESERVE_FOR_ELSE = "78 00 F8 01 00 C8 02 00".hexToBytes()

        @TestOnly
        val defaultTraceLogging: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.Factory.create(MessageEncoderPipelineImpl::class, "MessageEncoderPipeline")
                .withSwitch(systemProp("mirai.message.encoder.pipeline.log.full", false))
        }
    }
}