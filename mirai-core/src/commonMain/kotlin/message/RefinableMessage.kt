/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineMessageSource
import net.mamoe.mirai.internal.message.flags.InternalFlagOnlyMessage
import net.mamoe.mirai.internal.message.source.IncomingMessageSourceInternal
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.safeCast

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
        refineAction: (message: RefinableMessage) -> Message?,
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
    val name: String?,
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

    internal companion object {
        val MessageSourceKind = RefineContextKey<MessageSourceKind>("MessageSourceKind")
        val FromId = RefineContextKey<Long>("FromId")
        val GroupIdOrZero = RefineContextKey<Long>("GroupIdOrZero")
    }
}

/**
 * 转换消息时的上下文
 */
internal interface RefineContext {
    operator fun contains(key: RefineContextKey<*>): Boolean
    operator fun <T : Any> get(key: RefineContextKey<T>): T?
    fun <T : Any> getNotNull(key: RefineContextKey<T>): T = get(key) ?: error("No such value of `$key`")
    fun merge(other: RefineContext, override: Boolean): RefineContext
    fun entries(): Set<Pair<RefineContextKey<*>, Any>>
}

internal interface MutableRefineContext : RefineContext {
    operator fun <T : Any> set(key: RefineContextKey<T>, value: T)
    fun remove(key: RefineContextKey<*>)
}

internal object EmptyRefineContext : RefineContext {
    override fun contains(key: RefineContextKey<*>): Boolean = false
    override fun <T : Any> get(key: RefineContextKey<T>): T? = null
    override fun merge(other: RefineContext, override: Boolean): RefineContext {
        return other
    }
    override fun entries(): Set<Pair<RefineContextKey<*>, Any>> {
        return emptySet()
    }
    override fun toString(): String {
        return "EmptyRefineContext"
    }

    override fun equals(other: Any?): Boolean {
        return other === EmptyRefineContext
    }
}

@Suppress("UNCHECKED_CAST")
internal class SimpleRefineContext(
    private val delegate: MutableMap<RefineContextKey<*>, Any> = mutableMapOf(),
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

    override fun entries(): Set<Pair<RefineContextKey<*>, Any>> {
        return delegate.entries.map { (k, v) -> k to v }.toSet()
    }

    override fun merge(other: RefineContext, override: Boolean): RefineContext {
        val new = SimpleRefineContext(*entries().toTypedArray())
        other.entries().forEach { (key, value) ->
            if (new[key] == null || override) {
                new[key as RefineContextKey<Any>] = value
            }
        }
        return new
    }

    override fun equals(other: Any?): Boolean {
        if (other !is RefineContext) return false
        if (other === this) return true

        return other.entries() == entries()
    }
}

internal fun SimpleRefineContext(vararg elements: Pair<RefineContextKey<*>, Any>): SimpleRefineContext =
    SimpleRefineContext(elements.toMap().toMutableMap())

/**
 * 执行不需要 `suspend` 的 refine. 用于 [MessageSource.originalMessage].
 */
internal object LightMessageRefiner : MessageRefiner() {
    /* note:
     * 不在 refineLight 中处理的原因是 refineMessageSource
     * 需要的是 **最终处理完成后** 的 MessageChain (即 refineDeep 后的 MessageChain)
     *
     * 在 refineLight/RefinableMessage.(try)refine 中直接处理将导致获取不到最终结果导致逻辑错误
     */
    fun MessageChain.refineMessageSource(): MessageChain {
        val source = this.sourceOrNull?.safeCast<IncomingMessageSourceInternal>() ?: return this
        val originalMessage = this
        source.originalMessageLazy = lazy {
            originalMessage.filterNot { it is MessageSource }.toMessageChain()
//            @Suppress("INVISIBLE_MEMBER")
//            createMessageChainImplOptimized(originalMessage.filterNot { it is MessageSource })
        }
        return this
    }

    fun MessageChain.refineLight(
        bot: Bot,
        refineContext: RefineContext = EmptyRefineContext,
    ): MessageChain {
        return refineImpl(bot) { it.tryRefine(bot, this, refineContext) }
    }

    /**
     * 去除 [MessageChain] 携带的内部标识
     *
     * 用于 [createMessageReceipt] <- `RemoteFile.uploadAndSend` (文件操作API v1)
     */
    fun MessageChain.dropMiraiInternalFlags(): MessageChain {
        return asSequence().filterNot { it is InternalFlagOnlyMessage }.toMessageChain()
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
            .refineMessageSource()
    }
}