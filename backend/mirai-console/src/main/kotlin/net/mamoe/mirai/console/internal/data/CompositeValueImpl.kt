/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.internal.data

import net.mamoe.mirai.console.data.*


// type inference bug
internal fun <T> PluginData.createCompositeSetValueImpl(tToValue: (T) -> Value<T>): CompositeSetValueImpl<T> {
    return object : CompositeSetValueImpl<T>(tToValue) {
        override fun onChanged() {
            this@createCompositeSetValueImpl.onValueChanged(this)
        }
    }
}

internal abstract class CompositeSetValueImpl<T>(
    tToValue: (T) -> Value<T> // should override onChanged
) : CompositeSetValue<T>, AbstractValueImpl<Set<T>>() {
    private val internalSet: MutableSet<Value<T>> = mutableSetOf()

    private var _value: Set<T> = internalSet.shadowMap({ it.value }, tToValue).observable { onChanged() }

    override var value: Set<T>
        get() = _value
        set(v) {
            if (_value != v) {
                @Suppress("LocalVariableName")
                val _value = _value as MutableSet<T>
                _value.clear()
                _value.addAll(v)
                onChanged()
            }
        }

    override fun setValueBySerializer(value: Set<T>) {
        val thisValue = this.value
        if (!thisValue.tryPatch(value)) {
            this.value = value // deep set
        }
    }

    protected abstract fun onChanged()
    override fun toString(): String = _value.toString()
    override fun equals(other: Any?): Boolean =
        other is CompositeSetValueImpl<*> && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return value.hashCode() * 31 + super.hashCode()
    }
}


// type inference bug
internal fun <T> PluginData.createCompositeListValueImpl(tToValue: (T) -> Value<T>): CompositeListValueImpl<T> {
    return object : CompositeListValueImpl<T>(tToValue) {
        override fun onChanged() {
            this@createCompositeListValueImpl.onValueChanged(this)
        }
    }
}

internal abstract class CompositeListValueImpl<T>(
    tToValue: (T) -> Value<T> // should override onChanged
) : CompositeListValue<T>, AbstractValueImpl<List<T>>() {
    private val internalList: MutableList<Value<T>> = mutableListOf()

    private val _value: List<T> = internalList.shadowMap({ it.value }, tToValue).observable { onChanged() }

    override var value: List<T>
        get() = _value
        set(v) {
            if (_value != v) {
                @Suppress("LocalVariableName")
                val _value = _value as MutableList<T>
                _value.clear()
                _value.addAll(v)
                onChanged()
            }
        }

    override fun setValueBySerializer(value: List<T>) {
        val thisValue = this.value
        if (!thisValue.tryPatch(value)) {
            this.value = value // deep set
        }
    }

    protected abstract fun onChanged()
    override fun toString(): String = _value.toString()
    override fun equals(other: Any?): Boolean =
        other is CompositeListValueImpl<*> && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return value.hashCode() * 31 + super.hashCode()
    }
}

// workaround to a type inference bug
internal fun <K, V> PluginData.createCompositeMapValueImpl(
    kToValue: (K) -> Value<K>,
    vToValue: (V) -> Value<V>,
    valueToK: (Value<K>) -> K = Value<K>::value,
    valueToV: (Value<V>) -> V = Value<V>::value,
    applyToShadowedMap: ((MutableMap<K, V>) -> (MutableMap<K, V>))? = null
): CompositeMapValueImpl<K, V> {
    return object : CompositeMapValueImpl<K, V>(kToValue, vToValue, valueToK, valueToV, applyToShadowedMap) {
        override fun onChanged() = this@createCompositeMapValueImpl.onValueChanged(this)
    }
}

// TODO: 2020/6/24 在一个 Value 被删除后停止追踪其更新.

internal abstract class CompositeMapValueImpl<K, V>(
    @JvmField internal val kToValue: (K) -> Value<K>, // should override onChanged
    @JvmField internal val vToValue: (V) -> Value<V>, // should override onChanged
    @JvmField internal val valueToK: (Value<K>) -> K = Value<K>::value,
    @JvmField internal val valueToV: (Value<V>) -> V = Value<V>::value,
    applyToShadowedMap: ((MutableMap<K, V>) -> (MutableMap<K, V>))? = null
) : CompositeMapValue<K, V>, AbstractValueImpl<Map<K, V>>() {
    @JvmField
    internal val internalList: MutableMap<Value<K>, Value<V>> = mutableMapOf()

    private var _value: MutableMap<K, V> =
        internalList.shadowMap(valueToK, kToValue, valueToV, vToValue).let {
            applyToShadowedMap?.invoke(it) ?: it
        }.observable { onChanged() }

    override var value: Map<K, V>
        get() = _value
        set(v) {
            if (_value != v) {
                @Suppress("LocalVariableName")
                val _value = _value
                _value.clear()
                _value.putAll(v)
                onChanged()
            }
        }

    override fun setValueBySerializer(value: Map<K, V>) {
        val thisValue = this.value as MutableMap<K, V>
        if (!thisValue.tryPatch(value)) {
            this.value = value // deep set
        }
    }

    protected abstract fun onChanged()
    override fun toString(): String = _value.toString()
    override fun equals(other: Any?): Boolean =
        other is CompositeMapValueImpl<*, *> && other::class.java == this::class.java && other._value == this._value

    override fun hashCode(): Int {
        val value = _value
        return value.hashCode() * 31 + super.hashCode()
    }
}

internal fun <K, V> MutableMap<K, V>.patchImpl(_new: Map<K, V>) {
    val new = _new.toMutableMap()
    val iterator = this.iterator()
    for (entry in iterator) {
        val newValue = new.remove(entry.key)

        if (newValue != null) {
            // has replacer
            if (entry.value?.tryPatch(newValue) != true) {
                // patch not supported, or old value is null
                entry.setValue(newValue)
            } // else: patched, no remove
        } else {
            // no replacer
            iterator.remove()
        }
    }
    putAll(new)
}

internal fun <C : MutableCollection<E>, E> C.patchImpl(_new: Collection<E>) {
    this.clear()
    this.addAll(_new)
}

/**
 * True if successfully patched
 */
@Suppress("UNCHECKED_CAST")
internal fun Any.tryPatch(any: Any): Boolean = when {
    this is MutableCollection<*> && any is Collection<*> -> {
        (this as MutableCollection<Any?>).patchImpl(any as Collection<Any?>)
        true
    }
    this is MutableMap<*, *> && any is Map<*, *> -> {
        (this as MutableMap<Any?, Any?>).patchImpl(any as Map<Any?, Any?>)
        true
    }
    this is Value<*> && any is Value<*> -> any.value?.let { otherValue -> this.value?.tryPatch(otherValue) } == true
    else -> false
}