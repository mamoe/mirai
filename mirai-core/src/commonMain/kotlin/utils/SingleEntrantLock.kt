/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized


internal class SingleEntrantLock : SynchronizedObject() {
    private val locker: AtomicRef<Any?> = atomic(null)

    inline fun <R> withLock(locker: Any, crossinline block: () -> R): R? {
        return synchronized(this) {
            if (this.locker.value === locker) return@synchronized null
            this.locker.value = locker
            block().also {
                this.locker.value = null
            }
        }
    }
}