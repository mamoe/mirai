/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleInternal
import net.mamoe.mirai.console.utils.JavaFriendlyAPI
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

/**
 * 指令发送者
 *
 * @see ConsoleCommandSender
 * @see UserCommandSender
 */
@Suppress("FunctionName")
interface CommandSender {
    /**
     * 与这个 [CommandSender] 相关的 [Bot]. 当通过控制台执行时为 null.
     */
    val bot: Bot?

    /**
     * 立刻发送一条消息
     */
    @JvmSynthetic
    suspend fun sendMessage(message: Message)

    @JvmDefault
    @JavaFriendlyAPI
    @JvmName("sendMessage")
    fun __sendMessageBlocking(messageChain: Message) = runBlocking { sendMessage(messageChain) }

    @JvmDefault
    @JavaFriendlyAPI
    @JvmName("sendMessage")
    fun __sendMessageBlocking(message: String) = runBlocking { sendMessage(message) }
}

/**
 * 可以知道其 [Bot] 的 [CommandSender]
 */
interface BotAwareCommandSender : CommandSender {
    override val bot: Bot
}

suspend inline fun CommandSender.sendMessage(message: String) = sendMessage(PlainText(message))

/**
 * 控制台指令执行者. 代表由控制台执行指令
 */
// 前端实现
abstract class ConsoleCommandSender internal constructor() : CommandSender {
    final override val bot: Nothing? get() = null

    companion object {
        internal val instance get() = MiraiConsoleInternal.consoleCommandSender
    }
}

fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

fun Member.asCommandSender(): MemberCommandSender = MemberCommandSender(this)

fun User.asCommandSender(): UserCommandSender {
    return when (this) {
        is Friend -> this.asCommandSender()
        is Member -> this.asCommandSender()
        else -> error("stub")
    }
}


/**
 * 代表一个用户私聊机器人执行指令
 * @see User.asCommandSender
 */
sealed class UserCommandSender : CommandSender, BotAwareCommandSender {
    /**
     * @see MessageEvent.sender
     */
    abstract val user: User

    /**
     * @see MessageEvent.subject
     */
    abstract val subject: Contact

    final override val bot: Bot get() = user.bot

    final override suspend fun sendMessage(message: Message) {
        subject.sendMessage(message)
    }
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
class FriendCommandSender(override val user: Friend) : UserCommandSender() {
    override val subject: Contact get() = user
}

/**
 * 代表一个群成员在群内执行指令.
 * @see Member.asCommandSender
 */
class MemberCommandSender(override val user: Member) : UserCommandSender() {
    inline val group: Group get() = user.group
    override val subject: Contact get() = group
}