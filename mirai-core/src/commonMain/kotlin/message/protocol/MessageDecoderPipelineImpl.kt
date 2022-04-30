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
import net.mamoe.mirai.internal.pipeline.AbstractProcessorPipeline
import net.mamoe.mirai.internal.pipeline.PipelineConfiguration
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.*


internal open class MessageDecoderPipelineImpl :
    AbstractProcessorPipeline<MessageDecoderProcessor, MessageDecoderContext, ImMsgBody.Elem, Message>(
        PipelineConfiguration(stopWhenConsumed = true),
        @OptIn(TestOnly::class)
        defaultTraceLogging
    ),
    MessageDecoderPipeline {

    inner class MessageDecoderContextImpl(attributes: TypeSafeMap) : MessageDecoderContext, BaseContextImpl(attributes)

    override fun createContext(attributes: TypeSafeMap): MessageDecoderContext = MessageDecoderContextImpl(attributes)

    companion object {
        @TestOnly
        val defaultTraceLogging: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.Factory.create(MessageDecoderPipelineImpl::class, "MessageDecoderPipeline")
                .withSwitch(systemProp("mirai.message.decoder.pipeline.log.full", false))
        }
    }
}