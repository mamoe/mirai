/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.utils.LockFreeLinkedList
import java.util.*


internal actual object GlobalEventListeners {
    private val ALL_LEVEL_REGISTRIES: Map<Listener.EventPriority, LockFreeLinkedList<ListenerRegistry>>

    init {
        val map = EnumMap<Listener.EventPriority, LockFreeLinkedList<ListenerRegistry>>(Listener.EventPriority::class.java)
        Listener.EventPriority.values().forEach {
            map[it] = LockFreeLinkedList()
        }
        this.ALL_LEVEL_REGISTRIES = map
    }

    actual operator fun get(priority: Listener.EventPriority): LockFreeLinkedList<ListenerRegistry> = ALL_LEVEL_REGISTRIES[priority]!!

}
