/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.MessageSendEvent
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.qqandroid.asQQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToFriendImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.message.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.utils.verbose

internal suspend fun <T : Contact> Friend.sendMessageImpl(generic: T, message: Message): MessageReceipt<T> {
    val event = MessageSendEvent.FriendMessageSendEvent(this, message.asMessageChain()).broadcast()
    if (event.isCancelled) {
        throw EventCancelledException("cancelled by FriendMessageSendEvent")
    }
    event.message.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()
    lateinit var source: MessageSourceToFriendImpl
    (bot.network as QQAndroidBotNetworkHandler).run {
        check(
            MessageSvcPbSendMsg.createToFriend(
                bot.asQQAndroidBot().client,
                this@sendMessageImpl,
                event.message
            ) {
                source = it
            }.sendAndExpect<MessageSvcPbSendMsg.Response>() is MessageSvcPbSendMsg.Response.SUCCESS
        ) { "send message failed" }
    }
    return MessageReceipt(source, generic, null)
}

internal fun Contact.logMessageSent(message: Message) {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    if (message !is net.mamoe.mirai.message.data.LongMessage) {
        bot.logger.verbose("$this <- ${message.toString().singleLine()}")
    }
}

internal fun MessageEvent.logMessageReceived() {
    when (this) {
        is GroupMessageEvent -> bot.logger.verbose {
            "[${group.name.singleLine()}(${group.id})] ${senderName.singleLine()}(${sender.id}) -> ${message.toString()
                .singleLine()}"
        }
        is TempMessageEvent -> bot.logger.verbose {
            "[${group.name.singleLine()}(${group.id})] ${senderName.singleLine()}(Temp ${sender.id}) -> ${message.toString()
                .singleLine()}"
        }
        is FriendMessageEvent -> bot.logger.verbose {
            "${sender.nick.singleLine()}(${sender.id}) -> ${message.toString().singleLine()}"
        }
    }
}

internal fun String.singleLine(): String {
    return this.replace("\n", """\n""").replace("\r", "")
}