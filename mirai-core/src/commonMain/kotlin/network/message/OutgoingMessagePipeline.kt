/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.broadcastMessagePreSendEvent
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_FRAGMENTED
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_LONG
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_SIMPLE
import net.mamoe.mirai.internal.network.notice.BotAware
import net.mamoe.mirai.internal.network.pipeline.PipelineConfiguration
import net.mamoe.mirai.internal.network.pipeline.PipelineContext
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Steps:
 *
 * 1. Preconditions - Check length
 * 2. PreSendEvent - [Contact.broadcastMessagePreSendEvent]
 * 3. Logging - Log sent
 * 4. Pre transformation
 * 5. Transform special elements
 * 6. Fix images
 * 7. Convert to long message
 * 8. Ensure sequence id available
 * 9. Post transformation
 * 10. Send packet
 *
 * @since 2.8-M1
 */
internal interface OutgoingMessagePipeline {
    suspend fun <C : AbstractContact> sendMessage(contact: C, message: MessageChain): MessageReceipt<C>

    companion object : ComponentKey<OutgoingMessagePipeline>
}

internal class OutgoingMessagePipelineImpl(
    private val pipelineConfigurations: Map<KClass<out AbstractContact>, MessagePipelineConfiguration<out AbstractContact>>, // must be exhaustive ---- covering all AbstractContact
) : OutgoingMessagePipeline {
    override suspend fun <C : AbstractContact> sendMessage(contact: C, message: MessageChain): MessageReceipt<C> {
        val context = MessagePipelineContextImpl<AbstractContact>(contact)
        return pipelineConfigurations[contact::class]?.execute(context, message)?.cast()
            ?: error("Internal error: Could")
    }
}

internal typealias MessagePipelineConfiguration<T> = PipelineConfiguration<MessagePipelineContext<T>, MessageChain, MessageReceipt<T>>

internal interface MessagePipelineContext<out C : AbstractContact> : PipelineContext, BotAware, CoroutineScope {
    val contact: C
    val time: TimeSource

    override val bot get() = contact.bot

    companion object {
        @JvmField
        val KEY_CAN_SEND_AS_SIMPLE = TypeKey<Boolean>("canSendAsSimple")

        @JvmField
        val KEY_CAN_SEND_AS_LONG = TypeKey<Boolean>("canSendAsLong")

        @JvmField
        val KEY_CAN_SEND_AS_FRAGMENTED = TypeKey<Boolean>("canSendAsFragmented")

        @JvmField
        val KEY_ORIGINAL_MESSAGE = TypeKey<MessageChain>("originalMessage")

        @JvmField
        val KEY_FINAL_MESSAGE_CHAIN = TypeKey<MessageChain>("finalMessageChain")

        @JvmField
        val KEY_MESSAGE_SOURCE_RESULT = TypeKey<Deferred<OnlineMessageSource.Outgoing>>("messageSourceResult")
    }
}

internal typealias MessagePipelineContextRaw = MessagePipelineContext<AbstractContact>

internal class MessagePipelineContextImpl<out C : AbstractContact>(
    override val contact: C,
    override val coroutineContext: CoroutineContext = contact.coroutineContext.childScopeContext()
        .addNameHierarchically("MessagePipelineContext"),
    override val logger: MiraiLogger = contact.bot.logger.subLogger("MessagePipelineContext"), // TODO: 2021/8/15 use contact's logger
    override val attributes: MutableTypeSafeMap = buildTypeSafeMap {
        set(KEY_CAN_SEND_AS_FRAGMENTED, true)
        set(KEY_CAN_SEND_AS_LONG, true)
        set(KEY_CAN_SEND_AS_SIMPLE, true)
    },
    override val exceptionCollector: ExceptionCollector = ExceptionCollector(),
    override val time: TimeSource = TimeSource.System
) : MessagePipelineContext<C>