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
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.message.SimpleRefineContext
import net.mamoe.mirai.internal.message.data.OnlineShortVideoMsgInternal
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbGetRoamMsgReq
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.cast

internal sealed class TimeBasedRoamingMessagesImpl : AbstractRoamingMessages() {
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
                    messages.forEach {
                        emit(
                            it.toMessageChainOnline(
                                contact.bot,
                                refineContext = SimpleRefineContext(
                                    mutableListOf(
                                        OnlineShortVideoMsgInternal.FromId to it.msgHead.fromUin
                                    ).cast()
                                )
                            )
                        )
                    }
                } else {
                    for (message in messages) {
                        if (filter.invoke(createRoamingMessage(message, messages))) {
                            emit(
                                message.toMessageChainOnline(
                                    contact.bot,
                                    refineContext = SimpleRefineContext(
                                        mutableListOf(
                                            OnlineShortVideoMsgInternal.FromId to message.msgHead.fromUin
                                        ).cast()
                                    )
                                )
                            )
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
