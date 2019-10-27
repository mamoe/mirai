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
import kotlin.jvm.JvmName

/**
 * 消息事件时创建的临时容器.
 */
abstract class SenderAndMessage<S : Contact>(
    /**
     * 发送这条消息的用户.
     */
    val sender: QQ,
    /**
     * 消息事件主体. 对于好友消息, 这个属性为 [QQ] 的实例;  对于群消息, 这个属性为 [Group] 的实例
     */
    val subject: S,
    val message: MessageChain
) {
    /**
     * 给这个消息事件的主体发送消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend fun reply(message: MessageChain) = subject.sendMessage(message)

    suspend fun reply(plain: String) = reply(PlainText(plain))
    suspend fun reply(message: Message) = reply(message.toChain())
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
@MessageListenerDsl
suspend inline fun subscribeMessages(noinline listeners: suspend MessageSubscribersBuilder<SenderAndMessage<*>>.() -> Unit) {
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
@MessageListenerDsl
suspend inline fun subscribeGroupMessages(noinline listeners: suspend MessageSubscribersBuilder<GroupSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<GroupSenderAndMessage> { listener ->
        subscribeAlways<GroupMessageEvent> {
            listener(GroupSenderAndMessage(it.group, it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自所有 [Bot] 的所有好友消息事件
 */
@MessageListenerDsl
suspend inline fun subscribeFriendMessages(noinline listeners: suspend MessageSubscribersBuilder<FriendSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<FriendSenderAndMessage> { listener ->
        subscribeAlways<FriendMessageEvent> {
            listener(FriendSenderAndMessage(it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有联系人的消息事件. 联系人可以是任意群或任意好友或临时会话.
 */
@MessageListenerDsl
suspend inline fun Bot.subscribeMessages(noinline listeners: suspend MessageSubscribersBuilder<SenderAndMessage<*>>.() -> Unit) {
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
@MessageListenerDsl
suspend inline fun Bot.subscribeGroupMessages(noinline listeners: suspend MessageSubscribersBuilder<GroupSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<GroupSenderAndMessage> { listener ->
        this.subscribeAlways<GroupMessageEvent> {
            listener(GroupSenderAndMessage(it.group, it.sender, it.message))
        }
    }.apply { listeners() }
}

/**
 * 订阅来自这个 [Bot] 的所有好友消息事件.
 */
@MessageListenerDsl
suspend inline fun Bot.subscribeFriendMessages(noinline listeners: suspend MessageSubscribersBuilder<FriendSenderAndMessage>.() -> Unit) {
    MessageSubscribersBuilder<FriendSenderAndMessage> { listener ->
        this.subscribeAlways<FriendMessageEvent> {
            listener(FriendSenderAndMessage(it.sender, it.message))
        }
    }.apply { listeners() }
}

internal typealias MessageListener<T> = @MessageListenerDsl suspend T.(String) -> Unit

internal typealias MessageReplier<T> = @MessageListenerDsl suspend T.(String) -> String

internal suspend inline operator fun <T : SenderAndMessage<*>> MessageListener<T>.invoke(t: T) = this.invoke(t, t.message.toString())
@JvmName("invoke1") //Avoid Platform declaration clash
internal suspend inline operator fun <T : SenderAndMessage<*>> MessageReplier<T>.invoke(t: T) = this.invoke(t, t.message.toString())

/**
 * 消息订阅构造器
 *
 * @see subscribeFriendMessages
 * @sample demo.subscribe.messageDSL
 */
@Suppress("unused")
@MessageListenerDsl
inline class MessageSubscribersBuilder<T : SenderAndMessage<*>>(
    val handlerConsumer: suspend (MessageListener<T>) -> Unit
) {
    suspend inline fun case(equals: String, trim: Boolean = true, noinline listener: MessageListener<T>) = content({ equals == if (trim) it.trim() else it }, listener)
    suspend inline fun contains(value: String, noinline listener: MessageListener<T>) = content({ value in it }, listener)
    suspend inline fun replyEndsWith(value: String, noinline replier: MessageReplier<T>) = content({ it.endsWith(value) }) { replier(this) }
    suspend inline fun startsWith(start: String, noinline listener: MessageListener<T>) = content({ it.startsWith(start) }, listener)
    suspend inline fun endsWith(start: String, noinline listener: MessageListener<T>) = content({ it.endsWith(start) }, listener)
    suspend inline fun sentBy(id: UInt, noinline listener: MessageListener<T>) = content({ sender.id == id }, listener)
    suspend inline fun sentBy(id: Long, noinline listener: MessageListener<T>) = sentBy(id.toUInt(), listener)
    suspend inline fun <reified M : Message> has(noinline listener: MessageListener<T>) = handlerConsumer { if (message.any<M>()) listener(this) }
    suspend inline fun content(noinline filter: T.(String) -> Boolean, noinline listener: MessageListener<T>) =
        handlerConsumer { if (this.filter(message.toString())) listener(this) }


    suspend inline fun replyCase(equals: String, trim: Boolean = true, noinline replier: MessageReplier<T>) = case(equals, trim) { reply(replier(this)) }
    suspend inline fun replyContains(value: String, noinline replier: MessageReplier<T>) = content({ value in it }) { replier(this) }
    suspend inline fun replyStartsWith(value: String, noinline replier: MessageReplier<T>) = content({ it.startsWith(value) }) { replier(this) }

    suspend infix fun String.reply(reply: String) = case(this) { this.reply(reply) }
    suspend infix fun String.reply(reply: MessageReplier<T>) = case(this) { this.reply(reply(this)) }
}


/**
 * DSL 标记. 将能让 IDE 阻止一些错误的方法调用.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@DslMarker
internal annotation class MessageListenerDsl