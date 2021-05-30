/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.message.data.*

/**
 * 在接收解析消息后会经过一层转换的消息.
 *
 * @see DeepMessageRefiner.refineDeep
 * @see LightMessageRefiner.refineLight
 */
internal interface RefinableMessage : SingleMessage {

    /**
     * Refine if possible (without suspension), returns self otherwise.
     * @since 2.6
     */ // see #1157
    fun tryRefine(
        bot: Bot,
        context: MessageChain,
        refineContext: RefineContext = EmptyRefineContext,
    ): Message? = this

    /**
     * This message [RefinableMessage] will be replaced by return value of [refineLight]
     */
    suspend fun refine(
        bot: Bot,
        context: MessageChain,
        refineContext: RefineContext = EmptyRefineContext,
    ): Message? = tryRefine(bot, context, refineContext)
}

internal sealed class MessageRefiner {
    protected inline fun MessageChain.refineImpl(
        bot: Bot,
        refineAction: (message: RefinableMessage) -> Message?
    ): MessageChain {
        val convertLineSeparator = bot.configuration.convertLineSeparator

        if (none {
                it is RefinableMessage
                        || (it is PlainText && convertLineSeparator && it.content.contains('\r'))
            }
        ) return this


        val builder = MessageChainBuilder(this.size)
        for (singleMessage in this) {
            if (singleMessage is RefinableMessage) {
                val v = refineAction(singleMessage)
                if (v != null) builder.add(v)
            } else if (singleMessage is PlainText && convertLineSeparator) {
                val content = singleMessage.content
                if (content.contains('\r')) {
                    builder.add(
                        PlainText(
                            content
                                .replace("\r\n", "\n")
                                .replace('\r', '\n')
                        )
                    )
                } else {
                    builder.add(singleMessage)
                }
            } else {
                builder.add(singleMessage)
            }
        }
        return builder.build()
    }
}

@Suppress("unused")
internal class RefineContextKey<T : Any>(
    val name: String?
) {
    override fun toString(): String {
        return buildString {
            append("Key(")
            name?.also(this@buildString::append) ?: kotlin.run {
                append('#').append(this@RefineContextKey.hashCode())
            }
            append(')')
        }
    }
}

/**
 * 转换消息时的上下文
 */
internal interface RefineContext {
    operator fun contains(key: RefineContextKey<*>): Boolean
    operator fun <T : Any> get(key: RefineContextKey<T>): T?
    fun <T : Any> getNotNull(key: RefineContextKey<T>): T = get(key) ?: error("No such value of `$key`")
}

internal interface MutableRefineContext : RefineContext {
    operator fun <T : Any> set(key: RefineContextKey<T>, value: T)
    fun remove(key: RefineContextKey<*>)
}

internal object EmptyRefineContext : RefineContext {
    override fun contains(key: RefineContextKey<*>): Boolean = false
    override fun <T : Any> get(key: RefineContextKey<T>): T? = null
    override fun toString(): String {
        return "EmptyRefineContext"
    }
}

@Suppress("UNCHECKED_CAST")
internal class SimpleRefineContext(
    private val delegate: MutableMap<RefineContextKey<*>, Any> = mutableMapOf()
) : MutableRefineContext {

    override fun contains(key: RefineContextKey<*>): Boolean = delegate.containsKey(key)
    override fun <T : Any> get(key: RefineContextKey<T>): T? {
        return (delegate[key] ?: return null) as T
    }

    override fun <T : Any> set(key: RefineContextKey<T>, value: T) {
        delegate[key] = value
    }

    override fun remove(key: RefineContextKey<*>) {
        delegate.remove(key)
    }
}

/**
 * 执行不需要 `suspend` 的 refine. 用于 [MessageSource.originalMessage].
 */
internal object LightMessageRefiner : MessageRefiner() {
    fun MessageChain.refineLight(
        bot: Bot,
        refineContext: RefineContext = EmptyRefineContext,
    ): MessageChain {
        return refineImpl(bot) { it.tryRefine(bot, this, refineContext) }
    }
}

/**
 * 执行需要 `suspend` 的 refine. 用于解析到的消息.
 */
internal object DeepMessageRefiner : MessageRefiner() {
    suspend fun MessageChain.refineDeep(
        bot: Bot,
        refineContext: RefineContext = EmptyRefineContext,
    ): MessageChain {
        return refineImpl(bot) { it.refine(bot, this, refineContext) }
    }
}