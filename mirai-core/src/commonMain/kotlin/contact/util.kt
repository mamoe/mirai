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
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.asQQAndroidBot
import net.mamoe.mirai.internal.message.LongMessageInternal
import net.mamoe.mirai.internal.message.OnlineMessageSourceToFriendImpl
import net.mamoe.mirai.internal.message.OnlineMessageSourceToStrangerImpl
import net.mamoe.mirai.internal.message.ensureSequenceIdAvailable
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.createToFriend
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.createToStranger
import net.mamoe.mirai.internal.utils.estimateLength
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.castOrNull
import net.mamoe.mirai.utils.verbose
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline val Group.uin: Long get() = this.cast<GroupImpl>().uin
internal inline val Group.groupCode: Long get() = this.id
internal inline val User.uin: Long get() = this.id
internal inline val Bot.uin: Long get() = this.id

internal suspend fun <T : User> Friend.sendMessageImpl(
    message: Message,
    friendReceiptConstructor: (OnlineMessageSourceToFriendImpl) -> MessageReceipt<Friend>,
    tReceiptConstructor: (OnlineMessageSourceToFriendImpl) -> MessageReceipt<T>
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
    }.message.toMessageChain()
    chain.verityLength(message, this)

    chain.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()

    lateinit var source: OnlineMessageSourceToFriendImpl
    val result = bot.network.runCatching {
        MessageSvcPbSendMsg.createToFriend(
            bot.client,
            this@sendMessageImpl,
            chain
        ) {
            source = it
        }.forEach { packet ->
            packet.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
                check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                    "Send friend message failed: $it"
                }
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

internal suspend fun <T : User> Stranger.sendMessageImpl(
    message: Message,
    strangerReceiptConstructor: (OnlineMessageSourceToStrangerImpl) -> MessageReceipt<Stranger>,
    tReceiptConstructor: (OnlineMessageSourceToStrangerImpl) -> MessageReceipt<T>
): MessageReceipt<T> {
    contract { callsInPlace(strangerReceiptConstructor, InvocationKind.EXACTLY_ONCE) }
    val bot = bot.asQQAndroidBot()

    val chain = kotlin.runCatching {
        StrangerMessagePreSendEvent(this, message).broadcast()
    }.onSuccess {
        check(!it.isCancelled) {
            throw EventCancelledException("cancelled by StrangerMessagePreSendEvent")
        }
    }.getOrElse {
        throw EventCancelledException("exception thrown when broadcasting StrangerMessagePreSendEvent", it)
    }.message.toMessageChain()
    chain.verityLength(message, this)

    chain.firstIsInstanceOrNull<QuoteReply>()?.source?.ensureSequenceIdAvailable()

    lateinit var source: OnlineMessageSourceToStrangerImpl
    val result = bot.network.runCatching {
        MessageSvcPbSendMsg.createToStranger(
            bot.client,
            this@sendMessageImpl,
            chain,
        ) {
            source = it
        }.sendAndExpect<MessageSvcPbSendMsg.Response>().let {
            check(it is MessageSvcPbSendMsg.Response.SUCCESS) {
                "Send stranger message failed: $it"
            }
        }
        strangerReceiptConstructor(source)
    }

    result.fold(
        onSuccess = {
            StrangerMessagePostSendEvent(this, chain, null, it)
        },
        onFailure = {
            StrangerMessagePostSendEvent(this, chain, it, null)
        }
    ).broadcast()

    result.getOrThrow()
    return tReceiptConstructor(source)
}

internal fun Contact.logMessageSent(message: Message) {
    if (message !is LongMessageInternal) {
        bot.logger.verbose("$this <- $message".replaceMagicCodes())
    }
}

internal fun MessageChain.countImages(): Int = this.count { it is Image }

internal fun MessageChain.verityLength(
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
        return message.filterNot { it is MessageSource }.joinToString("").replaceMagicCodes()
    }

    fun renderGroupMessage(group: Group, senderName: String, sender: Member, message: MessageChain): String {
        val displayId = if (sender is AnonymousMember) "匿名" else sender.id.toString()
        return "[${group.name}(${group.id})] ${senderName}($displayId) -> ${renderMessage(message)}"
    }

    fun renderGroupTempMessage(group: Group, senderName: String, sender: Member, message: MessageChain): String {
        return "[${group.name}(${group.id})] $senderName(Temp ${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderStrangerMessage(senderName: String, sender: User, message: MessageChain): String {
        return "[$senderName(Stranger ${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderFriendMessage(sender: User, message: MessageChain): String {
        return "${sender.nick}(${sender.id}) -> ${renderMessage(message)}"
    }

    fun renderOtherClientMessage(client: OtherClient): String {
        return "${client.platform} -> ${renderMessage(message)}"
    }


    bot.logger.verbose {
        when (this) {
            is net.mamoe.mirai.event.events.GroupMessageEvent ->
                renderGroupMessage(group, senderName, sender, message)
            is net.mamoe.mirai.event.events.GroupMessageSyncEvent ->
                renderGroupMessage(group, senderName, sender, message)

            is net.mamoe.mirai.event.events.GroupTempMessageEvent ->
                renderGroupTempMessage(group, senderName, sender, message)
            is net.mamoe.mirai.event.events.GroupTempMessageSyncEvent ->
                renderGroupTempMessage(group, senderName, sender, message)

            is net.mamoe.mirai.event.events.StrangerMessageEvent,
            is net.mamoe.mirai.event.events.StrangerMessageSyncEvent ->
                renderStrangerMessage(senderName, sender, message)

            is net.mamoe.mirai.event.events.FriendMessageEvent,
            is net.mamoe.mirai.event.events.FriendMessageSyncEvent ->
                renderFriendMessage(sender, message)

            is net.mamoe.mirai.event.events.OtherClientMessageEvent ->
                renderOtherClientMessage(client)

            else -> toString()
        }
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
