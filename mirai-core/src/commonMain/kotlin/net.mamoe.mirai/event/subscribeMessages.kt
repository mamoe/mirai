/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.TempMessage
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.firstIsInstance
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.utils.SinceMirai
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.JsName
import kotlin.jvm.JvmName

typealias MessagePacketSubscribersBuilder = MessageSubscribersBuilder<ContactMessage, Listener<ContactMessage>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> CoroutineScope.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R {
    // contract 可帮助 IDE 进行类型推断. 无实际代码作用.
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }

    return MessagePacketSubscribersBuilder(Unit)
    { filter, messageListener: MessageListener<ContactMessage, Unit> ->
        // subscribeAlways 即注册一个监听器. 这个监听器收到消息后就传递给 [messageListener]
        // messageListener 即为 DSL 里 `contains(...) { }`, `startsWith(...) { }` 的代码块.
        subscribeAlways(coroutineContext, concurrencyKind) {
            // this.message.contentToString() 即为 messageListener 中 it 接收到的值
            val toString = this.message.contentToString()
            if (filter.invoke(this, toString))
                messageListener.invoke(this, toString)
        }
    }.run(listeners)
}

typealias GroupMessageSubscribersBuilder = MessageSubscribersBuilder<GroupMessage, Listener<GroupMessage>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> CoroutineScope.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return GroupMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

typealias FriendMessageSubscribersBuilder = MessageSubscribersBuilder<FriendMessage, Listener<FriendMessage>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> CoroutineScope.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return FriendMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

typealias TempMessageSubscribersBuilder = MessageSubscribersBuilder<TempMessage, Listener<TempMessage>, Unit, Unit>

/**
 * 订阅来自所有 [Bot] 的所有临时会话消息事件
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> CoroutineScope.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: TempMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return TempMessageSubscribersBuilder(Unit) { filter, listener ->
        subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

/**
 * 订阅来自这个 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> Bot.subscribeMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: MessagePacketSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return MessagePacketSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

/**
 * 订阅来自这个 [Bot] 的所有群消息事件
 *
 * @param coroutineContext 给事件监听协程的额外的 [CoroutineContext]
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> Bot.subscribeGroupMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: GroupMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return GroupMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

/**
 * 订阅来自这个 [Bot] 的所有好友消息事件.
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@OptIn(ExperimentalContracts::class)
fun <R> Bot.subscribeFriendMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: FriendMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return FriendMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}


/**
 * 订阅来自这个 [Bot] 的所有临时会话消息事件.
 *
 * @see CoroutineScope.incoming 打开一个指定事件的接收通道
 */
@SinceMirai("0.35.0")
@OptIn(ExperimentalContracts::class)
fun <R> Bot.subscribeTempMessages(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    listeners: TempMessageSubscribersBuilder.() -> R
): R {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    return TempMessageSubscribersBuilder(Unit) { filter, listener ->
        this.subscribeAlways(coroutineContext, concurrencyKind) {
            val toString = this.message.contentToString()
            if (filter(this, toString))
                listener(this, toString)
        }
    }.run(listeners)
}

/**
 * 打开一个指定事件的接收通道
 *
 * @param capacity 同 [Channel] 的参数, 参见 [Channel.Factory] 中的常量.
 *
 * @see capacity 默认无限大小. 详见 [Channel.Factory] 中的常量 [Channel.UNLIMITED], [Channel.CONFLATED], [Channel.RENDEZVOUS].
 * 请谨慎使用 [Channel.RENDEZVOUS]: 在 [Channel] 未被 [接收][Channel.receive] 时他将会阻塞事件处理
 *
 * @see subscribeFriendMessages
 * @see subscribeMessages
 * @see subscribeGroupMessages
 */
inline fun <reified E : Event> CoroutineScope.incoming(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    capacity: Int = Channel.UNLIMITED
): ReceiveChannel<E> {
    return Channel<E>(capacity).apply {
        val listener = this@incoming.subscribeAlways<E>(coroutineContext, concurrencyKind) {
            send(this)
        }
        this.invokeOnClose {
            listener.cancel(CancellationException("ReceiveChannel closed", it))
        }
    }
}


/**
 * 打开一个来自指定 [Bot] 的指定事件的接收通道
 *
 * @param capacity 同 [Channel] 的参数, 参见 [Channel.Factory] 中的常量.
 *
 * @see capacity 默认无限大小. 详见 [Channel.Factory] 中的常量 [Channel.UNLIMITED], [Channel.CONFLATED], [Channel.RENDEZVOUS].
 * 请谨慎使用 [Channel.RENDEZVOUS]: 在 [Channel] 未被 [接收][Channel.receive] 时他将会阻塞事件处理
 *
 * @see subscribeFriendMessages
 * @see subscribeMessages
 * @see subscribeGroupMessages
 */
inline fun <reified E : BotEvent> Bot.incoming(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    concurrencyKind: Listener.ConcurrencyKind = Listener.ConcurrencyKind.CONCURRENT,
    capacity: Int = Channel.UNLIMITED
): ReceiveChannel<E> {
    return Channel<E>(capacity).apply {
        val listener = this@incoming.subscribeAlways<E>(coroutineContext, concurrencyKind) {
            send(this)
        }
        this.invokeOnClose {
            listener.cancel(CancellationException("ReceiveChannel closed", it))
        }
    }
}

/**
 * 消息事件的处理器.
 *
 * 注:
 * 接受者 T 为 [ContactMessage]
 * 参数 String 为 转为字符串了的消息 ([Message.toString])
 */
typealias MessageListener<T, R> = @MessageDsl suspend T.(String) -> R


/**
 * 消息订阅构造器
 *
 * @param M 消息类型
 * @param R 消息监听器内部的返回值
 * @param Ret 每个 DSL 函数创建监听器之后的返回值
 *
 * @see subscribeFriendMessages
 */
@Suppress(
    "unused", "DSL_SCOPE_VIOLATION_WARNING", "INAPPLICABLE_JVM_NAME", "INVALID_CHARACTERS",
    "NAME_CONTAINS_ILLEGAL_CHARS", "FunctionName"
)
@MessageDsl
open class MessageSubscribersBuilder<M : ContactMessage, out Ret, R : RR, RR>(
    val stub: RR,
    /**
     * invoke 这个 lambda 时, 它将会把 [消息事件的处理器][MessageListener] 注册给事件, 并返回注册完成返回的监听器.
     */
    val subscriber: (M.(String) -> Boolean, MessageListener<M, RR>) -> Ret
) {
    @Suppress("DEPRECATION_ERROR")
    open fun newListeningFilter(filter: M.(String) -> Boolean): ListeningFilter = ListeningFilter(filter)

    /**
     * 监听的条件
     */
    open inner class ListeningFilter @Deprecated( // keep it for development warning
        "use newListeningFilter instead",
        ReplaceWith("newListeningFilter(filter)"),
        level = DeprecationLevel.ERROR
    ) constructor(
        val filter: M.(String) -> Boolean
    ) {
        /** 进行逻辑 `or`. */
        infix fun or(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) || another.filter.invoke(this, it) }

        /** 进行逻辑 `and`. */
        infix fun and(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) && another.filter.invoke(this, it) }

        /** 进行逻辑 `xor`. */
        infix fun xor(another: ListeningFilter): ListeningFilter =
            newListeningFilter { filter.invoke(this, it) xor another.filter.invoke(this, it) }

        /** 进行逻辑 `nand`, 即 `not and`. */
        infix fun nand(another: ListeningFilter): ListeningFilter =
            newListeningFilter { !filter.invoke(this, it) || !another.filter.invoke(this, it) }

        /** 进行逻辑 `not` */
        fun not(): ListeningFilter = newListeningFilter { !filter.invoke(this, it) }

        /** 启动事件监听. */
        // do not inline due to kotlin (1.3.61) bug: java.lang.IllegalAccessError
        operator fun invoke(onEvent: MessageListener<M, R>): Ret {
            return content(filter, onEvent)
        }
    }

    /** 启动这个监听器, 在满足条件时回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.reply(toReply: String): Ret {
        return content(filter) { reply(toReply);this@MessageSubscribersBuilder.stub }
    }

    /** 启动这个监听器, 在满足条件时回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.reply(message: Message): Ret {
        return content(filter) { reply(message);this@MessageSubscribersBuilder.stub }
    }

    /** 启动这个监听器, 在满足条件时回复原消息 */
    @JvmName("reply3")
    @SinceMirai("0.33.0")
    @MessageDsl
    open infix fun ListeningFilter.`->`(toReply: String): Ret {
        return this.reply(toReply)
    }

    /** 启动这个监听器, 在满足条件时回复原消息 */
    @JvmName("reply3")
    @SinceMirai("0.33.0")
    @MessageDsl
    open infix fun ListeningFilter.`->`(message: Message): Ret {
        return this.reply(message)
    }

    /** 启动这个监听器, 在满足条件时回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.reply(replier: (@MessageDsl suspend M.(String) -> Any?)): Ret {
        return content(filter) {
            this@MessageSubscribersBuilder.executeAndReply(this, replier)
        }
    }

    /** 启动这个监听器, 在满足条件时引用回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.quoteReply(toReply: String): Ret {
        return content(filter) { quoteReply(toReply);this@MessageSubscribersBuilder.stub }
    }

    /** 启动这个监听器, 在满足条件时引用回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.quoteReply(toReply: Message): Ret {
        return content(filter) { quoteReply(toReply);this@MessageSubscribersBuilder.stub }
    }

    /** 启动这个监听器, 在满足条件时执行 [replier] 并引用回复原消息 */
    @SinceMirai("0.29.0")
    @MessageDsl
    open infix fun ListeningFilter.quoteReply(replier: (@MessageDsl suspend M.(String) -> Any?)): Ret {
        return content(filter) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING")
            this@MessageSubscribersBuilder.executeAndQuoteReply(this, replier)
        }
    }


    /** 无任何触发条件, 每次收到消息都执行 [onEvent] */
    @MessageDsl
    open fun always(onEvent: MessageListener<M, RR>): Ret = subscriber({ true }, onEvent)

    /** 如果消息内容 `==` [equals] */
    @MessageDsl
    fun case(
        equals: String,
        ignoreCase: Boolean = false,
        trim: Boolean = true
    ): ListeningFilter {
        return if (trim) {
            val toCheck = equals.trim()
            content { it.trim().equals(toCheck, ignoreCase = ignoreCase) }
        } else {
            content { it.equals(equals, ignoreCase = ignoreCase) }
        }
    }

    /** 如果消息内容 `==` [equals] */
    @MessageDsl
    @JvmName("case1")
    @JsName("case1")
    @SinceMirai("0.29.0")
    infix fun String.`->`(block: MessageListener<M, R>): Ret {
        return case(this, onEvent = block)
    }

    /** 如果消息内容 `==` [equals] */
    @MessageDsl
    @SinceMirai("0.37.1")
    operator fun String.invoke(block: MessageListener<M, R>): Ret {
        return case(this, onEvent = block)
    }

    /**
     * 如果消息内容 `==` [equals]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
    @MessageDsl
    fun case(
        equals: String,
        ignoreCase: Boolean = false,
        trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret {
        val toCheck = if (trim) equals.trim() else equals
        return content({ (if (trim) it.trim() else it).equals(toCheck, ignoreCase = ignoreCase) }, {
            onEvent(this, this.message.contentToString())
        })
    }

    /** 如果消息内容包含 [sub] */
    @MessageDsl
    fun contains(sub: String): ListeningFilter = content { sub in it }

    /**
     * 如果消息内容包含 [sub] 中的任意一个元素
     */
    @MessageDsl
    fun contains(
        sub: String,
        ignoreCase: Boolean = false,
        trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret {
        return if (trim) {
            val toCheck = sub.trim()
            content({ it.contains(toCheck, ignoreCase = ignoreCase) }, {
                onEvent(this, this.message.contentToString().trim())
            })
        } else {
            content({ it.contains(sub, ignoreCase = ignoreCase) }, {
                onEvent(this, this.message.contentToString())
            })
        }
    }

    /** 如果消息内容包含 [sub] */
    @MessageDsl
    fun containsAny(vararg sub: String): ListeningFilter = content { sub.any { item -> item in it } }

    /** 如果消息内容包含 [sub] 中的任意一个元素 */
    @MessageDsl
    fun containsAny(
        vararg sub: String,
        ignoreCase: Boolean = false,
        trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret {
        return if (trim) {
            val list = sub.map { it.trim() }
            content({
                list.any { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) }
            }, {
                onEvent(this, this.message.contentToString().trim())
            })
        } else {
            content({
                sub.any { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) }
            }, {
                onEvent(this, this.message.contentToString())
            })
        }
    }

    /** 如果消息内容包含 [sub] */
    @MessageDsl
    fun containsAll(vararg sub: String): ListeningFilter = content { sub.all { item -> item in it } }

    /**
     * 如果消息内容包含 [sub] 中的任意一个元素
     */
    @MessageDsl
    fun containsAll(
        vararg sub: String,
        ignoreCase: Boolean = false,
        trim: Boolean = true,
        onEvent: MessageListener<M, R>
    ): Ret {
        return if (trim) {
            val list = sub.map { it.trim() }
            content({ list.all { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } },
                { onEvent(this, this.message.contentToString().trim()) })
        } else {
            content({ sub.all { toCheck -> it.contains(toCheck, ignoreCase = ignoreCase) } },
                { onEvent(this, this.message.contentToString()) })
        }
    }

    /** 如果消息的前缀是 [prefix] */
    @MessageDsl
    fun startsWith(prefix: String, trim: Boolean = true): ListeningFilter {
        val toCheck = if (trim) prefix.trim() else prefix
        return content { (if (trim) it.trim() else it).startsWith(toCheck) }
    }

    /** 如果消息的前缀是 [prefix] */
    @MessageDsl
    fun startsWith(
        prefix: String,
        removePrefix: Boolean = true,
        trim: Boolean = true,
        onEvent: @MessageDsl suspend M.(String) -> R
    ): Ret {
        return if (trim) {
            val toCheck = prefix.trim()
            content({ it.trimStart().startsWith(toCheck) }, {
                if (removePrefix) this.onEvent(this.message.contentToString().substringAfter(toCheck).trim())
                else onEvent(this, this.message.contentToString().trim())
            })
        } else {
            content({ it.startsWith(prefix) }, {
                if (removePrefix) this.onEvent(this.message.contentToString().removePrefix(prefix))
                else onEvent(this, this.message.contentToString())
            })
        }
    }

    /** 如果消息的结尾是 [suffix] */
    @MessageDsl
    fun endsWith(suffix: String): ListeningFilter = content { it.endsWith(suffix) }

    /** 如果消息的结尾是 [suffix] */
    @MessageDsl
    fun endsWith(
        suffix: String,
        removeSuffix: Boolean = true,
        trim: Boolean = true,
        onEvent: @MessageDsl suspend M.(String) -> R
    ): Ret {
        return if (trim) {
            val toCheck = suffix.trim()
            content({ it.trimEnd().endsWith(toCheck) }, {
                if (removeSuffix) this.onEvent(this.message.contentToString().removeSuffix(toCheck).trim())
                else onEvent(this, this.message.contentToString().trim())
            })
        } else {
            content({ it.endsWith(suffix) }, {
                if (removeSuffix) this.onEvent(this.message.contentToString().removeSuffix(suffix))
                else onEvent(this, this.message.contentToString())
            })
        }
    }

    /** 如果是这个人发的消息. 消息目前只会是群消息 */
    @MessageDsl
    fun sentBy(name: String): ListeningFilter = content { this is GroupMessage && this.senderName == name }

    /** 如果是这个人发的消息. 消息目前只会是群消息 */
    @MessageDsl
    fun sentBy(name: String, onEvent: MessageListener<M, R>): Ret =
        content({ (this as? GroupMessage)?.senderName == name }, onEvent)

    /** 如果是这个人发的消息. 消息可以是好友消息也可以是群消息 */
    @MessageDsl
    fun sentBy(qq: Long): ListeningFilter = content { sender.id == qq }

    /** 如果是这个人发的消息. 消息可以是好友消息也可以是群消息 */
    @MessageDsl
    fun sentBy(qq: Long, onEvent: MessageListener<M, R>): Ret = content({ this.sender.id == qq }, onEvent)

    /** 如果是好友发来的消息 */
    @MessageDsl
    fun sentByFriend(onEvent: MessageListener<FriendMessage, R>): Ret =
        content({ this is FriendMessage }) {
            onEvent(this as FriendMessage, it)
        }

    /** 如果是好友发来的消息 */
    @MessageDsl
    fun sentByFriend(): ListeningFilter = newListeningFilter { this is FriendMessage }

    /** 如果是好友发来的消息 */
    @MessageDsl
    fun sentByTemp(): ListeningFilter = newListeningFilter { this is TempMessage }

    /** 如果是管理员或群主发的消息 */
    @MessageDsl
    fun sentByOperator(): ListeningFilter =
        content { this is GroupMessage && sender.permission.isOperator() }

    /** 如果是管理员或群主发的消息 */
    @MessageDsl
    fun sentByOperator(onEvent: MessageListener<M, R>): Ret =
        content({ this is GroupMessage && this.sender.isOperator() }, onEvent)

    /** 如果是管理员发的消息 */
    @MessageDsl
    fun sentByAdministrator(): ListeningFilter =
        content { this is GroupMessage && sender.permission.isAdministrator() }

    /** 如果是管理员发的消息 */
    @MessageDsl
    fun sentByAdministrator(onEvent: MessageListener<M, R>): Ret =
        content({ this is GroupMessage && this.sender.isAdministrator() }, onEvent)

    /** 如果是群主发的消息 */
    @MessageDsl
    fun sentByOwner(): ListeningFilter =
        content { this is GroupMessage && sender.isOwner() }

    /** 如果是群主发的消息 */
    @MessageDsl
    fun sentByOwner(onEvent: MessageListener<M, R>): Ret =
        content({ this is GroupMessage && this.sender.isOwner() }, onEvent)

    /** 如果是来自这个群的消息 */
    @MessageDsl
    fun sentFrom(groupId: Long): ListeningFilter =
        content { this is GroupMessage && group.id == groupId }

    /** 如果是来自这个群的消息, 就执行 [onEvent] */
    @MessageDsl
    fun sentFrom(groupId: Long, onEvent: MessageListener<GroupMessage, R>): Ret =
        content({ this is GroupMessage && this.group.id == groupId }) { onEvent(this as GroupMessage, it) }

    /** 如果消息内容包含目标为 [Bot] 的 [At] */
    @MessageDsl
    fun atBot(): ListeningFilter =
        content { message.firstIsInstanceOrNull<At>()?.target == bot.id }

    /** 如果消息内容包含目标为 [Bot] 的 [At], 就执行 [onEvent] */
    @MessageDsl
    @SinceMirai("0.30.0")
    fun atBot(onEvent: @MessageDsl suspend M.(String) -> R): Ret =
        content({ message.firstIsInstanceOrNull<At>()?.target == bot.id },
            { onEvent.invoke(this, message.contentToString()) })

    /** 如果消息内容包含 [N] 类型的 [Message] */
    @MessageDsl
    inline fun <reified N : Message> has(): ListeningFilter = content { message.any { it is N } }

    /** 如果消息内容包含 [M] 类型的 [Message], 就执行 [onEvent] */
    @MessageDsl
    @SinceMirai("0.30.0")
    inline fun <reified N : Message> has(noinline onEvent: @MessageDsl suspend M.(N) -> R): Ret =
        content({ message.any { it is N } }, { onEvent.invoke(this, message.firstIsInstance()) })

    /** 如果 [mapper] 返回值非空, 就执行 [onEvent] */
    @MessageDsl
    @SinceMirai("0.30.0")
    open fun <N : Any> mapping(mapper: M.(String) -> N?, onEvent: @MessageDsl suspend M.(N) -> R): Ret =
        always { onEvent.invoke(this, mapper(this, message.contentToString()) ?: return@always stub) }

    /** 如果 [filter] 返回 `true` */
    @MessageDsl
    fun content(filter: M.(String) -> Boolean): ListeningFilter = newListeningFilter(filter)

    /** 如果 [filter] 返回 `true` 就执行 `onEvent`*/
    @MessageDsl
    fun content(filter: M.(String) -> Boolean, onEvent: MessageListener<M, RR>): Ret =
        subscriber(filter) { onEvent(this, it) }

    /** 如果消息内容可由正则表达式匹配([Regex.matchEntire]) */
    @MessageDsl
    fun matching(regex: Regex): ListeningFilter = content { regex.matchEntire(it) != null }


    /** 如果消息内容可由正则表达式匹配([Regex.matchEntire]), 就执行 `onEvent` */
    @MessageDsl
    fun matching(regex: Regex, onEvent: @MessageDsl suspend M.(MatchResult) -> Unit): Ret =
        always { executeAndReply(this) { onEvent.invoke(this, regex.matchEntire(it) ?: return@always stub) } }

    /** 如果消息内容可由正则表达式查找([Regex.find]) */
    @MessageDsl
    fun finding(regex: Regex): ListeningFilter = content { regex.find(it) != null }

    /** 如果消息内容可由正则表达式查找([Regex.find]), 就执行 `onEvent` */
    @MessageDsl
    fun finding(regex: Regex, onEvent: @MessageDsl suspend M.(MatchResult) -> Unit): Ret =
        always { executeAndReply(this) { onEvent.invoke(this, regex.find(it) ?: return@always stub) } }


    /** 若消息内容包含 [this] 则回复 [reply] */
    @MessageDsl
    open infix fun String.containsReply(reply: String): Ret =
        content({ this@containsReply in it }, { reply(reply); stub })

    /**
     * 若消息内容包含 [this] 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    open infix fun String.containsReply(replier: @MessageDsl suspend M.(String) -> Any?): Ret =
        content({ this@containsReply in it }, { executeAndReply(this, replier) })

    /**
     * 若消息内容可由正则表达式匹配([Regex.matchEntire]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    open infix fun Regex.matchingReply(replier: @MessageDsl suspend M.(MatchResult) -> Any?): Ret =
        always { executeAndReply(this) { replier.invoke(this, matchEntire(it) ?: return@always stub) } }

    /**
     * 若消息内容可由正则表达式查找([Regex.find]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    open infix fun Regex.findingReply(replier: @MessageDsl suspend M.(MatchResult) -> Any?): Ret =
        always { executeAndReply(this) { replier.invoke(this, this@findingReply.find(it) ?: return@always stub) } }

    /**
     * 不考虑空格, 若消息内容以 [this] 开始则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是去掉用来判断的前缀并删除前后空格后的字符串.
     * 如当消息为 "kick    123456     " 时
     * ```kotlin
     * "kick" startsWithReply {
     *     println(it) // it 为 "123456"
     * }
     * ```
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他类型则 [Any.toString] 后回复
     */
    @MessageDsl
    open infix fun String.startsWithReply(replier: @MessageDsl suspend M.(String) -> Any?): Ret {
        val toCheck = this.trimStart()
        return content({ it.trim().startsWith(toCheck) }, {
            executeAndReply(this) { replier(this, it.trim().removePrefix(toCheck)) }
        })
    }

    /**
     * 不考虑空格, 若消息内容以 [this] 结尾则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是去掉用来判断的后缀并删除前后空格后的字符串.
     * 如当消息为 "  123456 test" 时
     * ```kotlin
     * "test" endsWithReply {
     *     println(it) // it 为 "123456"
     * }
     * ```
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    open infix fun String.endsWithReply(replier: @MessageDsl suspend M.(String) -> Any?): Ret {
        val toCheck = this.trimEnd()
        return content({ it.trim().endsWith(toCheck) }, {
            executeAndReply(this) { replier(this, it.trim().removeSuffix(toCheck)) }
        })
    }

    /** 当发送的消息内容为 [this] 就回复 [reply] */
    @MessageDsl
    open infix fun String.reply(reply: String): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, { reply(reply);this@MessageSubscribersBuilder.stub })
    }

    /** 当发送的消息内容为 [this] 就回复 [reply] */
    @MessageDsl
    open infix fun String.reply(reply: Message): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, { reply(reply);this@MessageSubscribersBuilder.stub })
    }

    /** 当发送的消息内容为 [this] 就执行并回复 [replier] 的返回值 */
    @MessageDsl
    open infix fun String.reply(replier: @MessageDsl suspend M.(String) -> Any?): Ret {
        val toCheck = this.trim()
        return content({ it.trim() == toCheck }, {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING")
            executeAndReply(this) { replier(this, it.trim()) }
        })
    }

    @PublishedApi
    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE", "UNCHECKED_CAST") // false positive
    internal suspend inline fun executeAndReply(m: M, replier: suspend M.(String) -> Any?): RR {
        when (val message = replier(m, m.message.contentToString())) {
            is Message -> m.reply(message)
            is Unit -> Unit
            else -> m.reply(message.toString())
        }
        return stub
    }

    @PublishedApi
    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE", "UNCHECKED_CAST") // false positive
    internal suspend inline fun executeAndQuoteReply(m: M, replier: suspend M.(String) -> Any?): RR {
        when (val message = replier(m, m.message.contentToString())) {
            is Message -> m.quoteReply(message)
            is Unit -> Unit
            else -> m.quoteReply(message.toString())
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
annotation class MessageDsl