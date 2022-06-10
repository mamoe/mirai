/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.outgoing

import net.mamoe.mirai.internal.pipeline.Processor

internal sealed interface OutgoingMessagePipelineProcessor :
    Processor<OutgoingMessagePipelineContext, OutgoingMessagePipelineInput>

/**
 * Adapter for [OutgoingMessageProcessor] to be used as [Processor].
 */
internal class OutgoingMessageProcessorAdapter(
    private val processor: OutgoingMessageProcessor,
) : OutgoingMessagePipelineProcessor {
    override val origin: OutgoingMessageProcessor get() = processor

    override suspend fun process(context: OutgoingMessagePipelineContext, data: OutgoingMessagePipelineInput) {
        processor.run { context.process() }
    }

    override fun toString(): String {
        return "OutgoingMessageProcessorAdapter(transformer=$processor)"
    }
}