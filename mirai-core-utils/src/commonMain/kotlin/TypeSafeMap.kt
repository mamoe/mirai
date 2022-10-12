/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import kotlinx.serialization.Serializable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 携带值类型信息的键.
 * @see TypeSafeMap
 */
@Serializable
@JvmInline
public value class TypeKey<T>(public val name: String) {
    override fun toString(): String = "Key($name)"

    public inline infix fun to(value: T): TypeSafeMap = buildTypeSafeMap { set(this@TypeKey, value) }
}

/**
 * 类型安全的表. [TypeSafeMap] 使用带有值类型信息的 [TypeKey] 作为键, 可放入不同类型的值, 并且在取出时能获得期望的安全类型.
 *
 * **注意: 类型仅在编译期检查. 进行未受检类型转换将可能导致运行时问题.**
 *
 * 与 [Map] 类似, [TypeSafeMap] 是只读的实现.
 * 使用 [buildTypeSafeMap] 以构建一个表.
 *
 * @see buildTypeSafeMap
 * @see MutableTypeSafeMap
 */
public sealed interface TypeSafeMap {
    public val size: Int

    /**
     * @throws NoSuchElementException
     */
    public operator fun <T> get(key: TypeKey<T>): T
    public operator fun <T : S, S> get(key: TypeKey<T>, defaultValue: S): S
    public operator fun <T> contains(key: TypeKey<T>): Boolean = get(key) != null

    /**
     * 创建键为包装类型 [TypeKey] 的 [Map]. 此方法将会包装全部键因而性能低下, 尽可能使用 [toMap] 替代.
     */
    public fun toMapBoxed(): Map<TypeKey<*>, Any>

    /**
     * 得到实际数据表.
     */
    public fun toMap(): Map<String, Any>

    public operator fun <T> provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, T> {
        val typeKey = TypeKey<T>(property.name)
        return ReadOnlyProperty { _, _ -> get(typeKey) }
    }

    public companion object {
        public val EMPTY: TypeSafeMap = TypeSafeMapImpl(emptyMap())
    }
}

public fun <T> TypeSafeMap.property(name: String): ReadOnlyProperty<Any?, T> {
    return property(TypeKey(name))
}

public fun <T> TypeSafeMap.property(typeKey: TypeKey<T>): ReadOnlyProperty<Any?, T> {
    return ReadOnlyProperty { _, _ -> get(typeKey) }
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


    public override operator fun <T> provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): ReadWriteProperty<Any?, T> {
        val typeKey = TypeKey<T>(property.name)
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return get(typeKey)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                set(typeKey, value)
            }
        }
    }

}

private val NULL: Any = Symbol("NULL")!!

@PublishedApi
internal open class TypeSafeMapImpl(
    @PublishedApi internal open val map: Map<String, Any> = ConcurrentHashMap()
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

    override operator fun <T> get(key: TypeKey<T>): T {
        val value = map[key.name]
        if (value === NULL) {
            return null.uncheckedCast()
        }
        return value?.uncheckedCast() ?: throw NoSuchElementException(key.toString())
    }

    override operator fun <T : S, S> get(key: TypeKey<T>, defaultValue: S): S {
        val value = map[key.name]
        if (value === NULL) return defaultValue
        return value?.uncheckedCast() ?: defaultValue
    }

    override operator fun <T> contains(key: TypeKey<T>): Boolean = map.containsKey(key.name)

    override fun toMapBoxed(): Map<TypeKey<*>, Any> = map.mapKeys { TypeKey<Any?>(it.key) }
    override fun toMap(): Map<String, Any> = map
}

@PublishedApi
internal class MutableTypeSafeMapImpl(
    @PublishedApi override val map: MutableMap<String, Any> = ConcurrentHashMap()
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
        if (value == null) {
            map[key.name] = NULL
        } else {
            map[key.name] = value
        }
    }

    override fun setAll(other: TypeSafeMap) {
        if (other is TypeSafeMapImpl) {
            map.putAll(other.map)
        } else {
            map.putAll(other.toMap())
        }
    }

    override fun <T> remove(key: TypeKey<T>): T? {
        val value = map.remove(key.name)
        return if (value == NULL) {
            null
        } else {
            value?.uncheckedCast()
        }
    }
}

public fun TypeSafeMap.toMutableTypeSafeMap(): MutableTypeSafeMap = createMutableTypeSafeMap(this.toMap())

public inline fun createMutableTypeSafeMap(): MutableTypeSafeMap = MutableTypeSafeMapImpl()
public inline fun createMutableTypeSafeMap(map: Map<String, Any>): MutableTypeSafeMap =
    MutableTypeSafeMapImpl().also { it.map.putAll(map) }

public inline fun createTypeSafeMap(): TypeSafeMap = TypeSafeMap.EMPTY

public inline fun buildTypeSafeMap(block: MutableTypeSafeMap.() -> Unit): MutableTypeSafeMap {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return MutableTypeSafeMapImpl().apply(block)
}
