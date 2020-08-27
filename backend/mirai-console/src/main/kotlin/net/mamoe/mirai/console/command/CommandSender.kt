/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.description.CommandArgumentParserException
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

/**
 * 指令发送者.
 *
 * ### 获得指令发送者
 * - [MessageEvent.toCommandSender]
 * - [FriendMessageEvent.toCommandSender]
 * - [GroupMessageEvent.toCommandSender]
 * - [TempMessageEvent.toCommandSender]
 *
 * - [Member.asCommandSender]
 * - [Friend.asCommandSender]
 * - [User.asCommandSender]
 *
 * ### 子类型
 *
 * 当真实收到由用户执行的指令时:
 * - 若用户在群内指令执行, 对应 [CommandSender] 为 [MemberCommandSenderOnMessage]
 * - 若用户在私聊环境内指令执行, 对应 [CommandSender] 为 [FriendCommandSenderOnMessage]
 * - 若用户在临时会话内指令执行, 对应 [CommandSender] 为 [TempCommandSenderOnMessage]
 *
 * 当指令由其他插件主动执行时, 插件应使用 [toCommandSender] 或 [asCommandSender], 因此
 * - 若用户在群内指令执行, 对应 [CommandSender] 为 [MemberCommandSender]
 * - 若用户在私聊环境内指令执行, 对应 [CommandSender] 为 [FriendCommandSender]
 * - 若用户在临时会话内指令执行, 对应 [CommandSender] 为 [TempCommandSender]
 *
 * @see ConsoleCommandSender 控制台
 * @see UserCommandSender  [User] ([群成员][Member], [好友][Friend])
 * @see toCommandSender
 * @see asCommandSender
 */
@Suppress("FunctionName")
public interface CommandSender {
    /**
     * 与这个 [CommandSender] 相关的 [Bot]. 当通过控制台执行时为 null.
     */
    public val bot: Bot?

    /**
     * 获取好友昵称, 群员昵称, 或 Bot 的昵称. 当控制台发送消息时返回 [ConsoleCommandSender.NAME]
     */
    public val name: String

    /**
     * 立刻发送一条消息. 对于 [Member.asCommandSender], 这个函数总是发送给所在群
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message)

    /**
     * 立刻发送一条消息. 对于 [Member.asCommandSender], 这个函数总是发送给所在群
     */
    @JvmDefault
    @JvmBlockingBridge
    public suspend fun sendMessage(message: String): Unit = sendMessage(PlainText(message))

    @ConsoleExperimentalAPI
    public suspend fun catchExecutionException(e: Throwable) {
        if (this is CommandSenderOnMessage<*>) {
            // TODO: 2020/8/22 bad scope
            val cause = e.rootCauseOrSelf

            val message = cause
                .takeIf { it is CommandArgumentParserException }?.message
                ?: "${cause::class.simpleName.orEmpty()}: ${cause.message}"

            sendMessage(message) // \n\n60 秒内发送 stacktrace 查看堆栈信息
            bot.launch(CoroutineName("stacktrace delayer from command")) {
                if (fromEvent.nextMessageOrNull(60_000) {
                        it.message.contentEquals("stacktrace") || it.message.contentEquals("stack")
                    } != null) {
                    sendMessage(e.stackTraceToString())
                }
            }
        } else {
            sendMessage(e.stackTraceToString())
        }
    }
}

internal val Throwable.rootCauseOrSelf: Throwable get() = generateSequence(this) { it.cause }.lastOrNull() ?: this

/**
 * 可以知道其 [Bot] 的 [CommandSender]
 */
public interface BotAwareCommandSender : CommandSender {
    public override val bot: Bot
}

/**
 * 可以知道其 [Group] 环境的 [CommandSender]
 */
public interface GroupAwareCommandSender : CommandSender {
    public val group: Group
}

/**
 * 控制台指令执行者. 代表由控制台执行指令
 */
// 前端实现
public abstract class ConsoleCommandSender internal constructor() : CommandSender {
    public final override val bot: Nothing? get() = null
    public override val name: String get() = NAME

    public companion object {
        public const val NAME: String = "CONSOLE"

        internal val instance get() = MiraiConsoleImplementationBridge.consoleCommandSender
    }
}

@ConsoleExperimentalAPI
public fun FriendMessageEvent.toCommandSender(): FriendCommandSenderOnMessage = FriendCommandSenderOnMessage(this)

@ConsoleExperimentalAPI
public fun GroupMessageEvent.toCommandSender(): MemberCommandSenderOnMessage = MemberCommandSenderOnMessage(this)

@ConsoleExperimentalAPI
public fun TempMessageEvent.toCommandSender(): TempCommandSenderOnMessage = TempCommandSenderOnMessage(this)

@ConsoleExperimentalAPI
public fun MessageEvent.toCommandSender(): CommandSenderOnMessage<*> = when (this) {
    is FriendMessageEvent -> toCommandSender()
    is GroupMessageEvent -> toCommandSender()
    is TempMessageEvent -> toCommandSender()
    else -> throw IllegalArgumentException("unsupported MessageEvent: ${this::class.qualifiedNameOrTip}")
}

@ConsoleExperimentalAPI
public fun Member.asCommandSender(): MemberCommandSender = MemberCommandSender(this)

@ConsoleExperimentalAPI
public fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

@ConsoleExperimentalAPI
public fun User.asCommandSender(): UserCommandSender {
    return when (this) {
        is Friend -> this.asCommandSender()
        is Member -> this.asCommandSender()
        else -> error("stub")
    }
}

/**
 * 表示由 [MessageEvent] 触发的指令
 */
@ConsoleExperimentalAPI
public interface MessageEventContextAware<E : MessageEvent> : MessageEventExtensions<User, Contact> {
    public val fromEvent: E
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see User.asCommandSender
 */
@ConsoleExperimentalAPI
public sealed class UserCommandSender : CommandSender, BotAwareCommandSender {
    /**
     * @see MessageEvent.sender
     */
    public abstract val user: User

    /**
     * @see MessageEvent.subject
     */
    public abstract val subject: Contact

    public override val bot: Bot get() = user.bot
    public override val name: String get() = user.nameCardOrNick

    public final override suspend fun sendMessage(message: Message) {
        subject.sendMessage(message)
    }
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
@ConsoleExperimentalAPI
public open class FriendCommandSender(
    public final override val user: Friend
) : UserCommandSender() {
    public override val subject: Contact get() = user
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
@ConsoleExperimentalAPI
public class FriendCommandSenderOnMessage(
    public override val fromEvent: FriendMessageEvent
) : FriendCommandSender(fromEvent.sender),
    CommandSenderOnMessage<FriendMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Contact get() = super.subject
    public override val bot: Bot get() = super.bot
}

@ConsoleExperimentalAPI
public interface CommandSenderOnMessage<T : MessageEvent> : MessageEventContextAware<T>, CommandSender

/**
 * 代表一个群成员执行指令.
 * @see Member.asCommandSender
 */
@ConsoleExperimentalAPI
public open class MemberCommandSender(
    public final override val user: Member
) : UserCommandSender(), GroupAwareCommandSender {
    public override val group: Group get() = user.group
    public override val subject: Contact get() = group
}

/**
 * 代表一个群成员执行指令.
 * @see Member.asCommandSender
 */
@ConsoleExperimentalAPI
public open class TempCommandSender(
    public final override val user: Member
) : UserCommandSender(), GroupAwareCommandSender {
    public override val group: Group get() = user.group
    public override val subject: Contact get() = group
}

/**
 * 代表一个群成员在群内执行指令.
 * @see Member.asCommandSender
 */
@ConsoleExperimentalAPI
public class MemberCommandSenderOnMessage(
    public override val fromEvent: GroupMessageEvent
) : MemberCommandSender(fromEvent.sender),
    CommandSenderOnMessage<GroupMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Contact get() = super.subject
    public override val bot: Bot get() = super.bot
}

/**
 * 代表一个群成员通过临时会话私聊机器人执行指令.
 * @see Member.asCommandSender
 */
@ConsoleExperimentalAPI
public class TempCommandSenderOnMessage(
    public override val fromEvent: TempMessageEvent
) : TempCommandSender(fromEvent.sender),
    CommandSenderOnMessage<TempMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Contact get() = super.subject
    public override val bot: Bot get() = super.bot
}