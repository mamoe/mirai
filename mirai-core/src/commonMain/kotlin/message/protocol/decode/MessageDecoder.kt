/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.decode

import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.pipeline.PipelineConsumptionMarker
import net.mamoe.mirai.internal.pipeline.Processor


internal interface MessageDecoder : PipelineConsumptionMarker {
    suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem)
}

/**
 * Adapter for [MessageDecoder] to be used as [Processor].
 */
internal class MessageDecoderProcessor(
    private val decoder: MessageDecoder,
) : Processor<MessageDecoderContext, ImMsgBody.Elem> {
    override val origin: Any get() = this

    override suspend fun process(context: MessageDecoderContext, data: ImMsgBody.Elem) {
        @Suppress("ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL")
        decoder.run { context.process(data) }
        // TODO: 2022/4/27 handle exceptions
    }

    override fun toString(): String {
        return "MessageDecoderProcessor(decoder=$decoder)"
    }
}