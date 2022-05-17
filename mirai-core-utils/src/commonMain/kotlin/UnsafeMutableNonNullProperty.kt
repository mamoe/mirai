/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.jvm.JvmField

public fun <T : Any> unsafeMutableNonNullPropertyOf(
    name: String = "<unknown>"
): UnsafeMutableNonNullProperty<T> {
    return UnsafeMutableNonNullProperty(name)
}

@Suppress("NOTHING_TO_INLINE")
public class UnsafeMutableNonNullProperty<T : Any>(
    private val propertyName: String = "<unknown>"
) {
    @JvmField
    public var value0: T? = null

    public val isInitialized: Boolean get() = value0 !== null
    public var value: T
        get() = value0 ?: throw IllegalStateException("Property `$propertyName` not initialized")
        set(value) {
            value0 = value
        }

    public fun clear() {
        value0 = null
    }

    public inline operator fun getValue(thiz: Any?, property: Any?): T = value
    public inline operator fun setValue(thiz: Any?, property: Any?, value: T) {
        value0 = value
    }

    override fun toString(): String {
        return value0?.toString() ?: "<uninitialized>"
    }
}
