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
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.MessageSendEvent
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.LongMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.qqandroid.asQQAndroidBot
import net.mamoe.mirai.qqandroid.message.MessageSourceToFriendImpl
import net.mamoe.mirai.qqandroid.message.ensureSequenceIdAvailable
import net.mamoe.mirai.qqandroid.message.firstIsInstanceOrNull
import net.mamoe.mirai.qqandroid.network.QQAndroidBotNetworkHandler
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.verbose

@OptIn(MiraiInternalAPI::class)
internal suspend fun QQ.sendMessageImpl(message: Message): MessageReceipt<QQ> {
    val event = MessageSendEvent.FriendMessageSendEvent(this, message.asMessageChain()).broadcast()
    if (event.isCancelled) {
        throw EventCancelledException("cancelled by FriendMessageSendEvent")
    }
    event.message.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()
    lateinit var source: MessageSourceToFriendImpl
    (bot.network as QQAndroidBotNetworkHandler).run {
        check(
            MessageSvc.PbSendMsg.createToFriend(
                bot.asQQAndroidBot().client,
                this@sendMessageImpl,
                event.message
            ) {
                source = it
            }.sendAndExpect<MessageSvc.PbSendMsg.Response>() is MessageSvc.PbSendMsg.Response.SUCCESS
        ) { "send message failed" }
    }
    return MessageReceipt(source, this, null)
}

@OptIn(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
internal fun Contact.logMessageSent(message: Message) {
    if (message !is LongMessage) {
        bot.logger.verbose("$this <- ${message.toString().singleLine()}")
    }
}

@OptIn(MiraiInternalAPI::class, MiraiExperimentalAPI::class)
internal fun ContactMessage.logMessageReceived() {
    when (this) {
        is GroupMessage -> bot.logger.verbose {
            "[${group.name.singleLine()}(${group.id})] ${senderName.singleLine()}(${sender.id}) -> ${message.toString()
                .singleLine()}"
        }
        is TempMessage -> bot.logger.verbose {
            "[${group.name.singleLine()}(${group.id})] ${senderName.singleLine()}(Temp ${sender.id}) -> ${message.toString()
                .singleLine()}"
        }
        is FriendMessage -> bot.logger.verbose {
            "${sender.nick.singleLine()}(${sender.id}) -> ${message.toString().singleLine()}"
        }
    }
}


internal fun String.singleLine(): String {
    return this.replace("\n", """\n""").replace("\r", "")
}


/**
 * Size management isn't atomic.
 */
internal class LockFreeCacheList<E>(private val maxSize: Int) : LockFreeLinkedList<E>() {
    override fun addLast(element: E) {
        if (size >= maxSize) {
            this.removeFirst()
        }

        super.addLast(element)
    }

    @Deprecated("prohibited", level = DeprecationLevel.HIDDEN)
    override fun addAll(iterable: Iterable<E>) {
        super.addAll(iterable)
    }

    @Deprecated("prohibited", level = DeprecationLevel.HIDDEN)
    override fun addAll(iterable: Sequence<E>) {
        super.addAll(iterable)
    }
}