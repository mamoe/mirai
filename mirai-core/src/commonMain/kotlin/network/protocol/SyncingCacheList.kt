/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai.internal.network.protocol

import java.util.*

internal class SyncingCacheList<E>(private val size: Int = 50) {
    private val packetIdList = LinkedList<E>()

    @Synchronized // faster than suspending Mutex
    fun addCache(element: E): Boolean {
        if (packetIdList.contains(element)) return false // duplicate
        packetIdList.addLast(element)
        if (packetIdList.size >= size) packetIdList.removeFirst()
        return true
    }

    @Synchronized
    fun removeFirst(condition: (E) -> Boolean): Boolean {
        val itr = packetIdList.listIterator()
        for (element in itr) {
            if (element.let(condition)) {
                itr.remove()
                return true
            }
        }
        return false
    }

    @Synchronized
    fun contains(condition: (E) -> Boolean): Boolean = packetIdList.any(condition)
}