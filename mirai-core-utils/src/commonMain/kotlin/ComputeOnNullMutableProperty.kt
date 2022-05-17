/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.reflect.KProperty

public fun <T : Any> computeOnNullMutableProperty(initializer: () -> T): ComputeOnNullMutableProperty<T> =
    ComputeOnNullMutablePropertyImpl(initializer)

public interface ComputeOnNullMutableProperty<V : Any> {
    public fun get(): V
    public fun set(value: V?)

    public operator fun getValue(thisRef: Any?, property: KProperty<*>): V = get()
    public operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V?): Unit = set(value)
}


private class ComputeOnNullMutablePropertyImpl<T : Any>(
    private val initializer: () -> T
) : ComputeOnNullMutableProperty<T> {
    private val value = atomic<T?>(null)
    private val lock = SynchronizedObject()

    override tailrec fun get(): T {
        return when (val v = this.value.value) {
            null -> synchronized(lock) {
                if (this.value.value === null) {
                    val value = this.initializer()
                    // compiler inserts
                    this.value.compareAndSet(null, value) // setValue prevails
                    return get()
                } else this.value.value as T
            }
            else -> v
        }
    }

    override fun set(value: T?) {
        this.value.value = value
    }
}