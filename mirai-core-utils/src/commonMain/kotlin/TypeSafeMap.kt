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

public interface TypeSafeMap {
    public val size: Int

    public operator fun <T> get(key: TypeKey<T>): T
    public operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null

    public fun toMap(): Map<TypeKey<*>, Any?>

    public companion object {
        public val EMPTY: TypeSafeMap = TypeSafeMapImpl(emptyMap())
    }
}

public operator fun TypeSafeMap.plus(other: TypeSafeMap): TypeSafeMap {
    return when {
        other.size == 0 -> this
        this.size == 0 -> other
        else -> buildTypeSafeMap {
            setAll(this@plus)
            setAll(other)
        }
    }
}

public interface MutableTypeSafeMap : TypeSafeMap {
    public operator fun <T> set(key: TypeKey<T>, value: T)
    public fun <T> remove(key: TypeKey<T>): T?
    public fun setAll(other: TypeSafeMap)
}


internal open class TypeSafeMapImpl(
    internal open val map: Map<TypeKey<*>, Any?> = ConcurrentHashMap()
) : TypeSafeMap {
    override val size: Int get() = map.size

    override fun equals(other: Any?): Boolean {
        return other is TypeSafeMapImpl && other.map == this.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override operator fun <T> get(key: TypeKey<T>): T =
        map[key]?.uncheckedCast() ?: throw NoSuchElementException(key.toString())

    override operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null

    override fun toMap(): Map<TypeKey<*>, Any?> = map
}

@PublishedApi
internal class MutableTypeSafeMapImpl(
    override val map: MutableMap<TypeKey<*>, Any?> = ConcurrentHashMap()
) : TypeSafeMap, MutableTypeSafeMap, TypeSafeMapImpl(map) {
    override fun equals(other: Any?): Boolean {
        return other is MutableTypeSafeMapImpl && other.map == this.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override operator fun <T> set(key: TypeKey<T>, value: T) {
        map[key] = value
    }

    override fun setAll(other: TypeSafeMap) {
        if (other is TypeSafeMapImpl) {
            map.putAll(other.map)
        } else {
            map.putAll(other.toMap())
        }
    }

    override fun <T> remove(key: TypeKey<T>): T? = map.remove(key)?.uncheckedCast()
}

public inline fun buildTypeSafeMap(block: MutableTypeSafeMap.() -> Unit): MutableTypeSafeMap {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return MutableTypeSafeMapImpl().apply(block)
}
