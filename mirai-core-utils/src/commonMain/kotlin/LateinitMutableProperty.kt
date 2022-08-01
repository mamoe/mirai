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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val UNINITIALIZED: Any? = Symbol("UNINITIALIZED")

/**
 * Creates a lazily initialized, atomic, mutable property.
 *
 * [initializer] will be called at most once for most of the time, but not always.
 * Multiple invocations may happen if property getter is called by multiple coroutines in single thread (implementation use reentrant lock).
 * Hence, you must not trust [initializer] to be called only once.
 *
 * If property setter is executed before any execution of getter, [initializer] will not be called.
 * While [initializer] is running, i.e. still calculating the value to set to the property,
 * calling property setter will *outdo* the initializer. That is, the setter always prevails on competition with [initializer].
 */
public fun <T> lateinitMutableProperty(initializer: () -> T): ReadWriteProperty<Any?, T> =
    LateinitMutableProperty(initializer)

private class LateinitMutableProperty<T>(
    initializer: () -> T
) : ReadWriteProperty<Any?, T>, SynchronizedObject() {
    private val value = atomic(UNINITIALIZED)

    private var initializer: (() -> T)? = initializer

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (val v = this.value.value) {
            UNINITIALIZED -> synchronized(this) {
                val initializer = initializer
                if (initializer != null && this.value.value === UNINITIALIZED) {
                    val value = initializer()
                    this.initializer = null // not used anymore, help gc
                    this.value.compareAndSet(UNINITIALIZED, value) // setValue prevails
                    this.value.value.let {
                        check(it !== UNINITIALIZED)
                        return it as T
                    }
                } else this.value.value as T
            }

            else -> v as T
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value.value = value
    }
}