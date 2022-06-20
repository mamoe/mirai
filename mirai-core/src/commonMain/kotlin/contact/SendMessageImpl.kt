/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("RESULT_CLASS_IN_RETURN_TYPE") // inline ABI not stable but we don't care about internal ABI

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.MessagePostSendEvent
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.internal.message.flags.MiraiInternalMessageFlag
import net.mamoe.mirai.internal.message.protocol.MessageProtocolFacade
import net.mamoe.mirai.internal.message.protocol.outgoing.HighwayUploader
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.network.component.buildComponentStorage
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import kotlin.coroutines.CoroutineContext

internal suspend fun <C : AbstractContact> C.sendMessageImpl(
    message: Message,
    messageProtocolStrategy: MessageProtocolStrategy<C>,
    preSendEventConstructor: (C, Message, CoroutineContext) -> MessagePreSendEvent,
    postSendEventConstructor: (C, MessageChain, Throwable?, MessageReceipt<C>?, CoroutineContext) -> MessagePostSendEvent<C>,
): MessageReceipt<C> {
    val ctx = coroutineContext

    val isMiraiInternal = if (message is MessageChain) {
        message.anyIsInstance<MiraiInternalMessageFlag>()
    } else false

    require(!message.isContentEmpty()) { "message is empty" }

    val chain = broadcastMessagePreSendEvent(message, isMiraiInternal, ctx, preSendEventConstructor)

    val result = kotlin.runCatching {
        MessageProtocolFacade.preprocessAndSendOutgoing(this, message, buildComponentStorage {
            set(MessageProtocolStrategy, messageProtocolStrategy)
            set(HighwayUploader, HighwayUploader.Default)
            set(ClockHolder, bot.components[ClockHolder])
        })
    }

    if (result.isSuccess) {
        // logMessageSent(result.getOrNull()?.source?.plus(chain) ?: chain) // log with source
        bot.logger.verbose("$this <- $chain".replaceMagicCodes())
    }

    if (!isMiraiInternal) {
        postSendEventConstructor(this, chain, result.exceptionOrNull(), result.getOrNull(), ctx).broadcast()
    }

    return result.getOrThrow()
}

/**
 * Called only in 'public' apis.
 */
internal suspend fun <C : Contact> C.broadcastMessagePreSendEvent(
    message: Message,
    isMiraiInternal: Boolean,
    coroutineContext: CoroutineContext,
    eventConstructor: (C, Message, CoroutineContext) -> MessagePreSendEvent,
): MessageChain {
    if (isMiraiInternal) return message.toMessageChain()
    var eventName: String? = null
    return kotlin.runCatching {
        eventConstructor(this, message, coroutineContext).also {
            eventName = it.javaClass.simpleName
        }.broadcast()
    }.onSuccess {
        check(!it.isCancelled) {
            throw EventCancelledException("cancelled by $eventName")
        }
    }.getOrElse {
        eventName = eventName ?: (this@broadcastMessagePreSendEvent.javaClass.simpleName + "MessagePreSendEvent")
        throw EventCancelledException("exception thrown when broadcasting $eventName", it)
    }.message.toMessageChain()
}


internal enum class SendMessageStep(
    val allowMultiplePackets: Boolean
) {
    /**
     * 尝试单包直接发送全部消息
     */
    FIRST(false) {
        override fun nextStepOrNull(): SendMessageStep {
            return LONG_MESSAGE
        }
    },

    /**
     * 尝试通过长消息通道上传长消息取得 resId 后再通过普通消息通道发送长消息标识
     */
    LONG_MESSAGE(false) {
        override fun nextStepOrNull(): SendMessageStep {
            return FRAGMENTED
        }
    },

    /**
     * 发送分片多包发送
     */
    FRAGMENTED(true) {
        override fun nextStepOrNull(): SendMessageStep? {
            return null
        }
    };

    abstract fun nextStepOrNull(): SendMessageStep?
}
