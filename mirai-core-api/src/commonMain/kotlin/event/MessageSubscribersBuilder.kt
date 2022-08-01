/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "unused", "DSL_SCOPE_VIOLATION_WARNING", "INAPPLICABLE_JVM_NAME", "INVALID_CHARACTERS",
    "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
)

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.event.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic


/**
 * 消息事件的处理器.
 *
 * 注:
 * 接受者 T 为 [MessageEvent]
 * 参数 String 为 转为字符串了的消息 ([Message.toString])
 */
public typealias MessageListener<T, R> = @MessageDsl suspend T.(String) -> R


/**
 * 消息订阅构造器
 *
 * @param M 消息类型
 * @param R 消息监听器内部的返回值
 * @param Ret 每个 DSL 函数创建监听器之后的返回值
 *
 * @see EventChannel.subscribeMessages
 */
@MessageDsl
public open class MessageSubscribersBuilder<M : MessageEvent, out Ret, R : RR, RR> internal constructor(
    /**
     * 用于 [MessageListener] 无返回值的替代.
     */
    @PublishedApi
    internal val stub: RR,
    /**
     * invoke 这个 lambda 时, 它将会把 [消息事件的处理器][MessageListener] 注册给事件, 并返回注册完成返回的监听器.
     */
    public val subscriber: (M.(String) -> Boolean, MessageListener<M, RR>) -> Ret
) {
    @RequiresOptIn(level = RequiresOptIn.Level.ERROR)
    @Target(CONSTRUCTOR)
    private annotation class UseNewListenerFilterInstead

    @OptIn(UseNewListenerFilterInstead::class)
    public open fun newListeningFilter(filter: M.(String) -> Boolean): ListeningFilter = ListeningFilter(filter)

    /**
     * 由 [contains], [startsWith] 等 DSL 创建出的监听条件, 使用 [invoke] 将其注册给事件
     */
    public inner class ListeningFilter @UseNewListenerFilterInstead internal constructor(
        public val filter: M.(String) -> Boolean
    ) {
        /** 进行逻辑 `or`. */
        public infix fun or(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) || another.filter.invoke(this, it) }

        /** 进行逻辑 `and`. */
        public infix fun and(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) && another.filter.invoke(this, it) }

        /** 进行逻辑 `xor`. */
        public infix fun xor(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) xor another.filter.invoke(this, it) }

        /** 进行逻辑 `nand`, 即 `not and`. */
        public infix fun nand(another: ListeningFilter): ListeningFilter =
            newListeningFilter { !filter.invoke(this, it) || !another.filter.invoke(this, it) }

        /** 进行逻辑 `not` */
        public fun not(): ListeningFilter = newListeningFilter { !filter.invoke(this, it) }

        /** 启动事件监听. */
        // do not inline due to kotlin (1.3.61) bug: java.lang.IllegalAccessError
        public operator fun invoke(onEvent: MessageListener<M, R>): Ret = content(filter, onEvent)
    }

    /** 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时回复原消息 */
    @MessageDsl
    public open infix fun ListeningFilter.reply(toReply: String): Ret =
        content(filter) { if ((this as? GroupMessageEvent)?.group?.isBotMuted != true) subject.sendMessage(toReply);this@MessageSubscribersBuilder.stub }


    /** 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时回复原消息 */
    @MessageDsl
    public open infix fun ListeningFilter.reply(message: Message): Ret =
        content(filter) { if ((this as? GroupMessageEvent)?.group?.isBotMuted != true) subject.sendMessage(message);this@MessageSubscribersBuilder.stub }

    /**
     * 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时执行 [replier] 并以其返回值回复.
     * 返回值 [Unit] 将被忽略, [Message] 将被直接回复, 其他内容将会 [Any.toString] 后发送.
     */
    @MessageDsl
    public open infix fun ListeningFilter.reply(
        replier: (@MessageDsl suspend M.(String) -> Any?)
    ): Ret =
        content(filter) {
            if ((this as? GroupMessageEvent)?.group?.isBotMuted != true)
                this@MessageSubscribersBuilder.executeAndReply(this, replier)
            else this@MessageSubscribersBuilder.stub
        }

    /** 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时引用回复原消息 */
    @MessageDsl
    public open infix fun ListeningFilter.quoteReply(toReply: String): Ret =
        content(filter) { if ((this as? GroupMessageEvent)?.group?.isBotMuted != true) subject.sendMessage(message.quote() + toReply); this@MessageSubscribersBuilder.stub }

    /** 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时引用回复原消息 */
    @MessageDsl
    public open infix fun ListeningFilter.quoteReply(toReply: Message): Ret =
        content(filter) { if ((this as? GroupMessageEvent)?.group?.isBotMuted != true) subject.sendMessage(message.quote() + toReply);this@MessageSubscribersBuilder.stub }

    /**
     * 启动监听器, 在 [Bot] 未被禁言且消息满足条件 [this] 时执行 [replier] 并以其返回值回复原消息
     * 返回值 [Unit] 将被忽略, [Message] 将被直接回复, 其他内容将会 [Any.toString] 后发送
     */
    @MessageDsl
    public open infix fun ListeningFilter.quoteReply(replier: (@MessageDsl suspend M.(String) -> Any?)): Ret =
        content(filter) {
            if ((this as? GroupMessageEvent)?.group?.isBotMuted != true)
                this@MessageSubscribersBuilder.executeAndQuoteReply(this, replier)
            else this@MessageSubscribersBuilder.stub
        }

    /** 无触发条件, 每次收到消息都执行 [onEvent] */
    @MessageDsl
    public open fun always(onEvent: MessageListener<M, RR>): Ret = subscriber({ true }, onEvent)

    /** [消息内容][Message.contentToString] `==` [equals] */
    @MessageDsl
    public fun case(equals: String, ignoreCase: Boolean = false, trim: Boolean = true): ListeningFilter =
        caseImpl(equals, ignoreCase, trim)

    /** 如果[消息内容][Message.contentToString]  `==` [equals] */
    @MessageDsl
    public operator fun String.invoke(block: MessageListener<M, R>): Ret = case(this, onEvent = block)

    /** 如果[消息内容][Message.contentToString]  [matches] */
    @MessageDsl
    @JvmSynthetic
    @JvmName("matchingExtension")
    public infix fun Regex.matching(block: MessageListener<M, R>): Ret = content({ it matches this@matching }, block)

    /** 如果[消息内容][Message.contentToString] [Regex.find] 不为空 */
    @MessageDsl
    @JvmSynthetic
    @JvmName("findingExtension")
    public infix fun Regex.finding(block: @MessageDsl suspend M.(MatchResult) -> R): Ret =
        always { content -> this@finding.find(content)?.let { block(this, it) } ?: this@MessageSubscribersBuilder.stub }

    /**
     * [消息内容][Message.contentToString] `==` [equals]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
    @MessageDsl
    public fun case(
        equals: String, ignoreCase: Boolean = false, trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret = (if (trim) equals.trim() else equals).let { toCheck ->
        content({ (if (trim) it.trim() else it).equals(toCheck, ignoreCase = ignoreCase) }) {
            onEvent(this, this.message.contentToString())
        }
    }

    /** [消息内容][Message.contentToString]包含 [sub] */
    @MessageDsl
    @JvmOverloads // bin comp
    public fun contains(sub: String, ignoreCase: Boolean = false): ListeningFilter =
        content { it.contains(sub, ignoreCase) }

    /**
     * [消息内容][Message.contentToString]包含 [sub] 中的任意一个元素
     */
    @MessageDsl
    public fun contains(
        sub: String, ignoreCase: Boolean = false, trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret = containsImpl(sub, ignoreCase, trim, onEvent)

    /** [消息内容][Message.contentToString]包含 [sub] */
    @JvmOverloads
    @MessageDsl
    public fun containsAny(vararg sub: String, ignoreCase: Boolean = false, trim: Boolean = true): ListeningFilter =
        containsAnyImpl(*sub, ignoreCase = ignoreCase, trim = trim)

    /** [消息内容][Message.contentToString]包含 [sub] */
    @JvmOverloads
    @MessageDsl
    public fun containsAll(vararg sub: String, ignoreCase: Boolean = false, trim: Boolean = true): ListeningFilter =
        containsAllImpl(sub, ignoreCase = ignoreCase, trim = trim)


    /** 如果消息的前缀是 [prefix] */
    @MessageDsl
    public fun startsWith(prefix: String, trim: Boolean = true): ListeningFilter {
        val toCheck = if (trim) prefix.trimStart() else prefix
        return content { (if (trim) it.trimStart() else it).startsWith(toCheck) }
    }

    /** 如果消息的前缀是 [prefix] */
    @MessageDsl
    public fun startsWith(
        prefix: String, removePrefix: Boolean = true, trim: Boolean = true,
        onEvent: @MessageDsl suspend M.(String) -> R
    ): Ret = startsWithImpl(prefix, removePrefix, trim, onEvent)

    /** 如果消息的结尾是 [suffix] */
    @MessageDsl
    @JvmOverloads // for binary compatibility
    public fun endsWith(suffix: String, trim: Boolean = true): ListeningFilter {
        val toCheck = if (trim) suffix.trimEnd() else suffix
        return content { (if (trim) it.trimEnd() else it).endsWith(toCheck) }
    }

    /** 如果消息的结尾是 [suffix] */
    @MessageDsl
    public fun endsWith(
        suffix: String, removeSuffix: Boolean = true, trim: Boolean = true,
        onEvent: @MessageDsl suspend M.(String) -> R
    ): Ret = endsWithImpl(suffix, removeSuffix, trim, onEvent)

    /** 如果是这个人发的消息. 消息目前只会是群消息 */
    @MessageDsl
    public fun sentBy(name: String): ListeningFilter = content { this is GroupMessageEvent && this.senderName == name }

    /** 如果是这个人发的消息. 消息可以是好友消息也可以是群消息 */
    @MessageDsl
    public fun sentBy(qq: Long): ListeningFilter = content { sender.id == qq }

    /** 如果是这个人发的消息. 消息可以是好友消息也可以是群消息 */
    @MessageDsl
    public fun sentBy(friend: User): ListeningFilter = content { sender.id == friend.id }

    /** 如果是这个人发的消息. 消息可以是好友消息也可以是群消息 */
    @MessageDsl
    public fun sentBy(qq: Long, onEvent: MessageListener<M, R>): Ret = content { this.sender.id == qq }.invoke(onEvent)

    /** 如果是好友发来的消息 */
    @MessageDsl
    public fun sentByFriend(onEvent: MessageListener<FriendMessageEvent, R>): Ret =
        content({ this is FriendMessageEvent }) { onEvent(this as FriendMessageEvent, it) }

    /** 如果是好友发来的消息 */
    @MessageDsl
    public fun sentByFriend(): ListeningFilter = newListeningFilter { this is FriendMessageEvent }

    /** 如果是陌生人发来的消息 */
    @MessageDsl
    public fun sentByStranger(onEvent: MessageListener<StrangerMessageEvent, R>): Ret =
        content({ this is StrangerMessageEvent }) { onEvent(this as StrangerMessageEvent, it) }

    /** 如果是陌生人发来的消息 */
    @MessageDsl
    public fun sentByStranger(): ListeningFilter = newListeningFilter { this is StrangerMessageEvent }

    /** 如果是群临时会话消息 */
    @MessageDsl
    @Deprecated("use sentByGroupTemp()", ReplaceWith("sentByGroupTemp()"), DeprecationLevel.HIDDEN)
    @DeprecatedSinceMirai(hiddenSince = "2.0") // maybe 2.0
    public fun sentByTemp(): ListeningFilter = sentByGroupTemp()

    /** 如果是群临时会话消息 */
    @MessageDsl
    public fun sentByGroupTemp(): ListeningFilter = newListeningFilter { this is GroupTempMessageEvent }

    /** 如果是管理员或群主发的消息 */
    @MessageDsl
    public fun sentByOperator(): ListeningFilter =
        content { this is GroupMessageEvent && sender.permission.isOperator() }

    /** 如果是管理员发的消息 */
    @MessageDsl
    public fun sentByAdministrator(): ListeningFilter =
        content { this is GroupMessageEvent && sender.permission.isAdministrator() }

    /** 如果是群主发的消息 */
    @MessageDsl
    public fun sentByOwner(): ListeningFilter = content { this is GroupMessageEvent && sender.isOwner() }

    /** 如果是来自这个群的消息 */
    @MessageDsl
    public fun sentFrom(groupId: Long): ListeningFilter = content { this is GroupMessageEvent && group.id == groupId }

    /** 如果是来自这个群的消息 */
    @MessageDsl
    public fun sentFrom(group: Group): ListeningFilter = content { this is GroupMessageEvent && group.id == group.id }

    /** [消息内容][Message.contentToString]包含目标为 [Bot] 的 [At] */
    @MessageDsl
    public fun atBot(): ListeningFilter = content { message.firstIsInstanceOrNull<At>()?.target == bot.id }

    /** [消息内容][Message.contentToString]包含 [AtAll] */
    @MessageDsl
    public fun atAll(): ListeningFilter = content { message.firstIsInstanceOrNull<AtAll>() != null }

    /** [消息内容][Message.contentToString]包含目标为 [target] 的 [At] */
    @MessageDsl
    public fun at(target: Long): ListeningFilter = content { message.firstIsInstanceOrNull<At>()?.target == target }

    /** [消息内容][Message.contentToString]包含目标为 [target] 的 [At] */
    @MessageDsl
    public fun at(target: User): ListeningFilter = content { message.firstIsInstanceOrNull<At>()?.target == target.id }

    /** [消息内容][Message.contentToString]包含目标为 [Bot] 的 [At], 就执行 [onEvent] */
    @MessageDsl
    public fun atBot(onEvent: @MessageDsl suspend M.(String) -> R): Ret =
        content { message.firstIsInstanceOrNull<At>()?.target == bot.id }.invoke {
            onEvent.invoke(this, message.contentToString())
        }

    @MessageDsl
    public inline fun <reified N : SingleMessage> has(noinline onEvent: @MessageDsl suspend M.(N) -> R): Ret =
        content { message.any { it is N } }.invoke { onEvent.invoke(this, message.firstIsInstance()) }

    /** [消息内容][Message.contentToString]包含 [N] 类型的 [Message] */
    @MessageDsl
    public inline fun <reified N : SingleMessage> has(): ListeningFilter = content { message.any { it is N } }

    /** 如果 [mapper] 返回值非空, 就执行 [onEvent] */
    @MessageDsl
    public open fun <N : Any> mapping(mapper: M.(String) -> N?, onEvent: @MessageDsl suspend M.(N) -> R): Ret =
        always {
            onEvent.invoke(
                this,
                mapper(this, message.contentToString()) ?: return@always this@MessageSubscribersBuilder.stub
            )
        }

    /** 如果 [filter] 返回 `true` */
    @MessageDsl
    public fun content(filter: M.(String) -> Boolean): ListeningFilter = newListeningFilter(filter)

    /** [消息内容][Message.contentToString]可由正则表达式匹配([Regex.matchEntire]) */
    @MessageDsl
    public fun matching(regex: Regex): ListeningFilter = content { regex.matchEntire(it) != null }


    /** [消息内容][Message.contentToString]可由正则表达式匹配([Regex.matchEntire]), 就执行 `onEvent` */
    @MessageDsl
    public fun matching(regex: Regex, onEvent: @MessageDsl suspend M.(MatchResult) -> Unit): Ret =
        always {
            this@MessageSubscribersBuilder.executeAndReply(this) {
                onEvent.invoke(
                    this,
                    regex.matchEntire(it) ?: return@always this@MessageSubscribersBuilder.stub
                )
            }
        }

    /** [消息内容][Message.contentToString]可由正则表达式查找([Regex.find]) */
    @MessageDsl
    public fun finding(regex: Regex): ListeningFilter = content { regex.find(it) != null }

    /** [消息内容][Message.contentToString]可由正则表达式查找([Regex.find]), 就执行 `onEvent` */
    @MessageDsl
    public fun finding(regex: Regex, onEvent: @MessageDsl suspend M.(MatchResult) -> Unit): Ret =
        always {
            this@MessageSubscribersBuilder.executeAndReply(this) {
                onEvent.invoke(
                    this,
                    regex.find(it) ?: return@always this@MessageSubscribersBuilder.stub
                )
            }
        }


    /** [消息内容][Message.contentToString]包含 [this] 则回复 [reply] */
    @MessageDsl
    public open infix fun String.containsReply(reply: String): Ret =
        content({ this@containsReply in it }, { subject.sendMessage(reply); this@MessageSubscribersBuilder.stub })

    /**
     * [消息内容][Message.contentToString]包含 [this] 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    public open infix fun String.containsReply(replier: @MessageDsl suspend M.(String) -> Any?): Ret =
        content({ this@containsReply in it }, { this@MessageSubscribersBuilder.executeAndReply(this, replier) })

    /**
     * [消息内容][Message.contentToString]可由正则表达式匹配([Regex.matchEntire]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    public open infix fun Regex.matchingReply(replier: @MessageDsl suspend M.(MatchResult) -> Any?): Ret =
        always {
            this@MessageSubscribersBuilder.executeAndReply(this) {
                replier.invoke(
                    this,
                    matchEntire(it) ?: return@always this@MessageSubscribersBuilder.stub
                )
            }
        }

    /**
     * [消息内容][Message.contentToString]可由正则表达式查找([Regex.find]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    public open infix fun Regex.findingReply(replier: @MessageDsl suspend M.(MatchResult) -> Any?): Ret =
        always {
            this@MessageSubscribersBuilder.executeAndReply(this) {
                replier.invoke(
                    this,
                    this@findingReply.find(it) ?: return@always this@MessageSubscribersBuilder.stub
                )
            }
        }


    /**
     * 不考虑空格, [消息内容][Message.contentToString]以 [this] 结尾则执行 [replier] 并将其返回值回复给发信对象.
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    public open infix fun String.endsWithReply(replier: @MessageDsl suspend M.(String) -> Any?): Ret {
        val toCheck = this.trimEnd()
        return content({ it.trim().endsWith(toCheck) }, {
            this@MessageSubscribersBuilder.executeAndReply(this) { replier(this, it.trim().removeSuffix(toCheck)) }
        })
    }

    /** 当发送的消息内容为 [this] 就回复 [reply] */
    @MessageDsl
    public open infix fun String.reply(reply: String): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, { subject.sendMessage(reply);this@MessageSubscribersBuilder.stub })
    }

    /** 当发送的消息内容为 [this] 就回复 [reply] */
    @MessageDsl
    public open infix fun String.reply(reply: Message): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, { subject.sendMessage(reply);this@MessageSubscribersBuilder.stub })
    }

    /** 当发送的消息内容为 [this] 就执行并回复 [replier] 的返回值 */
    @MessageDsl
    public open infix fun String.reply(replier: @MessageDsl suspend M.(String) -> Any?): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING")
            this@MessageSubscribersBuilder.executeAndReply(this) { replier(this, it.trim()) }
        })
    }

    /////////////////////////////////
    //// DEPRECATED AND INTERNAL ////
    /////////////////////////////////

    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE", "UNCHECKED_CAST") // false positive
    internal suspend inline fun executeAndReply(m: M, replier: suspend M.(String) -> Any?): RR {
        when (val message = replier(m, m.message.contentToString())) {
            is Message -> m.subject.sendMessage(message)
            null,
            is Unit -> Unit
            else -> m.subject.sendMessage(message.toString())
        }
        return stub
    }

    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE", "UNCHECKED_CAST") // false positive
    internal suspend inline fun executeAndQuoteReply(m: M, replier: suspend M.(String) -> Any?): RR {
        when (val message = replier(m, m.message.contentToString())) {
            is Message -> m.subject.sendMessage(m.message.quote() + message)
            null,
            is Unit -> Unit
            else -> m.subject.sendMessage(m.message.quote() + message.toString())
        }
        return stub
    }
}

/**
 * DSL 标记. 将能让 IDE 阻止一些错误的方法调用.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
public annotation class MessageDsl
