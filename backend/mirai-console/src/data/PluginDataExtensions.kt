/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.internal.data.ShadowMap
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.internal.LowPriorityInOverloadResolution

/**
 * [PluginData] 相关一些扩展
 */
public object PluginDataExtensions {

    @ConsoleExperimentalApi
    public open class NotNullMap<K, V> internal constructor(
        private val delegate: Map<K, V>
    ) : Map<K, V> by delegate {
        override fun get(key: K): V =
            delegate[key] ?: error("Internal error: delegate[key] returned null for NotNullMap.get")

        @Deprecated(
            "getOrDefault on NotNullMap always returns the value in the map, and defaultValue will never be returned.",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("this.get(key)")
        )
        override fun getOrDefault(key: K, defaultValue: V): V {
            return super.getOrDefault(key, defaultValue)
        }
    }

    @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE") // as designed
    public class NotNullMutableMap<K, V> internal constructor(
        private val delegate: MutableMap<K, V>
    ) : MutableMap<K, V> by delegate, NotNullMap<K, V>(delegate) {
        override fun get(key: K): V =
            delegate[key] ?: error("Internal error: delegate[key] returned null for NotNullMutableMap.get")

        @Deprecated(
            "getOrDefault on NotNullMutableMap always returns the value in the map, and defaultValue will never be returned.",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("this.get(key)")
        )
        override fun getOrDefault(key: K, defaultValue: V): V {
            return super<MutableMap>.getOrDefault(key, defaultValue)
        }

        @Deprecated(
            "putIfAbsent on NotNullMutableMap always does nothing.",
            level = DeprecationLevel.WARNING,
            replaceWith = ReplaceWith("")
        )
        override fun putIfAbsent(key: K, value: V): Nothing? = null
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [LinkedHashMap], 再从 [this] 中取出链接自动保存的 [LinkedHashMap]. ([MutableMap.getOrPut] 的替代)
     *
     * @see withDefault
     */
    @JvmName("withEmptyDefaultMapImmutable")
    @JvmStatic
    public fun <K, InnerE, InnerV> SerializerAwareValue<MutableMap<K, Map<InnerE, InnerV>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, Map<InnerE, InnerV>>> {
        return this.withDefault { LinkedHashMap() }
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [LinkedHashMap], 再从 [this] 中取出链接自动保存的 [LinkedHashMap]. ([MutableMap.getOrPut] 的替代)
     * @see withDefault
     */
    @JvmName("withEmptyDefaultMap")
    @JvmStatic
    public fun <K, InnerE, InnerV> SerializerAwareValue<MutableMap<K, MutableMap<InnerE, InnerV>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, MutableMap<InnerE, InnerV>>> {
        return this.withDefault { LinkedHashMap() }
    }


    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [ArrayList], 再从 [this] 中取出链接自动保存的 [ArrayList].
     * @see withDefault
     */
    @JvmName("withEmptyDefaultListImmutable")
    @JvmStatic
    public fun <K, E> SerializerAwareValue<MutableMap<K, List<E>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, List<E>>> {
        return this.withDefault { ArrayList() }
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [ArrayList], 再从 [this] 中取出链接自动保存的 [ArrayList].
     * @see withDefault
     */
    @JvmName("withEmptyDefaultList")
    @JvmStatic
    public fun <K, E> SerializerAwareValue<MutableMap<K, MutableList<E>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, MutableList<E>>> {
        return this.withDefault { ArrayList() }
    }


    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [LinkedHashSet], 再从 [this] 中取出链接自动保存的 [LinkedHashSet].
     * @see withDefault
     */
    @JvmName("withEmptyDefaultSetImmutable")
    @JvmStatic
    public fun <K, E> SerializerAwareValue<MutableMap<K, Set<E>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, Set<E>>> {
        return this.withDefault { LinkedHashSet() }
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先放入一个 [LinkedHashSet], 再从 [this] 中取出链接自动保存的 [LinkedHashSet].
     * @see withDefault
     */
    @JvmName("withEmptyDefaultSet")
    @JvmStatic
    public fun <K, E> SerializerAwareValue<MutableMap<K, MutableSet<E>>>.withEmptyDefault(): SerializerAwareValue<NotNullMutableMap<K, MutableSet<E>>> {
        return this.withDefault { LinkedHashSet() }
    }


    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先调用 [defaultValueComputer] 并放入 [Map], 再返回调用的返回值
     */
    @JvmStatic
    @JvmName("withDefaultMapImmutableNotNull")
    public fun <K, V : Any> SerializerAwareValue<Map<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<NotNullMap<K, V>> {
        @Suppress("UNCHECKED_CAST") // magic
        return (this as SerializerAwareValue<MutableMap<K, V>>).withDefault(defaultValueComputer) as SerializerAwareValue<NotNullMap<K, V>>
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先调用 [defaultValueComputer] 并放入 [Map], 再返回调用的返回值
     */
    @JvmStatic
    @LowPriorityInOverloadResolution
    @JvmName("withDefaultMapImmutable")
    public fun <K, V> SerializerAwareValue<Map<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<Map<K, V>> {
        @Suppress("UNCHECKED_CAST") // magic
        return (this as SerializerAwareValue<MutableMap<K, V>>).withDefault(defaultValueComputer) as SerializerAwareValue<Map<K, V>>
    }

    @JvmStatic
    @JvmName("withDefaultMapNotNull")
    public fun <K, V : Any> SerializerAwareValue<MutableMap<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<NotNullMutableMap<K, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<K, V> {
                private val instance = NotNullMutableMap(createDelegateInstance(origin, defaultValueComputer))

                override var value: Map<K, V>
                    get() = instance
                    set(value) {
                        origin.value = value as MutableMap<K, V> // erased cast
                    }
            } as Value<NotNullMutableMap<K, V>>, // erased cast
            this.serializer
        )
    }

    /**
     * 创建一个代理对象, 当 [Map.get] 返回 `null` 时先调用 [defaultValueComputer] 并放入 [Map], 再返回调用的返回值
     */
    @LowPriorityInOverloadResolution
    @JvmStatic
    @JvmName("withDefaultMap")
    public fun <K, V> SerializerAwareValue<MutableMap<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<MutableMap<K, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<K, V> {
                private val instance = createDelegateInstance(origin, defaultValueComputer)
                override var value: Map<K, V>
                    get() = instance
                    set(value) {
                        origin.value = value as MutableMap<K, V> // erased cast
                    }
            } as Value<MutableMap<K, V>>, // erased cast
            this.serializer
        )
    }

    private fun <K, V> createDelegateInstance(
        origin: SerializerAwareValue<MutableMap<K, V>>,
        defaultValueComputer: (K) -> V,
    ): MutableMap<K, V> {
        return object : MutableMap<K, V>, AbstractMap<K, V>() {
            override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = origin.value.entries
            override val keys: MutableSet<K> get() = origin.value.keys
            override val values: MutableCollection<V> get() = origin.value.values
            override fun clear() = origin.value.clear()
            override fun putAll(from: Map<out K, V>) = origin.value.putAll(from)
            override fun remove(key: K): V? = origin.value.remove(key)
            override fun put(key: K, value: V): V? = origin.value.put(key, value)

            override fun get(key: K): V? {
                // the only difference
                val result = origin.value[key]
                if (result != null) return result
                put(key, defaultValueComputer(key))
                return origin.value[key]
            }
        }
    }


    /**
     * 替换 [MutableMap] 的 key
     */
    @JvmName("mapKeysNotNull")
    @JvmStatic
    public fun <OldK, NewK, V : Any> SerializerAwareValue<NotNullMutableMap<OldK, V>>.mapKeys(
        oldToNew: (OldK) -> NewK,
        newToOld: (NewK) -> OldK,
    ): SerializerAwareValue<NotNullMutableMap<NewK, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<NewK, V> {
                private val instance =
                    NotNullMutableMap(ShadowMap({ origin.value }, oldToNew, newToOld, { it }, { it }))

                override var value: Map<NewK, V>
                    get() = instance
                    set(value) {
                        origin.value =
                            value.mapKeysTo(NotNullMutableMap(LinkedHashMap())) { it.key.let(newToOld) } // erased cast
                    }
            } as Value<NotNullMutableMap<NewK, V>>, // erased cast
            this.serializer
        )
    }

    /**
     * 替换 [MutableMap] 的 key
     */
    @JvmName("mapKeys")
    @JvmStatic
    public fun <OldK, NewK, V> SerializerAwareValue<MutableMap<OldK, V>>.mapKeys(
        oldToNew: (OldK) -> NewK,
        newToOld: (NewK) -> OldK,
    ): SerializerAwareValue<MutableMap<NewK, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<NewK, V> {
                private val instance = ShadowMap({ origin.value }, oldToNew, newToOld, { it }, { it })

                override var value: Map<NewK, V>
                    get() = instance
                    set(value) {
                        origin.value = value.mapKeysTo(LinkedHashMap()) { it.key.let(newToOld) } // erased cast
                    }
            } as Value<MutableMap<NewK, V>>, // erased cast
            this.serializer
        )
    }

    /**
     * 替换 [Map] 的 key
     */
    @JvmName("mapKeysImmutable")
    @JvmStatic
    public fun <OldK, NewK, V> SerializerAwareValue<Map<OldK, V>>.mapKeys(
        oldToNew: (OldK) -> NewK,
        newToOld: (NewK) -> OldK,
    ): SerializerAwareValue<Map<NewK, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<NewK, V> {
                // casting Map to MutableMap is OK here, as we don't call mutable functions
                private val instance =
                    ShadowMap({ origin.value as MutableMap<OldK, V> }, oldToNew, newToOld, { it }, { it })

                override var value: Map<NewK, V>
                    get() = instance
                    set(value) {
                        origin.value = value.mapKeysTo(LinkedHashMap()) { it.key.let(newToOld) } // erased cast
                    }
            } as Value<Map<NewK, V>>, // erased cast
            this.serializer
        )
    }

    /**
     * 替换 [Map] 的 key
     */
    @JvmName("mapKeysImmutableNotNull")
    @JvmStatic
    public fun <OldK, NewK, V : Any> SerializerAwareValue<NotNullMap<OldK, V>>.mapKeys(
        oldToNew: (OldK) -> NewK,
        newToOld: (NewK) -> OldK,
    ): SerializerAwareValue<NotNullMap<NewK, V>> {
        val origin = this

        @Suppress("UNCHECKED_CAST")
        return SerializableValue(
            object : CompositeMapValue<NewK, V> {
                // casting Map to MutableMap is OK here, as we don't call mutable functions
                private val instance =
                    NotNullMap(ShadowMap({ origin.value as MutableMap<OldK, V> }, oldToNew, newToOld, { it }, { it }))

                override var value: Map<NewK, V>
                    get() = instance
                    set(value) {
                        origin.value =
                            value.mapKeysTo(NotNullMutableMap(LinkedHashMap())) { it.key.let(newToOld) } // erased cast
                    }
            } as Value<NotNullMap<NewK, V>>, // erased cast
            this.serializer
        )
    }
}







































