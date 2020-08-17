/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.utils.currentTimeMillis
import kotlin.jvm.JvmField
import kotlin.jvm.Volatile


/**
 * Dynamically sized cache list with retention period.
 * No concurrency guaranteed on same elements.
 */
internal class AtomicResizeCacheList<E>(private val retention: Long) {
    private inner class Cache {
        @Volatile
        @JvmField
        var element: E? = null

        val time: AtomicLong = atomic(0L)
    }

    companion object {
        const val initialCapacity: Int = 32
    }

    private val list: MutableList<Cache> = ArrayList(initialCapacity)
    private val lock = reentrantLock()

    /**
     * Adds an element, also cleanup outdated caches, but no duplication is removed.
     * No concurrency guaranteed on same [element].
     */
    private fun add(element: E) {
        val currentTime = currentTimeMillis
        findAvailable@ while (true) {
            for (cache in list) {
                val instant = cache.time.value
                when {
                    instant == 0L -> {
                        if (cache.time.compareAndSet(instant, currentTime + retention)) {
                            cache.element = element
                            return
                        } else continue@findAvailable
                    }
                    // outdated
                    instant < currentTime -> cache.time.compareAndSet(instant, 0)
                }
            }
            // no more Cache instance available
            lock.withLock {
                list.add(Cache().apply {
                    this.element = element
                    this.time.value = currentTime + retention
                })
            }
            return
        }
    }

    /**
     * No concurrency guaranteed on same [element]
     */
    private fun removeDuplication(element: E): Boolean {
        val duplicate = list.firstOrNull { it.time.value != 0L && it.element == element } ?: return false
        duplicate.time.value = 0
        return true
    }

    fun ensureNoDuplication(element: E): Boolean {
        return if (removeDuplication(element)) {
            false
        } else {
            add(element)
            true
        }
    }
}