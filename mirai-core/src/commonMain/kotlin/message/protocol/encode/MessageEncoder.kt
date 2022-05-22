/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.encode

import net.mamoe.mirai.internal.pipeline.PipelineConsumptionMarker
import net.mamoe.mirai.internal.pipeline.Processor
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.utils.uncheckedCast
import kotlin.reflect.KClass

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
    override val origin: Any get() = this

    override suspend fun process(context: MessageEncoderContext, data: SingleMessage) {
        if (elementType.isInstance(data)) {
            @Suppress("ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL")
            encoder.run { context.process(data.uncheckedCast()) }
            // TODO: 2022/4/27 handle exceptions
        }
    }
}