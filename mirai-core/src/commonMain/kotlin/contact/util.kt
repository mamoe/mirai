/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.internal.message.LongMessageInternal
import net.mamoe.mirai.internal.utils.estimateLength
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.verbose

internal inline val Group.uin: Long get() = this.cast<GroupImpl>().uin
internal inline val Group.groupCode: Long get() = this.id
internal inline val User.uin: Long get() = this.id
internal inline val Bot.uin: Long get() = this.id

internal fun Contact.logMessageSent(message: Message) {
    if (message !is LongMessageInternal) {
        bot.logger.verbose("$this <- $message".replaceMagicCodes())
    }
}

internal fun MessageChain.countImages(): Int = this.count { it is Image }

@Throws(MessageTooLargeException::class)
internal fun MessageChain.checkLength(
    originalMessage: Message, target: Contact,
): Int {
    val chain = this
    val length = estimateLength(target, 15001)
    if (length > 15000 || countImages() > 50) {
        throw MessageTooLargeException(
            target, originalMessage, this,
            "message(${
                chain.joinToString("", limit = 10)
            }) is too large. Allow up to 50 images or 5000 chars"
        )
    }
    return length
}

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

internal inline fun <reified T : MessageContent> Message.takeSingleContent(): T? {
    return this as? T ?: this.castOrNull<MessageChain>()?.findIsInstance()
}
