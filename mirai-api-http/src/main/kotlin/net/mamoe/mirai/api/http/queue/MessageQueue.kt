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
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.firstKey
import java.util.concurrent.ConcurrentLinkedDeque

class MessageQueue : ConcurrentLinkedDeque<BotEvent>() {

    val quoteCacheSize = 4096
    val quoteCache = LinkedHashMap<Long, GroupMessage>()

    fun fetch(size: Int): List<EventDTO> {
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

            // TODO: 等FriendMessage支持quote
            if (event is GroupMessage) {
                addQuoteCache(event)
            }
        }
        return ret
    }

    private fun addQuoteCache(msg: GroupMessage) {
        quoteCache[msg.message[MessageSource].messageUid.toLong()] = msg
        if (quoteCache.size > quoteCacheSize) {
            quoteCache.remove(quoteCache.firstKey())
        }
    }
}