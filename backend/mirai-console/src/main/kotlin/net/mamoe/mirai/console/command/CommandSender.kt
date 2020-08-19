/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.console.command

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

/**
 * 指令发送者
 *
 * @see ConsoleCommandSender
 * @see UserCommandSender
 */
@Suppress("FunctionName")
public interface CommandSender {
    /**
     * 与这个 [CommandSender] 相关的 [Bot]. 当通过控制台执行时为 null.
     */
    public val bot: Bot?

    /**
     * 立刻发送一条消息
     */
    @JvmBlockingBridge
    public suspend fun sendMessage(message: Message)
}

/**
 * 可以知道其 [Bot] 的 [CommandSender]
 */
public interface BotAwareCommandSender : CommandSender {
    public override val bot: Bot
}

@JvmSynthetic
public suspend inline fun CommandSender.sendMessage(message: String): Unit = sendMessage(PlainText(message))

/**
 * 控制台指令执行者. 代表由控制台执行指令
 */
// 前端实现
public abstract class ConsoleCommandSender internal constructor() : CommandSender {
    public final override val bot: Nothing? get() = null

    internal companion object {
        internal val instance get() = MiraiConsoleImplementationBridge.consoleCommandSender
    }
}

public fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

public fun Member.asCommandSender(): MemberCommandSender = MemberCommandSender(this)

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
public interface MessageEventContextAware<E : MessageEvent> : MessageEventExtensions<User, Contact> {
    public val fromEvent: E
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see User.asCommandSender
 */
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

    public final override suspend fun sendMessage(message: Message) {
        subject.sendMessage(message)
    }
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
public open class FriendCommandSender(
    public final override val user: Friend
) : UserCommandSender() {
    public override val subject: Contact get() = user
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
public class FriendCommandSenderOnMessage(
    public override val fromEvent: FriendMessageEvent
) :
    FriendCommandSender(fromEvent.sender),
    MessageEventContextAware<FriendMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Contact get() = super.subject
    public override val bot: Bot get() = super.bot
}

/**
 * 代表一个群成员执行指令.
 * @see Member.asCommandSender
 */
public open class MemberCommandSender(
    public final override val user: Member
) : UserCommandSender() {
    public inline val group: Group get() = user.group
    public override val subject: Contact get() = group
}

/**
 * 代表一个群成员在群内执行指令.
 * @see Member.asCommandSender
 */
public class MemberCommandSenderOnMessage(
    public override val fromEvent: GroupMessageEvent
) :
    MemberCommandSender(fromEvent.sender),
    MessageEventContextAware<GroupMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
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
) :
    MemberCommandSender(fromEvent.sender),
    MessageEventContextAware<TempMessageEvent>, MessageEventExtensions<User, Contact> by fromEvent {
    public override val subject: Contact get() = super.subject
    public override val bot: Bot get() = super.bot
}