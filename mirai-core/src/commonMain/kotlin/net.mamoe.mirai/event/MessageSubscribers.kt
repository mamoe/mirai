@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.any
import net.mamoe.mirai.network.protocol.tim.packet.event.FriendMessage
import net.mamoe.mirai.network.protocol.tim.packet.event.GroupMessage
import net.mamoe.mirai.network.protocol.tim.packet.event.MessagePacket
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<MessagePacket<*>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*>> { listener ->
        subscribeAlways<MessagePacket<*>> {
            listener(it)
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun subscribeGroupMessages(crossinline listeners: suspend MessageSubscribersBuilder<GroupMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<GroupMessage> { listener ->
        subscribeAlways<GroupMessage> {
            listener(it)
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun subscribeFriendMessages(crossinline listeners: suspend MessageSubscribersBuilder<FriendMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<FriendMessage> { listener ->
        subscribeAlways<FriendMessage> {
            listener(it)
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun Bot.subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<MessagePacket<*>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*>> { listener ->
        this.subscribeAlways<MessagePacket<*>> {
            listener(it)
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有群消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun Bot.subscribeGroupMessages(crossinline listeners: suspend MessageSubscribersBuilder<GroupMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<GroupMessage> { listener ->
        this.subscribeAlways<GroupMessage> {
            listener(it)
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有好友消息事件.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun Bot.subscribeFriendMessages(crossinline listeners: suspend MessageSubscribersBuilder<FriendMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<FriendMessage> { listener ->
        this.subscribeAlways<FriendMessage> {
            listener(it)
        }
    }.apply { listeners() }
}

private typealias AnyReplier<T> = @MessageDsl suspend T.(String) -> Any?

private suspend inline operator fun <T : MessagePacket<*>> (@MessageDsl suspend T.(String) -> Unit).invoke(t: T) =
    this.invoke(t, t.message.stringValue)

@JvmName("invoke1") //Avoid Platform declaration clash
private suspend inline operator fun <T : MessagePacket<*>> AnyReplier<T>.invoke(t: T): Any? =
    this.invoke(t, t.message.stringValue)

/**
 * 消息订阅构造器
 *
 * @see subscribeFriendMessages
 * @sample demo.subscribe.messageDSL
 */
@Suppress("unused")
@MessageDsl
class MessageSubscribersBuilder<T : MessagePacket<*>>(
    inline val subscriber: suspend (@MessageDsl suspend T.(String) -> Unit) -> Unit
) {
    /**
     * 如果消息内容 `==` [equals], 就执行 [onEvent]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
    @MessageDsl
    suspend fun case(
        equals: String,
        trim: Boolean = true,
        ignoreCase: Boolean = false,
        onEvent: @MessageDsl suspend T.(String) -> Unit
    ) =
        content({ equals.equals(if (trim) it.trim() else it, ignoreCase = ignoreCase) }, onEvent)

    /**
     * 如果消息内容包含 [sub], 就执行 [onEvent]
     */
    @MessageDsl
    suspend fun contains(sub: String, onEvent: @MessageDsl suspend T.(String) -> Unit) = content({ sub in it }, onEvent)

    /**
     * 如果消息的前缀是 [prefix], 就执行 [onEvent]
     */
    @MessageDsl
    suspend fun startsWith(
        prefix: String,
        removePrefix: Boolean = true,
        onEvent: @MessageDsl suspend T.(String) -> Unit
    ) =
        content({ it.startsWith(prefix) }) {
            if (removePrefix) this.onEvent(this.message.stringValue.substringAfter(prefix))
            else onEvent(this)
        }

    /**
     * 如果消息的结尾是 [suffix], 就执行 [onEvent]
     */
    @MessageDsl
    suspend fun endsWith(suffix: String, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ it.endsWith(suffix) }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    @MessageDsl
    suspend fun sentBy(qqId: UInt, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ sender.id == qqId }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    @MessageDsl
    suspend fun sentBy(qqId: Long, onEvent: @MessageDsl suspend T.(String) -> Unit) = sentBy(qqId.toUInt(), onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend fun sentFrom(id: UInt, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ if (this is GroupMessage) group.id == id else false }, onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend fun sentFrom(id: Long, onEvent: @MessageDsl suspend T.(String) -> Unit) = sentFrom(id.toUInt(), onEvent)

    /**
     * 如果消息内容包含 [M] 类型的 [Message], 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun <reified M : Message> has(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (message.any<M>()) onEvent(this) }

    /**
     * 如果 [filter] 返回 `true` 就执行 `onEvent`
     */
    @MessageDsl
    suspend fun content(filter: T.(String) -> Boolean, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (this.filter(message.stringValue)) onEvent(this) }

    @MessageDsl
    suspend infix fun String.containsReply(replier: String) =
        content({ this@containsReply in it }) { this@content.reply(replier) }

    @MessageDsl
    suspend infix fun String.containsReply(replier: AnyReplier<T>) =
        content({ this@containsReply in it }) { replier(this) }

    @MessageDsl
    suspend infix fun String.startsWithReply(replier: AnyReplier<T>) =
        content({ it.startsWith(this@startsWithReply) }) { replier(this) }

    @MessageDsl
    suspend infix fun String.endswithReply(replier: AnyReplier<T>) =
        content({ it.endsWith(this@endswithReply) }) { replier(this) }

    @MessageDsl
    suspend infix fun String.reply(reply: String) = case(this) {
        this@case.reply(reply)
    }

    @MessageDsl
    suspend infix fun String.reply(reply: AnyReplier<T>) = case(this) {
        when (val message = reply(this)) {
            is Message -> reply(message)
            is Unit -> {

            }
            else -> reply(message.toString())
        }
    }


/* 易产生迷惑感
    suspend inline fun replyCase(equals: String, trim: Boolean = true, noinline replier: MessageReplier<T>) = case(equals, trim) { reply(replier(this)) }
    suspend inline fun replyContains(value: String, noinline replier: MessageReplier<T>) = content({ value in it }) { replier(this) }
    suspend inline fun replyStartsWith(value: String, noinline replier: MessageReplier<T>) = content({ it.startsWith(value) }) { replier(this) }
    suspend inline fun replyEndsWith(value: String, noinline replier: MessageReplier<T>) = content({ it.endsWith(value) }) { replier(this) }
*/
}

/**
 * DSL 标记. 将能让 IDE 阻止一些错误的方法调用.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
internal annotation class MessageDsl