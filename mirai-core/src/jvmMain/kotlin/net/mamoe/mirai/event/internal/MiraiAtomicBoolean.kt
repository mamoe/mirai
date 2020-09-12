/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.internal

import java.util.concurrent.atomic.AtomicBoolean

internal actual class MiraiAtomicBoolean actual constructor(initial: Boolean) {
    private val delegate: AtomicBoolean = AtomicBoolean(initial)

    actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        return delegate.compareAndSet(expect, update)
    }

    actual var value: Boolean
        get() = delegate.get()
        set(value) {
            delegate.set(value)
        }
}
