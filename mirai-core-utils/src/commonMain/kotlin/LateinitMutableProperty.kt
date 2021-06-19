/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val UNINITIALIZED: Any? = Symbol("UNINITIALIZED")

/**
 * - [initializer] is supported to be called at most once, however multiple invocations may happen if executed by multiple coroutines in single thread.
 * - [ReadWriteProperty.setValue] prevails on competition with [initializer].
 */
public fun <T> lateinitMutableProperty(initializer: () -> T): ReadWriteProperty<Any?, T> =
    LateinitMutableProperty(initializer)

private class LateinitMutableProperty<T>(
    initializer: () -> T
) : ReadWriteProperty<Any?, T> {
    private val value = atomic(UNINITIALIZED)

    private var initializer: (() -> T)? = initializer

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (val v = this.value.value) {
            UNINITIALIZED -> synchronized(this) {
                val initializer = initializer
                if (initializer != null && this.value.value === UNINITIALIZED) {
                    val value = initializer()
                    this.value.compareAndSet(UNINITIALIZED, value) // setValue prevails
                    this.initializer = null
                    value
                } else this.value.value as T
            }
            else -> v as T
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value.value = value
    }
}