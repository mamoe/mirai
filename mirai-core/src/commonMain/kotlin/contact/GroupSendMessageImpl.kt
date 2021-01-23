/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("RESULT_CLASS_IN_RETURN_TYPE") // inline ABI not stable but we don't care about internal ABI

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MessageTooLargeException
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent
import net.mamoe.mirai.event.nextEventOrNull
import net.mamoe.mirai.internal.MiraiImpl
import net.mamoe.mirai.internal.forwardMessage
import net.mamoe.mirai.internal.longMessage
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.chat.MusicSharePacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.SendMessageMultiProtocol
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.OnlinePushPbPushGroupMsg
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.currentTimeSeconds

/**
 * Might be recalled with [transformedMessage] `is` [LongMessageInternal] if length estimation failed ([sendMessagePacket])
 */
internal suspend fun GroupImpl.sendMessageImpl(
    originalMessage: Message,
    transformedMessage: Message,
    step: GroupMessageSendingStep,
): Result<MessageReceipt<Group>> { // Result<MessageReceipt<Group>>
    val chain = transformedMessage
        .transformSpecialMessages(this)
        .convertToLongMessageIfNeeded(step, this)

    chain.findIsInstance<QuoteReply>()?.source?.ensureSequenceIdAvailable()

    chain.asSequence().filterIsInstance<FriendImage>().forEach { image ->
        updateFriendImageForGroupMessage(image)
    }

    return kotlin.runCatching {
        sendMessagePacket(
            originalMessage,
            transformedMessage,
            chain,
            step
        )
    }
}


/**
 * Called only in 'public' apis.
 */
internal suspend fun GroupImpl.broadcastGroupMessagePreSendEvent(message: Message): MessageChain {
    return kotlin.runCatching {
        GroupMessagePreSendEvent(this, message).broadcast()
    }.onSuccess {
        check(!it.isCancelled) {
            throw EventCancelledException("cancelled by GroupMessagePreSendEvent")
        }
    }.getOrElse {
        throw EventCancelledException("exception thrown when broadcasting GroupMessagePreSendEvent", it)
    }.message.toMessageChain()
}


/**
 * - [ForwardMessage] -> [ForwardMessageInternal] (by uploading through highway)
 * - ... any others for future
 */
private suspend fun Message.transformSpecialMessages(contact: Contact): MessageChain {
    return takeSingleContent<ForwardMessage>()?.let { forward ->
        check(forward.nodeList.size <= 200) {
            throw MessageTooLargeException(
                contact, forward, forward,
                "ForwardMessage allows up to 200 nodes, but found ${forward.nodeList.size}"
            )
        }

        val resId = MiraiImpl.uploadGroupMessageHighway(contact.bot, contact.id, forward.nodeList, false)
        RichMessage.forwardMessage(
            resId = resId,
            timeSeconds = currentTimeSeconds(),
            forwardMessage = forward,
        )
    }?.toMessageChain() ?: toMessageChain()
}

internal enum class GroupMessageSendingStep {
    FIRST, LONG_MESSAGE, FRAGMENTED
}

/**
 * Final process
 */
private suspend fun GroupImpl.sendMessagePacket(
    originalMessage: Message,
    transformedMessage: Message,
    finalMessage: MessageChain,
    step: GroupMessageSendingStep,
): MessageReceipt<Group> {

    val group = this

    var source: OnlineMessageSourceToGroupImpl? = null

    bot.network.run {
        SendMessageMultiProtocol.createToGroup(
            bot.client, group, finalMessage,
            step == GroupMessageSendingStep.FRAGMENTED
        ) { source = it }.forEach { packet ->

            when (val resp = packet.sendAndExpect<Packet>()) {
                is MessageSvcPbSendMsg.Response -> {
                    if (resp is MessageSvcPbSendMsg.Response.MessageTooLarge) {
                        return when (step) {
                            GroupMessageSendingStep.FIRST -> {
                                sendMessageImpl(
                                    originalMessage,
                                    transformedMessage,
                                    GroupMessageSendingStep.LONG_MESSAGE
                                )
                            }
                            GroupMessageSendingStep.LONG_MESSAGE -> {
                                sendMessageImpl(
                                    originalMessage,
                                    transformedMessage,
                                    GroupMessageSendingStep.FRAGMENTED
                                )

                            }
                            else -> {
                                throw MessageTooLargeException(
                                    group,
                                    originalMessage,
                                    finalMessage,
                                    "Message '${finalMessage.content.take(10)}' is too large."
                                )
                            }
                        }.getOrThrow()
                    }
                    check(resp is MessageSvcPbSendMsg.Response.SUCCESS) {
                        "Send group message failed: $resp"
                    }
                }
                is MusicSharePacket.Response -> {
                    resp.pkg.checkSuccess("send group music share")

                    val receipt: OnlinePushPbPushGroupMsg.SendGroupMessageReceipt =
                        nextEventOrNull(3000) { it.fromAppId == 3116 }
                            ?: OnlinePushPbPushGroupMsg.SendGroupMessageReceipt.EMPTY

                    source = OnlineMessageSourceToGroupImpl(
                        group,
                        internalIds = intArrayOf(receipt.messageRandom),
                        providedSequenceIds = intArrayOf(receipt.sequenceId),
                        sender = bot,
                        target = group,
                        time = currentTimeSeconds().toInt(),
                        originalMessage = finalMessage
                    )
                }
            }
        }

        check(source != null) {
            "Internal error: source is not initialized"
        }

        try {
            source!!.ensureSequenceIdAvailable()
        } catch (e: Exception) {
            bot.network.logger.warning(
                "Timeout awaiting sequenceId for group message(${finalMessage.content.take(10)}). Some features may not work properly",
                e

            )
        }

        return MessageReceipt(source!!, group)
    }
}

private suspend fun GroupImpl.uploadGroupLongMessageHighway(
    chain: MessageChain
) = MiraiImpl.uploadGroupMessageHighway(
    bot, this.id,
    listOf(
        ForwardMessage.Node(
            senderId = bot.id,
            time = currentTimeSeconds().toInt(),
            messageChain = chain,
            senderName = bot.nick
        )
    ),
    true
)

private suspend fun MessageChain.convertToLongMessageIfNeeded(
    step: GroupMessageSendingStep,
    groupImpl: GroupImpl,
): MessageChain {
    return when (step) {
        GroupMessageSendingStep.FIRST -> {
            // 只需要在第一次发送的时候验证长度
            // 后续重试直接跳过
            verityLength(this, groupImpl)
            this
        }
        GroupMessageSendingStep.LONG_MESSAGE -> {
            val resId = groupImpl.uploadGroupLongMessageHighway(this)
            this + RichMessage.longMessage(
                brief = takeContent(27),
                resId = resId,
                timeSeconds = currentTimeSeconds()
            ) // LongMessageInternal replaces all contents and preserves metadata
        }
        GroupMessageSendingStep.FRAGMENTED -> this
    }
}

/**
 * Ensures server holds the cache
 */
private suspend fun GroupImpl.updateFriendImageForGroupMessage(image: FriendImage) {
    bot.network.run {
        ImgStore.GroupPicUp(
            bot.client,
            uin = bot.id,
            groupCode = id,
            md5 = image.md5,
            size = if (image is OnlineFriendImageImpl) image.delegate.fileLen else 0
        ).sendAndExpect<ImgStore.GroupPicUp.Response>()
    }
}
