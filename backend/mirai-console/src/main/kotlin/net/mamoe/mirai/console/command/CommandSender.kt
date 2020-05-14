/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

/**
 * 指令发送者
 *
 * @see AbstractCommandSender 请继承于该抽象类
 */
interface CommandSender {
    /**
     * 与这个 [CommandSender] 相关的 [Bot]. 当通过控制台执行时为 null.
     */
    val bot: Bot?

    /**
     * 立刻发送一条消息
     */
    suspend fun sendMessage(message: Message)

    /**
     * 写入要发送的内容 所有内容最后会被以一条发出
     */
    fun appendMessage(message: String)

    fun sendMessageBlocking(messageChain: Message) = runBlocking { sendMessage(messageChain) }
    fun sendMessageBlocking(message: String) = runBlocking { sendMessage(message) }
}

suspend inline fun CommandSender.sendMessage(message: String) = sendMessage(PlainText(message))

abstract class AbstractCommandSender : CommandSender {
    internal val builder = StringBuilder()

    override fun appendMessage(message: String) {
        builder.appendln(message)
    }

    internal open suspend fun flushMessage() {
        if (builder.isNotEmpty()) {
            sendMessage(builder.toString().removeSuffix("\n"))
        }
    }
}

/**
 * 控制台指令执行者. 代表由控制台执行指令
 */
object ConsoleCommandSender : AbstractCommandSender() {
    override val bot: Nothing? get() = null

    override suspend fun sendMessage(message: Message) {
        TODO()
        // MiraiConsole.logger("[Command]", 0, messageChain.toString())
    }

    override suspend fun flushMessage() {
        super.flushMessage()
        builder.clear()
    }
}

inline fun Friend.asCommandSender(): FriendCommandSender = FriendCommandSender(this)

inline fun Member.asCommandSender(): MemberCommandSender = MemberCommandSender(this)

inline fun User.asCommandSender(): UserCommandSender {
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
sealed class UserCommandSender : AbstractCommandSender() {
    abstract val user: User

    final override val bot: Bot get() = user.bot

    final override suspend fun sendMessage(message: Message) {
        user.sendMessage(message)
    }
}

/**
 * 代表一个用户私聊机器人执行指令
 * @see Friend.asCommandSender
 */
class FriendCommandSender(override val user: Friend) : UserCommandSender()

/**
 * 代表一个群成员在群内执行指令.
 * @see Member.asCommandSender
 */
class MemberCommandSender(override val user: Member) : UserCommandSender() {
    inline val group: Group get() = user.group
}