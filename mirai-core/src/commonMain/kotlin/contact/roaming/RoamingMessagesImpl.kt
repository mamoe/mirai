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
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.roaming.RoamingMessage
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.contact.CommonGroupImpl
import net.mamoe.mirai.internal.contact.FriendImpl
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetGroupMsg
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetRoamMsgReq
import net.mamoe.mirai.internal.utils.indexFirstBE
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.check
import net.mamoe.mirai.utils.mapToIntArray
import net.mamoe.mirai.utils.toLongUnsigned

internal abstract class CommonRoamingMessagesImpl : RoamingMessages {
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

    protected fun createRoamingMessage(
        message: MsgComm.Msg,
        messages: List<MsgComm.Msg>
    ) = object : RoamingMessage {
        override val contact: Contact get() = this@CommonRoamingMessagesImpl.contact
        override val sender: Long get() = message.msgHead.fromUin
        override val target: Long
            get() = message.msgHead.groupInfo?.groupCode ?: message.msgHead.toUin
        override val time: Long get() = message.msgHead.msgTime.toLongUnsigned()
        override val ids: IntArray by lazy { messages.mapToIntArray { it.msgHead.msgSeq } }
        override val internalIds: IntArray by lazy {
            messages.mapToIntArray { it.msgBody.richText.attr?.random ?: 0 } // other client 消息的这个是0
        }
    }

    abstract suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long
    ): MessageSvcPbGetRoamMsgReq.Response
}


internal expect sealed class RoamingMessagesImpl() : CommonRoamingMessagesImpl

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

internal class RoamingMessagesImplGroup(
    override val contact: CommonGroupImpl
) : RoamingMessagesImpl() {
    override suspend fun requestRoamMsg(
        timeStart: Long,
        lastMessageTime: Long,
        random: Long // unused field
    ): MessageSvcPbGetRoamMsgReq.Response {
        val lastMsgSeq = contact.bot.network.sendAndExpect(
            TroopManagement.GetGroupLastMsgSeq(
                client = contact.bot.client,
                groupUin = contact.uin
            )
        )
        return when(lastMsgSeq) {
            is TroopManagement.GetGroupLastMsgSeq.Response.Success -> {
                val results = mutableListOf<MsgComm.Msg>()
                var currentSeq = lastMsgSeq.seq

                while (true) {
                    if (currentSeq <= 0) break

                    val resp = contact.bot.network.sendAndExpect(
                        MessageSvcPbGetGroupMsg(
                            client = contact.bot.client,
                            groupUin = contact.uin,
                            messageSequence = currentSeq,
                            20 // maximum 20
                        )
                    )
                    if (resp is MessageSvcPbGetGroupMsg.Failed) break
                    if ((resp as MessageSvcPbGetGroupMsg.Success).msgElem.isEmpty()) break

                    // the message may be sorted increasing by message time,
                    // if so, additional sortBy will not take cost.
                    val msgElems = resp.msgElem.sortedBy { it.msgHead.msgTime }
                    results.addAll(0, msgElems)

                    val firstMsgElem = msgElems.first()
                    if (firstMsgElem.msgHead.msgTime < timeStart) {
                        break
                    } else {
                        currentSeq = (firstMsgElem.msgHead.msgSeq - 1).toLong()
                    }
                }

                // use binary search to find the first message that message time is lager than lastMessageTime
                var right = results.indexFirstBE(lastMessageTime) { it.msgHead.msgTime.toLong() }
                // check messages with same time
                if (results[right].msgHead.msgTime.toLong() == lastMessageTime) {
                    do { right ++ } while (right <= results.size - 1 && results[right].msgHead.msgTime <= lastMessageTime)
                }
                // loops at most 20 times, just traverse
                val left = results.indexOfFirst { it.msgHead.msgTime >= timeStart }

                MessageSvcPbGetRoamMsgReq.Response(
                    if (left == right) null else results.subList(left, right),
                    if (left == right) -1L else results[right - 1].msgHead.msgTime.toLong(), -1L, byteArrayOf()
                )
            }
            is TroopManagement.GetGroupLastMsgSeq.Response.Failed -> {
                MessageSvcPbGetRoamMsgReq.Response(null, -1L, -1L, byteArrayOf())
            }
        }
    }
}