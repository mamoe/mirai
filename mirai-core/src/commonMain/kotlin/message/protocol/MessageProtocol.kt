/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.pipeline.Processor
import net.mamoe.mirai.internal.pipeline.ProcessorPipeline
import net.mamoe.mirai.internal.pipeline.ProcessorPipelineContext
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.uncheckedCast
import java.util.*
import kotlin.reflect.KClass

internal abstract class ProcessorCollector {
    inline fun <reified T : SingleMessage> add(encoder: MessageEncoder<T>) = add(encoder, T::class)


    abstract fun <T : SingleMessage> add(encoder: MessageEncoder<T>, elementType: KClass<T>)

    abstract fun add(decoder: MessageDecoder)
}

internal abstract class MessageProtocol {
    fun collectProcessors(processorCollector: ProcessorCollector) {
        processorCollector.collectProcessorsImpl()
    }

    protected abstract fun ProcessorCollector.collectProcessorsImpl()
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

}

internal interface MessageDecoder {
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
    }
}

internal interface MessageDecoderPipeline : ProcessorPipeline<MessageDecoderProcessor, ImMsgBody.Elem, Message>

///////////////////////////////////////////////////////////////////////////
// encoders
///////////////////////////////////////////////////////////////////////////

internal interface MessageEncoderContext : ProcessorPipelineContext<SingleMessage, ImMsgBody.Elem> {

}


internal interface MessageEncoder<T : SingleMessage> {
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
