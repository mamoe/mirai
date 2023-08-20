/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.decode

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.message.protocol.decode.MessageDecoderContext.Companion.CONTAINING_MSG
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.pipeline.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.utils.*
import kotlin.coroutines.RestrictsSuspension

internal interface MessageDecoderPipeline :
    ProcessorPipeline<MessageDecoderProcessor, MessageDecoderContext, ImMsgBody.Elem, Message>

@RestrictsSuspension // Implementor can only call `MessageDecoderContext.process` and `processAlso` so there will be no suspension point
internal interface MessageDecoderContext : ProcessorPipelineContext<ImMsgBody.Elem, Message> {
    companion object {
        val BOT = TypeKey<Bot>("bot")
        val MESSAGE_SOURCE_KIND = TypeKey<MessageSourceKind>("messageSourceKind")
        val GROUP_ID = TypeKey<Long>("groupId") // zero if not group
        val CONTAINING_MSG = TypeKey<MsgComm.Msg?>("containingMsg")
        val FROM_ID = TypeKey<Long>("fromId") // group/temp = sender, friend/stranger = this
    }
}

internal open class MessageDecoderPipelineImpl :
    AbstractProcessorPipeline<MessageDecoderProcessor, MessageDecoderContext, ImMsgBody.Elem, Message>(
        PipelineConfiguration(stopWhenConsumed = true),
        @OptIn(TestOnly::class)
        defaultTraceLogging
    ),
    MessageDecoderPipeline {

    inner class MessageDecoderContextImpl(attributes: TypeSafeMap) : MessageDecoderContext, BaseContextImpl(attributes)

    override fun createContext(data: ImMsgBody.Elem, attributes: TypeSafeMap): MessageDecoderContext =
        MessageDecoderContextImpl(attributes)

    override suspend fun process(
        data: ImMsgBody.Elem,
        context: MessageDecoderContext,
        attributes: TypeSafeMap
    ): ProcessResult<MessageDecoderContext, Message> {
        context.attributes[CONTAINING_MSG]?.let { msg ->
            traceLogging.info { "Processing MsgCommon.Msg: ${msg.structureToStringAndDesensitizeIfAvailable()}" }
        }
        return super.process(data, context, attributes)
    }

    companion object {
        @TestOnly
        val defaultTraceLogging: MiraiLoggerWithSwitch by lazy {
            MiraiLogger.Factory.create(MessageDecoderPipelineImpl::class, "MessageDecoderPipeline")
                .withSwitch(systemProp("mirai.message.decoder.pipeline.log.full", false))
        }
    }
}