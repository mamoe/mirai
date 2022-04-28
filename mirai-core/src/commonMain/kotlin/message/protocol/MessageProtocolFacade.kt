/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.message.data.SingleMessage
import java.util.*
import kotlin.reflect.KClass

internal interface MessageProtocolFacade {
    val encoderPipeline: MessageEncoderPipeline
    val decoderPipeline: MessageDecoderPipeline
    val loaded: List<MessageProtocol>

    companion object INSTANCE : MessageProtocolFacade by MessageProtocolFacadeImpl()
}

internal class MessageProtocolFacadeImpl : MessageProtocolFacade {
    override val encoderPipeline: MessageEncoderPipeline = MessageEncoderPipelineImpl()
    override val decoderPipeline: MessageDecoderPipeline = MessageDecoderPipelineImpl()

    override val loaded: List<MessageProtocol> = initialize()

    private fun initialize(): List<MessageProtocol> {
        val instances = ServiceLoader.load(MessageProtocol::class.java).iterator().asSequence()
            .toCollection(PriorityQueue(MessageProtocol.PriorityComparator.reversed()))

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

        return instances.toList()
    }
}
