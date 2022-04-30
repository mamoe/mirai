/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.pipeline.PipelineConsumptionMarker
import net.mamoe.mirai.internal.pipeline.Processor
import net.mamoe.mirai.internal.pipeline.ProcessorPipeline
import net.mamoe.mirai.internal.pipeline.ProcessorPipelineContext
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.TypeKey
import net.mamoe.mirai.utils.uncheckedCast
import kotlin.coroutines.RestrictsSuspension
import kotlin.reflect.KClass

internal interface MessageEncoderPipeline :
    ProcessorPipeline<MessageEncoderProcessor<*>, SingleMessage, ImMsgBody.Elem> {
}

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


internal fun interface MessageEncoder<T : SingleMessage> : PipelineConsumptionMarker {
    suspend fun MessageEncoderContext.process(data: T)
}

/**
 * Adapter for [MessageEncoder] to be used as [Processor].
 */
internal class MessageEncoderProcessor<T : SingleMessage>(
    private val encoder: MessageEncoder<T>,
    private val elementType: KClass<T>,
) : Processor<MessageEncoderContext, SingleMessage> {
    override suspend fun process(context: MessageEncoderContext, data: SingleMessage) {
        if (elementType.isInstance(data)) {
            @Suppress("ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL")
            encoder.run { context.process(data.uncheckedCast()) }
            // TODO: 2022/4/27 handle exceptions
        }
    }
}
