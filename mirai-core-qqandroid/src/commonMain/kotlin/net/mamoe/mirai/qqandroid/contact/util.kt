/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.FriendMessagePostSendEvent
import net.mamoe.mirai.event.events.FriendMessagePreSendEvent
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.asQQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToFriendImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.createToFriend
import net.mamoe.mirai.utils.verbose
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal suspend fun <T : User> Friend.sendMessageImpl(
    message: Message,
    friendReceiptConstructor: (MessageSourceToFriendImpl) -> MessageReceipt<Friend>,
    tReceiptConstructor: (MessageSourceToFriendImpl) -> MessageReceipt<T>
): MessageReceipt<T> {
    contract { callsInPlace(friendReceiptConstructor, InvocationKind.EXACTLY_ONCE) }
    val bot = bot.asQQAndroidBot()

    val chain = kotlin.runCatching {
        FriendMessagePreSendEvent(this, message).broadcast()
    }.onSuccess {
        check(!it.isCancelled) {
            throw EventCancelledException("cancelled by FriendMessagePreSendEvent")
        }
    }.getOrElse {
        throw EventCancelledException("exception thrown when broadcasting FriendMessagePreSendEvent", it)
    }.message.asMessageChain()

    chain.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()

    lateinit var source: MessageSourceToFriendImpl
    val result = bot.network.runCatching {
        MessageSvcPbSendMsg.createToFriend(
            bot.client,
            this@sendMessageImpl,
            chain
        ) {
            source = it
        }.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
            check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                "Send temp message failed: $it"
            }
        }
        friendReceiptConstructor(source)
    }

    result.fold(
        onSuccess = {
            FriendMessagePostSendEvent(this, chain, null, it)
        },
        onFailure = {
            FriendMessagePostSendEvent(this, chain, it, null)
        }
    ).broadcast()

    result.getOrThrow()
    return tReceiptConstructor(source)
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