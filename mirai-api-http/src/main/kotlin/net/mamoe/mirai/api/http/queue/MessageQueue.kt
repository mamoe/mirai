/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.queue

import net.mamoe.mirai.api.http.data.common.EventDTO
import net.mamoe.mirai.api.http.data.common.IgnoreEventDTO
import net.mamoe.mirai.api.http.data.common.toDTO
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.MessagePacket
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.firstKey
import java.util.concurrent.ConcurrentLinkedDeque

class MessageQueue : ConcurrentLinkedDeque<BotEvent>() {

    val cacheSize = 4096
    val cache = LinkedHashMap<Long, MessagePacket<*, *>>()

    suspend fun fetch(size: Int): List<EventDTO> {
        var count = size

        val ret = ArrayList<EventDTO>(count)
        while (!this.isEmpty() && count > 0) {
            val event = pop()

            event.toDTO().also {
                if (it != IgnoreEventDTO) {
                    ret.add(it)
                    count--
                }
            }

            if (event is MessagePacket<*, *>) {
                addQuoteCache(event)
            }
        }
        return ret
    }

    fun cache(messageId: Long) =
        cache[messageId] ?: throw NoSuchElementException()

    fun addQuoteCache(msg: MessagePacket<*, *>) {
        cache[msg.message[MessageSource].id] = msg
        if (cache.size > cacheSize) {
            cache.remove(cache.firstKey())
        }
    }
}