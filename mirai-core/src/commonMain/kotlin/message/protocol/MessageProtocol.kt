/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.pipeline.PipelineConsumptionMarker
import net.mamoe.mirai.internal.pipeline.Processor
import net.mamoe.mirai.internal.pipeline.ProcessorPipeline
import net.mamoe.mirai.internal.pipeline.ProcessorPipelineContext
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.TypeKey
import net.mamoe.mirai.utils.uncheckedCast
import java.util.*
import kotlin.reflect.KClass

internal abstract class ProcessorCollector {
    inline fun <reified T : SingleMessage> add(encoder: MessageEncoder<T>) = add(encoder, T::class)


    abstract fun <T : SingleMessage> add(encoder: MessageEncoder<T>, elementType: KClass<T>)

    abstract fun add(decoder: MessageDecoder)
}

internal abstract class MessageProtocol(
    private val priority: UInt = 1000u // the higher, the prior it being called
) {
    fun collectProcessors(processorCollector: ProcessorCollector) {
        processorCollector.collectProcessorsImpl()
    }

    protected abstract fun ProcessorCollector.collectProcessorsImpl()

    companion object {
        const val PRIORITY_METADATA: UInt = 10000u
        const val PRIORITY_CONTENT: UInt = 1000u
        const val PRIORITY_UNSUPPORTED: UInt = 100u
    }
}

internal object MessageProtocols {
    val instances: List<MessageProtocol> = initialize()

    private fun initialize(): List<MessageProtocol> {
        val encoderPipeline = MessageEncoderPipelineImpl()
        val decoderPipeline = MessageDecoderPipelineImpl()

        val instances = ServiceLoader.load(MessageProtocol::class.java).iterator().asSequence().toList()
        for (instance in instances) {
            instance.collectProcessors(object : ProcessorCollector() {
                override fun <T : SingleMessage> add(encoder: MessageEncoder<T>, elementType: KClass<T>) {
                    encoderPipeline.registerProcessor(MessageEncoderProcessor(encoder, elementType))
                }

                override fun add(decoder: MessageDecoder) {
                    decoderPipeline.registerProcessor(MessageDecoderProcessor(decoder))
                }

            })
        }

        return instances
    }

}

///////////////////////////////////////////////////////////////////////////
// decoders
///////////////////////////////////////////////////////////////////////////

internal interface MessageDecoderContext : ProcessorPipelineContext<ImMsgBody.Elem, Message> {
    companion object {
        val BOT = TypeKey<Bot>("bot")
        val MESSAGE_SOURCE_KIND = TypeKey<MessageSourceKind>("messageSourceKind")
        val GROUP_ID = TypeKey<Long>("groupId") // zero if not group
    }
}

internal interface MessageDecoder : PipelineConsumptionMarker {
    suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem)
}

/**
 * Adapter for [MessageDecoder] to be used as [Processor].
 */
internal class MessageDecoderProcessor(
    private val decoder: MessageDecoder
) : Processor<MessageDecoderContext, ImMsgBody.Elem> {
    override suspend fun process(context: MessageDecoderContext, data: ImMsgBody.Elem) {
        decoder.run { context.process(data) }
        // TODO: 2022/4/27 handle exceptions
    }
}

internal interface MessageDecoderPipeline : ProcessorPipeline<MessageDecoderProcessor, ImMsgBody.Elem, Message>

///////////////////////////////////////////////////////////////////////////
// encoders
///////////////////////////////////////////////////////////////////////////

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

        val CONTACT = TypeKey<Contact>("contact")
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
            encoder.run { context.process(data.uncheckedCast()) }
            // TODO: 2022/4/27 handle exceptions
        }
    }
}

internal interface MessageEncoderPipeline : ProcessorPipeline<MessageEncoderProcessor<*>, SingleMessage, ImMsgBody.Elem>

///////////////////////////////////////////////////////////////////////////
// refiners
///////////////////////////////////////////////////////////////////////////


//internal interface MessageRefiner : Processor<MessageRefinerContext>
//
//internal interface MessageRefinerContext : ProcessorPipelineContext<SingleMessage, Message?> {
//    /**
//     * Refine if possible (without suspension), returns self otherwise.
//     * @since 2.6
//     */ // see #1157
//    fun tryRefine(
//        bot: Bot,
//        context: MessageChain,
//        refineContext: RefineContext = EmptyRefineContext,
//    ): Message? = this
//
//    /**
//     * This message [RefinableMessage] will be replaced by return value of [refineLight]
//     */
//    suspend fun refine(
//        bot: Bot,
//        context: MessageChain,
//        refineContext: RefineContext = EmptyRefineContext,
//    ): Message? = tryRefine(bot, context, refineContext)
//}
//
