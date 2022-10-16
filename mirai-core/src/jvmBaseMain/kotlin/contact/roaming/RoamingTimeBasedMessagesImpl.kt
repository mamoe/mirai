/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.roaming

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.roaming.RoamingMessageFilter
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.stream
import java.util.stream.Stream

internal actual sealed class RoamingTimeBasedMessagesImpl : CommonRoamingMessagesImpl() {
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
}