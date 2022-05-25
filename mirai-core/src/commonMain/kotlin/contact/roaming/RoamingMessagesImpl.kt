/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.roaming.RoamingMessage
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.FriendImpl
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetRoamMsgReq
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.*
import java.util.stream.Stream

internal sealed class RoamingMessagesImpl : RoamingMessages {
    abstract val contact: AbstractContact

    override suspend fun getMessagesIn(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?
    ): Flow<MessageChain> {
        return flow {
            var lastMessageTime = timeEnd.coerceAtLeast(timeStart).coerceAtLeast(1)
            var random = 0L
            while (currentCoroutineContext().isActive) {
                val resp = requestRoamMsg(timeStart, lastMessageTime, random)
                val messages = resp.messages ?: break
                if (filter == null || filter === RoamingMessageFilter.ANY) {
                    // fast path
                    messages.forEach { emit(it.toMessageChainOnline(contact.bot)) }
                } else {
                    for (message in messages) {
                        if (filter.invoke(createRoamingMessage(message, messages))) {
                            emit(message.toMessageChainOnline(contact.bot))
                        }
                    }
                }

                lastMessageTime = resp.lastMessageTime
                random = resp.random
            }
        }
    }

    private fun createRoamingMessage(
        message: MsgComm.Msg,
        messages: List<MsgComm.Msg>
    ) = object : RoamingMessage {
        override val contact: Contact get() = this@RoamingMessagesImpl.contact
        override val sender: Long get() = message.msgHead.fromUin
        override val target: Long
            get() = message.msgHead.groupInfo?.groupCode ?: message.msgHead.toUin
        override val time: Long get() = message.msgHead.msgTime.toLongUnsigned()
        override val ids: IntArray by lazy { messages.mapToIntArray { it.msgHead.msgSeq } }
        override val internalIds: IntArray by lazy {
            messages.mapToIntArray { it.msgBody.richText.attr?.random ?: 0 } // other client 消息的这个是0
        }
    }


    @JavaFriendlyAPI
    override suspend fun getMessagesStream(
        timeStart: Long,
        timeEnd: Long,
        filter: RoamingMessageFilter?,
    ): Stream<MessageChain> {
        return stream {
            var lastMessageTime = timeEnd
            var random = 0L
            while (true) {
                val resp = runBlocking {
                    requestRoamMsg(timeStart, lastMessageTime, random)
                }

                val messages = resp.messages ?: break
                if (filter == null || filter === RoamingMessageFilter.ANY) {
                    messages.forEach { yield(runBlocking { it.toMessageChainOnline(contact.bot) }) }
                } else {
                    for (message in messages) {
                        if (filter.invoke(createRoamingMessage(message, messages))) {
                            yield(runBlocking { message.toMessageChainOnline(contact.bot) })
                        }
                    }
                }

                lastMessageTime = resp.lastMessageTime
                random = resp.random
            }
        }
    }

    abstract suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long
    ): MessageSvcPbGetRoamMsgReq.Response
}

internal class RoamingMessagesImplFriend(
    override val contact: FriendImpl
) : RoamingMessagesImpl() {
    override suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long
    ): MessageSvcPbGetRoamMsgReq.Response {
        return contact.bot.network.sendAndExpect(
            MessageSvcPbGetRoamMsgReq.createForFriend(
                client = contact.bot.client,
                uin = contact.id,
                timeStart = timeStart,
                lastMsgTime = lastMessageTime,
                random = random,
                maxCount = 1000,
                sig = byteArrayOf(),
                pwd = byteArrayOf()
            )
        ).value.check()
    }
}