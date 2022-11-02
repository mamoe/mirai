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

/**
 * Wraps a [atomic] to allow more complicated usages.
 */
@TestOnly
public class AtomicInteger(
    value: Int
) {
    private val delegate = atomic(value)

    public var value: Int
        get() = delegate.value
        set(value) {
            delegate.value = value
        }

    public fun compareAndSet(expect: Int, update: Int): Boolean = delegate.compareAndSet(expect, update)
    public fun getAndIncrement(): Int = delegate.getAndIncrement()
    public fun incrementAndGet(): Int = delegate.incrementAndGet()
}

/**
 * Wraps a [atomic] to allow more complicated usages.
 */
@TestOnly
public class AtomicBoolean(
    value: Boolean
) {
    private val delegate = atomic(value)

    public var value: Boolean
        get() = delegate.value
        set(value) {
            delegate.value = value
        }

    public fun compareAndSet(expect: Boolean, update: Boolean): Boolean = delegate.compareAndSet(expect, update)
}