/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.jvm.Volatile


internal class SingleEntrantLock {
    @Volatile
    @PublishedApi
    internal var locker: Any? = null

    private val lock = SynchronizedObject()

    inline fun <R> withLock(locker: Any, crossinline block: () -> R): R? {
        return synchronized(lock) {
            if (this.locker === locker) return null
            this.locker = locker
            block().also {
                this.locker = null
            }
        }
    }
}