@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.upload
import kotlin.jvm.JvmName

/**
 * 消息事件时创建的临时容器.
 */
abstract class SenderAndMessage<TContact : Contact>(
    /**
     * 发送这条消息的用户.
     */
    val sender: QQ,
    /**
     * 消息事件主体. 对于好友消息, 这个属性为 [QQ] 的实例;  对于群消息, 这个属性为 [Group] 的实例
     */
    val subject: TContact,
    val message: MessageChain
) {
    /**
     * 给这个消息事件的主体发送消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend inline fun reply(message: MessageChain) = subject.sendMessage(message)

    suspend fun reply(message: Message) = subject.sendMessage(message.singleChain())
    suspend fun reply(plain: String) = subject.sendMessage(plain.toMessage())


    // region Send to subject
    suspend inline fun ExternalImage.send() = this.sendTo(subject)

    suspend inline fun ExternalImage.upload(): Image = this.upload(subject)
    suspend inline fun Image.send() = this.sendTo(subject)
    suspend inline fun ImageId.send() = this.sendTo(subject)
    suspend inline fun Message.send() = this.sendTo(subject)

    // endregion
}

/**
 * [subject] = [sender] = [QQ]
 */
class FriendSenderAndMessage(
    sender: QQ,
    message: MessageChain
) : SenderAndMessage<QQ>(sender, sender, message)

/**
 * [subject] = [group] = [Group]
 */
class GroupSenderAndMessage(
    val group: Group,
    sender: QQ,
    message: MessageChain
) : SenderAndMessage<Group>(sender, group, message)

/**
 * 订阅来自所有 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@MessageDsl
suspend inline fun subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<SenderAndMessage<*>>.() -> Unit) {
    MessageSubscribersBuilder<SenderAndMessage<*>> { listener ->
        subscribeAlways<BotEvent> {
            when (it) {
                is FriendMessageEvent -> listener(FriendSenderAndMessage(it.sender, it.message))
                is GroupMessageEvent -> listener(GroupSenderAndMessage(it.group, it.sender, it.message))
            }
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有群消息事件
 */
@MessageDsl
suspend inline fun subscribeGroupMessages(crossinline listeners: suspend MessageSubscribersBuilder<GroupSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<GroupSenderAndMessage> { listener ->
        subscribeAlways<GroupMessageEvent> {
            listener(GroupSenderAndMessage(it.group, it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 */
@MessageDsl
suspend inline fun subscribeFriendMessages(crossinline listeners: suspend MessageSubscribersBuilder<FriendSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<FriendSenderAndMessage> { listener ->
        subscribeAlways<FriendMessageEvent> {
            listener(FriendSenderAndMessage(it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@MessageDsl
suspend inline fun Bot.subscribeMessages(crossinline listeners: suspend MessageSubscribersBuilder<SenderAndMessage<*>>.() -> Unit) {
    MessageSubscribersBuilder<SenderAndMessage<*>> { listener ->
        this.subscribeAlways<BotEvent> {
            when (it) {
                is FriendMessageEvent -> listener(FriendSenderAndMessage(it.sender, it.message))
                is GroupMessageEvent -> listener(GroupSenderAndMessage(it.group, it.sender, it.message))
            }
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有群消息事件
 */
@MessageDsl
suspend inline fun Bot.subscribeGroupMessages(crossinline listeners: suspend MessageSubscribersBuilder<GroupSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<GroupSenderAndMessage> { listener ->
        this.subscribeAlways<GroupMessageEvent> {
            listener(GroupSenderAndMessage(it.group, it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有好友消息事件.
 */
@MessageDsl
suspend inline fun Bot.subscribeFriendMessages(crossinline listeners: suspend MessageSubscribersBuilder<FriendSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<FriendSenderAndMessage> { listener ->
        this.subscribeAlways<FriendMessageEvent> {
            listener(FriendSenderAndMessage(it.sender, it.message))
        }
    }.apply { listeners() }
}

internal typealias MessageReplier<T> = @MessageDsl suspend T.(String) -> Message

internal typealias StringReplier<T> = @MessageDsl suspend T.(String) -> String

internal suspend inline operator fun <T : SenderAndMessage<*>> (@MessageDsl suspend T.(String) -> Unit).invoke(t: T) =
    this.invoke(t, t.message.stringValue)

@JvmName("invoke1") //Avoid Platform declaration clash
internal suspend inline operator fun <T : SenderAndMessage<*>> StringReplier<T>.invoke(t: T): String =
    this.invoke(t, t.message.stringValue)

/**
 * 消息订阅构造器
 *
 * @see subscribeFriendMessages
 * @sample demo.subscribe.messageDSL
 */
@Suppress("unused")
@MessageDsl
class MessageSubscribersBuilder<T : SenderAndMessage<*>>(
    inline val subscriber: suspend (@MessageDsl suspend T.(String) -> Unit) -> Unit
) {
    /**
     * 如果消息内容 `==` [equals], 就执行 [onEvent]
     * @param trim `true` 则删除首尾空格后比较
     * @param ignoreCase `true` 则不区分大小写
     */
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
    suspend fun contains(sub: String, onEvent: @MessageDsl suspend T.(String) -> Unit) = content({ sub in it }, onEvent)

    /**
     * 如果消息的前缀是 [prefix], 就执行 [onEvent]
     */
    suspend fun startsWith(
        prefix: String,
        removePrefix: Boolean = false,
        onEvent: @MessageDsl suspend T.(String) -> Unit
    ) =
        content({ it.startsWith(prefix) }) {
            if (removePrefix) this.onEvent(this.message.stringValue.substringAfter(prefix))
            else onEvent(this)
        }

    /**
     * 如果消息的结尾是 [suffix], 就执行 [onEvent]
     */
    suspend fun endsWith(suffix: String, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ it.endsWith(suffix) }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    suspend fun sentBy(qqId: UInt, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ sender.id == qqId }, onEvent)

    /**
     * 如果是这个人发的消息, 就执行 [onEvent]. 消息可以是好友消息也可以是群消息
     */
    suspend fun sentBy(qqId: Long, onEvent: @MessageDsl suspend T.(String) -> Unit) = sentBy(qqId.toUInt(), onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    suspend fun sentFrom(id: UInt, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        content({ if (this is GroupSenderAndMessage) group.id == id else false }, onEvent)

    /**
     * 如果是来自这个群的消息, 就执行 [onEvent]
     */
    suspend fun sentFrom(id: Long, onEvent: @MessageDsl suspend T.(String) -> Unit) = sentFrom(id.toUInt(), onEvent)

    /**
     * 如果消息内容包含 [M] 类型的 [Message], 就执行 [onEvent]
     */
    suspend inline fun <reified M : Message> has(noinline onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (message.any<M>()) onEvent(this) }

    /**
     * 如果 [filter] 返回 `true` 就执行 `onEvent`
     */
    suspend fun content(filter: T.(String) -> Boolean, onEvent: @MessageDsl suspend T.(String) -> Unit) =
        subscriber { if (this.filter(message.stringValue)) onEvent(this) }

    suspend infix fun String.caseReply(replier: String) = case(this, true) { this@case.reply(replier) }
    suspend infix fun String.caseReply(replier: StringReplier<T>) = case(this, true) { this@case.reply(replier(this)) }
    suspend infix fun String.containsReply(replier: String) =
        content({ this@containsReply in it }) { this@content.reply(replier) }

    suspend infix fun String.containsReply(replier: StringReplier<T>) =
        content({ this@containsReply in it }) { replier(this) }

    suspend infix fun String.startsWithReply(replier: StringReplier<T>) =
        content({ it.startsWith(this@startsWithReply) }) { replier(this) }

    suspend infix fun String.endswithReply(replier: StringReplier<T>) =
        content({ it.endsWith(this@endswithReply) }) { replier(this) }

    suspend infix fun String.reply(reply: String) = case(this) {
        this@case.reply(reply)
    }

    suspend infix fun String.reply(reply: StringReplier<T>) = case(this) { this@case.reply(reply(this)) }


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


fun main() {

    println('B')
    println("\u7154225")
    println('B' - 'b')
}