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

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
@JvmInline
public value class TypeKey<T>(public val name: String) {
    override fun toString(): String = "Key($name)"

    public inline infix fun to(value: T): TypeSafeMap =
        buildTypeSafeMap { set(this@TypeKey, value) }
}

/**
 * @see buildTypeSafeMap
 */
public sealed interface TypeSafeMap {
    public val size: Int

    public operator fun <T> get(key: TypeKey<T>): T = getOrNull(key) ?: throw NoSuchElementException(key.toString())
    public fun <T> getOrNull(key: TypeKey<T>): T?
    public operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null

    public fun toMapBoxed(): Map<TypeKey<*>, Any?>
    public fun toMap(): Map<String, Any?>

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

public sealed interface MutableTypeSafeMap : TypeSafeMap {
    public operator fun <T> set(key: TypeKey<T>, value: T)
    public fun <T> remove(key: TypeKey<T>): T?
    public fun setAll(other: TypeSafeMap)
}


@PublishedApi
internal open class TypeSafeMapImpl(
    @PublishedApi internal open val map: Map<String, Any?> = ConcurrentHashMap()
) : TypeSafeMap {
    override val size: Int get() = map.size

    override fun equals(other: Any?): Boolean {
        return other is TypeSafeMapImpl && other.map == this.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        return "TypeSafeMapImpl(map=$map)"
    }

    override fun <T> getOrNull(key: TypeKey<T>): T? = map[key.name]?.uncheckedCast()

    override operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null

    override fun toMapBoxed(): Map<TypeKey<*>, Any?> = map.mapKeys { TypeKey<Any?>(it.key) }
    override fun toMap(): Map<String, Any?> = map
}

@PublishedApi
internal class MutableTypeSafeMapImpl(
    @PublishedApi override val map: MutableMap<String, Any?> = ConcurrentHashMap()
) : TypeSafeMap, MutableTypeSafeMap, TypeSafeMapImpl(map) {
    override fun equals(other: Any?): Boolean {
        return other is MutableTypeSafeMapImpl && other.map == this.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }

    override fun toString(): String {
        return "MutableTypeSafeMapImpl(map=$map)"
    }

    override operator fun <T> set(key: TypeKey<T>, value: T) {
        map[key.name] = value
    }

    override fun setAll(other: TypeSafeMap) {
        if (other is TypeSafeMapImpl) {
            map.putAll(other.map)
        } else {
            map.putAll(other.toMap())
        }
    }

    override fun <T> remove(key: TypeKey<T>): T? = map.remove(key.name)?.uncheckedCast()
}

public inline fun MutableTypeSafeMap(): MutableTypeSafeMap = MutableTypeSafeMapImpl()
public inline fun MutableTypeSafeMap(map: Map<String, Any?>): MutableTypeSafeMap =
    MutableTypeSafeMapImpl().also { it.map.putAll(map) }

public inline fun TypeSafeMap(): TypeSafeMap = TypeSafeMap.EMPTY
public inline fun TypeSafeMap(map: Map<String, Any?>): TypeSafeMap =
    MutableTypeSafeMapImpl().also { it.map.putAll(map) }

public inline fun buildTypeSafeMap(block: MutableTypeSafeMap.() -> Unit): MutableTypeSafeMap {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return MutableTypeSafeMapImpl().apply(block)
}
