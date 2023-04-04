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
import net.mamoe.mirai.utils.toUHexString
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

@Suppress("SpellCheckingInspection")
private val charMappings = mapOf(
    '\n' to """\n""",
    '\r' to "",

    // region Control Characters https://en.wikipedia.org/wiki/Control_character https://en.wikipedia.org/wiki/Unicode_control_characters

    // ASCII
    '\u0000' to "<NUL>",
    '\u0001' to "<SOH>",
    '\u0002' to "<STX>",
    '\u0003' to "<ETX>",
    '\u0004' to "<EOT>",
    '\u0005' to "<ENQ>",
    '\u0006' to "<ACK>",
    '\u0007' to "<BEL>",
    '\u0008' to "<BS>",
    '\u0009' to "<HT>",
    // '\u000a' to "<LF>", // \n
    '\u000b' to "<VT>",
    '\u000c' to "<FF>",
    // '\u000d' to "<CR>", // \r
    '\u000e' to "<SO>",
    '\u000F' to "<SI>",
    '\u0010' to "<DLE>",
    '\u0011' to "<DC1>",
    '\u0012' to "<DC2>",
    '\u0013' to "<DC3>",
    '\u0014' to "<DC4>",
    '\u0015' to "<NAK>",
    '\u0016' to "<SYN>",
    '\u0017' to "<ETB>",
    '\u0018' to "<CAN>",
    '\u0019' to "<EM>",
    '\u001a' to "<SUB>",
    '\u001b' to "<ESC>",
    '\u001c' to "<FS>",
    '\u001d' to "<GS>",
    '\u001e' to "<RS>",
    '\u001f' to "<US>",

    '\u007F' to "<DEL>",
    '\u0085' to "<NEL>",

    // Unicode Control Characters - Bidirectional text control
    // https://en.wikipedia.org/wiki/Unicode_control_characters#Bidirectional_text_control

    '\u061C' to "<ALM>",
    '\u200E' to "<LTRM>",
    '\u200F' to "<RTLM>",
    '\u202A' to "<LTRE>",
    '\u202B' to "<RTLE>",
    '\u202C' to "<PDF>",
    '\u202D' to "<LTR>",
    '\u202E' to "<RTL>",
    '\u2066' to "<LTRI>",
    '\u2067' to "<RTLI>",
    '\u2068' to "<FSI>",
    '\u2069' to "<PDI>",
    // endregion

)

private val regionMappings: Map<IntRange, StringBuilder.(Char) -> Unit> = mapOf(
    0x0080..0x009F to { // https://en.wikipedia.org/wiki/Control_character#In_Unicode
        append("<control-").append(it.code.toUHexString()).append(">")
    },
)

internal fun String.applyCharMapping() = buildString(capacity = this.length) {
    this@applyCharMapping.forEach { char ->

        charMappings[char]?.let { append(char); return@forEach }

        regionMappings.entries.find { char.code in it.key }?.let { mapping ->
            mapping.value.invoke(this@buildString, char)
            return@forEach
        }

        append(char)
    }
}

internal fun String.replaceMagicCodes(): String = this
    .applyCharMapping()


internal fun Message.takeContent(length: Int): String =
    this.toMessageChain().joinToString("", limit = length) { it.content }
