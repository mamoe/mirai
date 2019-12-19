@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

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
import kotlin.jvm.JvmName

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@UseExperimental(ExperimentalContracts::class)
@MessageDsl
suspend inline fun subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<MessagePacket<*, *>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*, *>> { listener ->
        subscribeAlways<MessagePacket<*, *>> {
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
suspend inline fun Bot.subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<MessagePacket<*, *>>.() -> Unit) {
    contract {
        callsInPlace(listeners, InvocationKind.EXACTLY_ONCE)
    }
    MessageSubscribersBuilder<MessagePacket<*, *>> { listener ->
        this.subscribeAlways<MessagePacket<*, *>> {
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

private suspend inline operator fun <T : MessagePacket<*, *>> (@MessageDsl suspend T.(String) -> Unit).invoke(t: T) =
    this.invoke(t, t.message.stringValue)

@JvmName("invoke1") //Avoid Platform declaration clash
private suspend inline operator fun <T : MessagePacket<*, *>> AnyReplier<T>.invoke(t: T): Any? =
    this.invoke(t, t.message.stringValue)

/**
 * 消息订阅构造器
 *
 * @see subscribeFriendMessages
 * @sample demo.subscribe.messageDSL
 */
// TODO: 2019/11/29 应定义为 inline, 但这会导致一个 JVM run-time VerifyError. 等待 kotlin 修复 bug
@Suppress("unused")
@MessageDsl
class MessageSubscribersBuilder<T : MessagePacket<*, *>>(
    inline val subscriber: suspend (@MessageDsl suspend T.(String) -> Unit) -> Unit
) {
    /**
     * 无任何触发条件.
     */
    @MessageDsl
    suspend inline fun always(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) {
        content({ true }, onEvent)
    } // TODO: 2019/12/4 这些 onEvent 都应该为 cross-inline, 而这会导致一个 CompilationException

    /**
     * 如果消息内容 `==` [equals], 就执行 [onEvent]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
    @MessageDsl
    suspend inline fun case(
        equals: String,
        trim: Boolean = true,
        ignoreCase: Boolean = false,
        noinline onEvent: @MessageDsl suspend T.(String) -> Unit
    ) {
        val toCheck = if (trim) equals.trim() else equals
        content({ toCheck.equals(if (trim) it.trim() else it, ignoreCase = ignoreCase) }, onEvent)
    }

    /**
     * 如果消息内容包含 [sub], 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun contains(sub: String, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) = content({ sub in it }, onEvent)

    /**
     * 如果消息的前缀是 [prefix], 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun startsWith(
        prefix: String,
        removePrefix: Boolean = true,
        noinline onEvent: @MessageDsl suspend T.(String) -> Unit
    ) =
        content({ it.startsWith(prefix) }) {
            if (removePrefix) this.onEvent(this.message.stringValue.substringAfter(prefix))
            else onEvent(this)
        }

    /**
     * 如果消息的结尾是 [suffix], 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun endsWith(suffix: String, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ it.endsWith(suffix) }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    @MessageDsl
    suspend inline fun sentBy(qqId: Long, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ sender.id == qqId }, onEvent)

    /**
     * 如果是管理员或群主发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun sentByOperator(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ this is GroupMessage && sender.permission.isOperator() }, onEvent)

    /**
     * 如果是管理员发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun sentByAdministrator(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ this is GroupMessage && sender.permission.isAdministrator() }, onEvent)

    /**
     * 如果是群主发的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun sentByOwner(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ this is GroupMessage && sender.permission.isOwner() }, onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun sentFrom(id: Long, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ if (this is GroupMessage) group.id == id else false }, onEvent)

    /**
     * 如果消息内容包含 [M] 类型的 [Message], 就执行 [onEvent]
     */
    @MessageDsl
    suspend inline fun <reified M : Message> has(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (message.any { it::class == M::class }) onEvent(this) }

    /**
     * 如果 [filter] 返回 `true` 就执行 `onEvent`
     */
    @MessageDsl
    suspend inline fun content(noinline filter: T.(String) -> Boolean, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (this.filter(message.stringValue)) onEvent(this) }

    /**
     * 如果消息内容可由正则表达式匹配([Regex.matchEntire]), 就执行 `onEvent`
     */
    @MessageDsl
    suspend inline fun matching(regex: Regex, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) {
        content({ regex.matchEntire(it) != null }, onEvent)
    }

    /**
     * 如果消息内容可由正则表达式查找([Regex.find]), 就执行 `onEvent`
     */
    @MessageDsl
    suspend inline fun finding(regex: Regex, noinline onEvent: @MessageDsl suspend T.(String) -> Unit) {
        content({ regex.find(it) != null }, onEvent)
    }

    /**
     * 若消息内容包含 [this] 则回复 [reply]
     */
    @MessageDsl
    suspend inline infix fun String.containsReply(reply: String) =
        content({ this@containsReply in it }) { this@content.reply(reply) }

    /**
     * 若消息内容包含 [this] 则执行 [replier] 并将其返回值回复给发信对象.
     *
     * [replier] 的 `it` 将会是消息内容 string.
     *
     * @param replier 若返回 [Message] 则直接发送; 若返回 [Unit] 则不回复; 其他情况则 [Any.toString] 后回复
     */
    @MessageDsl
    suspend inline infix fun String.containsReply(noinline replier: AnyReplier<T>) =
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
    suspend inline infix fun Regex.matchingReply(noinline replier: AnyReplier<T>) {
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
    suspend inline infix fun Regex.findingReply(noinline replier: AnyReplier<T>) {
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
    suspend inline infix fun String.startsWithReply(noinline replier: AnyReplier<T>) {
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
    suspend inline infix fun String.endswithReply(noinline replier: AnyReplier<T>) {
        val toCheck = this.trimEnd()
        content({ it.endsWith(this@endswithReply) }) {
            @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
            executeAndReply {
                replier(it.removeSuffix(toCheck).trim())
            }
        }
    }

    @MessageDsl
    suspend inline infix fun String.reply(reply: String) = case(this) {
        this@case.reply(reply)
    }

    @MessageDsl
    suspend inline infix fun String.reply(noinline replier: AnyReplier<T>) = case(this) {
        @Suppress("DSL_SCOPE_VIOLATION_WARNING") // false negative warning
        executeAndReply(replier)
    }

    @PublishedApi
    @Suppress("NOTHING_TO_INLINE")
    internal suspend inline fun T.executeAndReply(noinline replier: AnyReplier<T>) {
        when (val message = replier(this)) {
            is Message -> this.reply(message)
            is Unit -> {

            }
            else -> this.reply(message.toString())
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
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
internal annotation class MessageDsl