package net.mamoe.mirai.api.http.queue

import net.mamoe.mirai.message.MessagePacket
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.collections.ArrayList

class MessageQueue : ConcurrentLinkedDeque<MessagePacket<*, *>>() {

    fun fetch(size: Int): List<MessagePacket<*, *>> {
        var count = size
        val ret = ArrayList<MessagePacket<*, *>>(count)
        while (!this.isEmpty() && count-- > 0) {
            ret.add(this.pop())
        }
        return ret
    }
}