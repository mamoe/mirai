@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.isOwner
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.message.data.Message
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun CoroutineScope.subscribeMessages(crossinline listeners: MessageSubscribersBuilder<MessagePacket<*, *>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*, *>> { listener ->
        subscribeAlways {
            listener(it, this.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun CoroutineScope.subscribeGroupMessages(crossinline listeners: MessageSubscribersBuilder<GroupMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<GroupMessage> { listener ->
        subscribeAlways {
            listener(it, this.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun CoroutineScope.subscribeFriendMessages(crossinline listeners: MessageSubscribersBuilder<FriendMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<FriendMessage> { listener ->
        subscribeAlways {
            listener(it, this.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun Bot.subscribeMessages(crossinline listeners: MessageSubscribersBuilder<MessagePacket<*, *>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*, *>> { listener ->
        this.subscribeAlways {
            listener(it, this.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有群消息事件
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun Bot.subscribeGroupMessages(crossinline listeners: MessageSubscribersBuilder<GroupMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<GroupMessage> { listener ->
        this.subscribeAlways {
            listener(it, this.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有好友消息事件.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
inline fun Bot.subscribeFriendMessages(crossinline listeners: MessageSubscribersBuilder<FriendMessage>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<FriendMessage> { listener ->
        this.subscribeAlways {
            it.listener(it.message.toString())
        }
    }.apply { listeners() }
}

/**
 * 消息订阅构造器
 *
 * @see subscribeFriendMessages
 * @sample demo.subscribe.messageDSL
 */
// TODO: 2019/12/23 应定义为 inline, 但这会导致一个 JVM run-time VerifyError. 等待 kotlin 修复 bug (Kotlin 1.3.61)
@Suppress("unused")
@MessageDsl
class MessageSubscribersBuilder<T : MessagePacket<*, *>>(
    val subscriber: (@MessageDsl suspend T.(String) -> Unit) -> Listener<T>
) {
    /**
     * 无任何触发条件.
     */
    @MessageDsl
    fun always(onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> {
        return subscriber(onEvent)
    }

    /**
     * 如果消息内容 `==` [equals], 就执行 [onEvent]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
    @MessageDsl
    inline fun case(
        equals: String,
        trim: Boolean = true,
        ignoreCase: Boolean = false,
        crossinline onEvent: @MessageDsl suspend T.(String) -> Unit
    ): Listener<T> {
        val toCheck = if (trim) equals.trim() else equals
        return content({ toCheck.equals(if (trim) it.trim() else it, ignoreCase = ignoreCase) }, onEvent)
    }

    /**
     * 如果消息内容包含 [sub], 就执行 [onEvent]
     */
    @MessageDsl
    inline fun contains(sub: String, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> = content({ sub in it }, onEvent)

    /**
     * 如果消息的前缀是 [prefix], 就执行 [onEvent]
     */
    @MessageDsl
    inline fun startsWith(
        prefix: String,
        removePrefix: Boolean = true,
        crossinline onEvent: @MessageDsl suspend T.(String) -> Unit
    ): Listener<T> =
        content({ it.startsWith(prefix) }) {
            if (removePrefix) this.onEvent(this.message.toString().substringAfter(prefix))
            else onEvent(this, this.message.toString())
        }

    /**
     * 如果消息的结尾是 [suffix], 就执行 [onEvent]
     */
    @MessageDsl
    inline fun endsWith(suffix: String, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ it.endsWith(suffix) }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    @MessageDsl
    inline fun sentBy(qqId: Long, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ sender.id == qqId }, onEvent)

    /**
     * 如果是管理员或群主发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    inline fun sentByOperator(crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ this is GroupMessage && sender.permission.isOperator() }, onEvent)

    /**
     * 如果是管理员发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    inline fun sentByAdministrator(crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ this is GroupMessage && sender.permission.isAdministrator() }, onEvent)

    /**
     * 如果是群主发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    inline fun sentByOwner(crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ this is GroupMessage && sender.permission.isOwner() }, onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    @MessageDsl
    inline fun sentFrom(id: Long, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        content({ if (this is GroupMessage) group.id == id else false }, onEvent)

    /**
     * 如果消息内容包含 [M] 类型的 [Message], 就执行 [onEvent]
     */
    @MessageDsl
    inline fun <reified M : Message> has(crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        subscriber { if (message.any { it::class == M::class }) onEvent(this, this.message.toString()) }

    /**
     * 如果 [filter] 返回 `true` 就执行 `onEvent`
     */
    @MessageDsl
    inline fun content(crossinline filter: T.(String) -> Boolean, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit): Listener<T> =
        subscriber { if (this.filter(message.toString())) onEvent(this, this.message.toString()) }

    /**
     * 如果消息内容可由正则表达式匹配([Regex.matchEntire]), 就执行 `onEvent`
     */
    @MessageDsl
    inline fun matching(regex: Regex, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit) {
        content({ regex.matchEntire(it) != null }, onEvent)
    }

    /**
     * 如果消息内容可由正则表达式查找([Regex.find]), 就执行 `onEvent`
     */
    @MessageDsl
    inline fun finding(regex: Regex, crossinline onEvent: @MessageDsl suspend T.(String) -> Unit) {
        content({ regex.find(it) != null }, onEvent)
    }

    /**
     * 若消息内容包含 [this] 则回复 [reply]
     */
    @MessageDsl
    infix fun String.containsReply(reply: String) =
        content({ this@containsReply in it }) { this@content.reply(reply) }

    /**
     * 若消息内容包含 [this] 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    inline infix fun String.containsReply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) =
        content({ this@containsReply in it }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply(replier)
        }

    /**
     * 若消息内容可由正则表达式匹配([Regex.matchEntire]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    inline infix fun Regex.matchingReply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) {
        content({ this@matchingReply.matchEntire(it) != null }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply(replier)
        }
    }

    /**
     * 若消息内容可由正则表达式查找([Regex.find]), 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    inline infix fun Regex.findingReply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) {
        content({ this@findingReply.find(it) != null }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply(replier)
        }
    }

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
    inline infix fun String.startsWithReply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) {
        val toCheck = this.trimStart()
        content({ it.trimStart().startsWith(toCheck) }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply {
                replier(it.removePrefix(toCheck).trim())
            }
        }
    }

    /**
     * 不考虑空格, 若消息内容以 [this] 结尾则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是去掉用来判断的后缀并删除前后空格后的字符串.
     * 如当消息为 "  123456 test" 时
     * ```kotlin
     * "test" endswithReply {
     *     println(it) // it 为 "123456"
     * }
     * ```
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    inline infix fun String.endswithReply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) {
        val toCheck = this.trimEnd()
        content({ it.endsWith(this@endswithReply) }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply {
                replier(it.removeSuffix(toCheck).trim())
            }
        }
    }

    @MessageDsl
    infix fun String.reply(reply: String) = case(this) {
        this@case.reply(reply)
    }

    @MessageDsl
    inline infix fun String.reply(crossinline replier: @MessageDsl suspend T.(String) -> Any?) = case(this) {
        @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
        executeAndReply(replier)
    }

    @PublishedApi
    @Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE") // false positive
    internal suspend inline fun T.executeAndReply(replier: @MessageDsl suspend T.(String) -> Any?) {
        when (val message = replier(this, this.message.toString())) {
            is Message -> this.reply(message)
            is Unit -> {

            }
            else -> this.reply(message.toString())
        }
    }

/* 易产生迷惑感
 fun replyCase(equals: String, trim: Boolean = true, replier: MessageReplier<T>) = case(equals, trim) { reply(replier(this)) }
 fun replyContains(value: String, replier: MessageReplier<T>) = content({ value in it }) { replier(this) }
 fun replyStartsWith(value: String, replier: MessageReplier<T>) = content({ it.startsWith(value) }) { replier(this) }
 fun replyEndsWith(value: String, replier: MessageReplier<T>) = content({ it.endsWith(value) }) { replier(this) }
*/
}

/**
 * DSL 标记. 将能让 IDE 阻止一些错误的方法调用.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
internal annotation class MessageDsl