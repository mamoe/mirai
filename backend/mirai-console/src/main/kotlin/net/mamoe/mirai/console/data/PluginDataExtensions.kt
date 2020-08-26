package net.mamoe.mirai.console.data

import net.mamoe.mirai.console.internal.data.CompositeMapValueImpl
import net.mamoe.mirai.console.internal.data.castOrInternalError
import net.mamoe.mirai.console.internal.data.createCompositeMapValueImpl

@Suppress("INAPPLICABLE_JVM_NAME", "UNCHECKED_CAST")
public interface PluginDataExtensions {

    @JvmName("withDefaultImmutable")
    public fun <V, K> SerializerAwareValue<Map<K, V>>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<Map<K, V>> {
        return (this as SerializerAwareValue<MutableMap<K, V>>).withDefault(defaultValueComputer) as SerializerAwareValue<Map<K, V>>
    }

    @JvmName("withDefaultImmutableMap")
    public fun <M : Map<K, V>, V : Map<*, *>, K> SerializerAwareValue<M>.withEmptyDefault(): SerializerAwareValue<M> {
        return this.withDefault { LinkedHashMap<Any?, Any?>() as V }
    }

    @JvmName("withDefaultImmutableSet")
    public fun <M : Map<K, V>, V : Set<*>, K> SerializerAwareValue<M>.withEmptyDefault(): SerializerAwareValue<M> {
        return this.withDefault { LinkedHashSet<Any?>() as V }
    }

    @JvmName("withDefaultImmutableList")
    public fun <M : Map<K, V>, V : List<*>, K> SerializerAwareValue<M>.withEmptyDefault(): SerializerAwareValue<M> {
        return this.withDefault { ArrayList<Any?>() as V }
    }

    public fun <M : Map<K, V>, V, K> SerializerAwareValue<M>.withDefault(defaultValueComputer: (K) -> V): SerializerAwareValue<M> {
        val pluginData = this@PluginDataExtensions.castOrInternalError<PluginData>()


        val origin = (this as SerializableValue<M>).delegate.castOrInternalError<CompositeMapValueImpl<K, V>>()

        return SerializableValue(
            object : CompositeMapValue<K, V> {
                private val instance = object : MutableMap<K, V> {
                    override val size: Int get() = origin.value.size
                    override fun containsKey(key: K): Boolean = origin.value.containsKey(key)
                    override fun containsValue(value: V): Boolean = origin.value.containsValue(value)
                    override fun isEmpty(): Boolean = origin.value.isEmpty()
                    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = origin.value.entries as MutableSet<MutableMap.MutableEntry<K, V>>
                    override val keys: MutableSet<K> get() = origin.value.keys as MutableSet<K>
                    override val values: MutableCollection<V> get() = origin.value.values as MutableCollection<V>
                    override fun clear() = (origin.value as MutableMap<K, V>).clear()
                    override fun putAll(from: Map<out K, V>) = (origin.value as MutableMap<K, V>).putAll(from)
                    override fun remove(key: K): V? = (origin.value as MutableMap<K, V>).remove(key)
                    override fun put(key: K, value: V): V? = (origin.value as MutableMap<K, V>).put(key, value)

                    override fun get(key: K): V? {
                        // the only difference
                        val result = origin.value[key]
                        if (result != null) {
                            return result
                        }
                        put(key, defaultValueComputer(key))
                        return origin.value[key]
                    }
                }

                override var value: Map<K, V>
                    get() = instance
                    set(value) {
                        origin.value = value
                    }
            } as Value<M>,
            this.serializer
        )
        return pluginData.createCompositeMapValueImpl(
            kToValue = origin.kToValue,
            vToValue = origin.vToValue,
            applyToShadowedMap = { theMap ->
                object : MutableMap<K, V> by theMap {
                    override fun get(key: K): V? {
                        val result = theMap[key]
                        if (result != null) return result
                        theMap[key] = defaultValueComputer(key)
                        return theMap[key]
                    }
                }
            }
        ).let { SerializableValue(it, serializer) } as SerializerAwareValue<M>
    }

}