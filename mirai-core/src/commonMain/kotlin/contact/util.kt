/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.verbose

internal inline val Group.uin: Long get() = this.cast<GroupImpl>().uin
internal inline val Group.groupCode: Long get() = this.id
internal inline val User.uin: Long get() = this.id
internal inline val Bot.uin: Long get() = this.id

@Suppress("RemoveRedundantQualifierName") // compiler bug
internal fun net.mamoe.mirai.event.events.MessageEvent.logMessageReceived() {
    fun renderMessage(message: MessageChain): String {
        return message.filterNot { it is MessageSource }.joinToString("")
    }

    fun renderGroupMessage(group: Group, senderName: String, sender: Member, message: MessageChain): String {
        val displayId = if (sender is AnonymousMember) "匿名" else sender.id.toString()
        return "[${group.name}(${group.id})] ${senderName}($displayId) -> ${renderMessage(message)}"
    }

    fun renderGroupMessageSync(group: Group, message: MessageChain): String {
        return "[${group.name}(${group.id})][SYNC] <- ${renderMessage(message)}"
    }

    fun renderGroupTempMessage(group: Group, senderName: String, sender: Member, message: MessageChain): String {
        return "[${group.name}(${group.id})] $senderName(Temp ${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderGroupTempMessageSync(group: Group, subjectName: String, subject: Member, message: MessageChain): String {
        return "[${group.name}(${group.id})] $subjectName(Temp ${subject.id})[SYNC] <- ${renderMessage(message)}"
    }

    fun renderStrangerMessage(senderName: String, sender: User, message: MessageChain): String {
        return "[$senderName(Stranger ${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderStrangerMessageSync(subjectName: String, subject: User, message: MessageChain): String {
        return "[$subjectName(Stranger ${subject.id})[SYNC] <- ${renderMessage(message)}"
    }

    fun renderFriendMessage(sender: User, message: MessageChain): String {
        return "${sender.nick}(${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderFriendMessageSync(subject: User, message: MessageChain): String {
        return "${subject.nick}(${subject.id})[SYNC] <- ${renderMessage(message)}"
    }

    fun renderOtherClientMessage(client: OtherClient): String {
        return "${client.platform} -> ${renderMessage(message)}"
    }

    fun renderGuildMessage(
        guild: Guild,
        channel: Channel,
        senderName: String,
        sender: GuildMember,
        message: MessageChain
    ): String {
        return "[${guild.name}(${guild.id})] [${channel.name}(${channel.id})] ${senderName}(${sender.id}) -> ${
            renderMessage(
                message
            )
        }"
    }

    fun renderGuildMessageSync(
        guild: Guild,
        channel: Channel,
        sender: GuildMember,
        message: MessageChain
    ): String {
        return "[${guild.name}(${guild.id})] [${channel.name}(${channel.id})] ${senderName}(${sender.id})[SYNC] <- ${
            renderMessage(
                message
            )
        }"
    }

    bot.logger.verbose {
        when (this) {
            is net.mamoe.mirai.event.events.GroupMessageEvent ->
                renderGroupMessage(group, senderName, sender, message)

            is net.mamoe.mirai.event.events.GroupMessageSyncEvent ->
                renderGroupMessageSync(group, message)

            is net.mamoe.mirai.event.events.GroupTempMessageEvent ->
                renderGroupTempMessage(group, senderName, sender, message)

            is net.mamoe.mirai.event.events.GroupTempMessageSyncEvent ->
                renderGroupTempMessageSync(group, subject.nameCardOrNick, subject, message)

            is net.mamoe.mirai.event.events.StrangerMessageEvent ->
                renderStrangerMessage(senderName, sender, message)

            is net.mamoe.mirai.event.events.StrangerMessageSyncEvent ->
                renderStrangerMessageSync(subject.nick, subject, message)

            is net.mamoe.mirai.event.events.FriendMessageEvent ->
                renderFriendMessage(sender, message)

            is net.mamoe.mirai.event.events.FriendMessageSyncEvent ->
                renderFriendMessageSync(subject, message)

            is net.mamoe.mirai.event.events.OtherClientMessageEvent ->
                renderOtherClientMessage(client)

            is net.mamoe.mirai.event.events.GuildMessageEvent ->
                renderGuildMessage(guild, channel, sender.nameCard, sender, message)

            is net.mamoe.mirai.event.events.GuildMessageSyncEvent ->
                renderGuildMessageSync(guild, channel, sender, message)

            else -> toString()
        }.replaceMagicCodes() // group name & sender nick & message
    }
}

internal val charMappings = mapOf(
    '\n' to """\n""",
    '\r' to "",
    '\u202E' to "<RTL>",
    '\u202D' to "<LTR>",
)

internal fun String.applyCharMapping() = buildString(capacity = this.length) {
    this@applyCharMapping.forEach { char ->
        append(charMappings[char] ?: char)
    }
}

internal fun String.replaceMagicCodes(): String = this
    .applyCharMapping()


internal fun Message.takeContent(length: Int): String =
    this.toMessageChain().joinToString("", limit = length) { it.content }
