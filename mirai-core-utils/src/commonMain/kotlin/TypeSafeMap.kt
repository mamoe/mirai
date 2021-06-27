/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
public value class TypeKey<out T>(public val name: String) {
    override fun toString(): String = "Key($name)"

    public inline infix fun <T> to(value: T): TypeSafeMap = buildTypeSafeMap { set(this@TypeKey, value) }
}

@JvmInline
public value class TypeSafeMap(
    private val map: MutableMap<TypeKey<*>, Any?> = ConcurrentHashMap()
) {
    public operator fun <T> get(key: TypeKey<T>): T =
        map[key]?.uncheckedCast() ?: throw NoSuchElementException(key.toString())

    public operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null
    public operator fun <T> set(key: TypeKey<T>, value: T) {
        map[key] = value
    }

    public fun <T> remove(key: TypeKey<T>): T? = map.remove(key)?.uncheckedCast()
}

public inline fun buildTypeSafeMap(block: TypeSafeMap.() -> Unit): TypeSafeMap {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return TypeSafeMap().apply(block)
}
