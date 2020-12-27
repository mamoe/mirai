/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import net.mamoe.mirai.event.events.FriendEvent
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import org.junit.jupiter.api.Test

internal class EventChannelTest {
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testVariance() {
        var global: EventChannel<Event> = GlobalEventChannel
        var a: EventChannel<MessageEvent> = global.filterIsInstance<MessageEvent>()
        a.filter {
            // it: Event
            it.isIntercepted
        }
        val messageEventChannel = a.filterIsInstance<MessageEvent>()
        // group.asChannel<GroupMessageEvent>()

        val listener: Listener<GroupMessageEvent> = messageEventChannel.subscribeAlways<GroupEvent>() {

        }

        global = a

        global.subscribeMessages {

        }

        messageEventChannel.subscribeMessages {

        }

        global.subscribeAlways<FriendEvent> {

        }

        // inappliable: out cannot passed as in
        // val b: EventChannel<in FriendMessageEvent> = global.filterIsInstance<FriendMessageEvent>()
    }
}