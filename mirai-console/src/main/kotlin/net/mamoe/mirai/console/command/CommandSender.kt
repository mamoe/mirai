/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.data.Message

/**
 * 指令发送者
 *
 * @see AbstractCommandSender 请继承于该抽象类
 */
interface CommandSender {
    /**
     * 立刻发送一条消息
     */
    suspend fun sendMessage(messageChain: Message)

    suspend fun sendMessage(message: String)
    /**
     * 写入要发送的内容 所有内容最后会被以一条发出
     */
    fun appendMessage(message: String)

    fun sendMessageBlocking(messageChain: Message) = runBlocking { sendMessage(messageChain) }
    fun sendMessageBlocking(message: String) = runBlocking { sendMessage(message) }
}

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
    override suspend fun sendMessage(messageChain: Message) {
        MiraiConsole.logger("[Command]", 0, messageChain.toString())
    }

    override suspend fun sendMessage(message: String) {
        MiraiConsole.logger("[Command]", 0, message)
    }

    override suspend fun flushMessage() {
        super.flushMessage()
        builder.clear()
    }
}

/**
 * 联系人指令执行者. 代表由一个 QQ 用户私聊执行指令
 */
@Suppress("MemberVisibilityCanBePrivate")
open class ContactCommandSender(val contact: Contact) : AbstractCommandSender() {
    override suspend fun sendMessage(messageChain: Message) {
        contact.sendMessage(messageChain)
    }

    override suspend fun sendMessage(message: String) {
        contact.sendMessage(message)
    }
}

/**
 * 联系人指令执行者. 代表由一个 QQ 用户 在群里执行指令
 */
open class GroupContactCommandSender(
    val realSender: Member,
    subject: Contact
):ContactCommandSender(subject)