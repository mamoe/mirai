/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

@Suppress("unused", "UNCHECKED_CAST")
public class SizedCache<T>(size: Int) : Iterable<T> {
    public val lock: ReentrantLock = reentrantLock()
    public val data: Array<Any?> = arrayOfNulls(size)

    public var filled: Boolean = false
    public var idx: Int = 0

    public fun emit(v: T) {
        lock.withLock {
            data[idx] = v
            idx++
            if (idx == data.size) {
                filled = true
                idx = 0
            }
        }
    }

    override fun iterator(): Iterator<T> {
        if (filled) {
            return object : Iterator<T> {
                private var idx0: Int = idx
                private var floopend = false
                override fun hasNext(): Boolean = !floopend || idx0 != idx
                override fun next(): T {
                    val rsp = data[idx0] as T
                    idx0++
                    if (idx0 == data.size) {
                        idx0 = 0
                        floopend = true
                    }
                    return rsp
                }
            }
        }

        return object : Iterator<T> {
            private var idx0: Int = 0
            override fun hasNext(): Boolean = idx0 < idx
            override fun next(): T = data[idx0].also { idx0++ } as T
        }
    }
}