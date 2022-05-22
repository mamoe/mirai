/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.outgoing

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.pipeline.PipelineConsumptionMarker
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain

/**
 * A handler that is responsible for some logic in sending a [MessageChain] to a target [Contact].
 *
 * Broadcasting events is not of responsibility of [OutgoingMessageProcessor].
 */
internal sealed interface OutgoingMessageProcessor {
    suspend fun OutgoingMessagePipelineContext.process()
}

/**
 * A preprocessor will be called in the first place.
 *
 * It is designed to do type conversions, and pre-conditional checks.
 *
 * **Preprocessors are not called in nested processing.**
 */
internal fun interface OutgoingMessagePreprocessor : OutgoingMessageProcessor


/**
 * A transformer will be called after [Preprocessors][OutgoingMessagePreprocessor] and before [Postprocessors][OutgoingMessagePostprocessor].
 *
 * It is capable for handling some special messages, e.g. transform long messages as [LongMessageInternal].
 */
internal fun interface OutgoingMessageTransformer : OutgoingMessageProcessor

/**
 * A transformer will be called after [Preprocessors][OutgoingMessagePreprocessor] and before [Postprocessors][OutgoingMessagePostprocessor].
 *
 * It is capable for sending packets, and create [MessageReceipt].
 * Senders can finish the pipeline by [OutgoingMessagePipelineContext.markAsConsumed].
 */
internal fun interface OutgoingMessageSender : OutgoingMessageProcessor, PipelineConsumptionMarker

/**
 * A postprocessor will be called in the last place.
 *
 * It is designed to do cleanup.
 *
 * Note that if an exception was thrown by previous processors, postprocessors may not be called,
 * so do not reply on the postprocessor to close resources.
 */
internal fun interface OutgoingMessagePostprocessor : OutgoingMessageProcessor

//internal interface MessageRefiner
//
//internal fun interface DeepMessageRefiner : MessageRefiner, OutgoingMessageProcessor
//
//@RestrictsSuspension // only allowed to call `processAlso`
//internal fun interface LightMessageRefiner : MessageRefiner {
//    suspend fun OutgoingMessagePipelineContext.process()
//}
