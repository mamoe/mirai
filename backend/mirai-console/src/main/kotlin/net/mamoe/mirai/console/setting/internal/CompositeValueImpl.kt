/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.setting.internal

import net.mamoe.mirai.console.setting.*


// type inference bug
internal fun <T> Setting.createCompositeSetValueImpl(tToValue: (T) -> Value<T>): CompositeSetValueImpl<T> {
    return object : CompositeSetValueImpl<T>(tToValue) {
        override fun onChanged() {
            this@createCompositeSetValueImpl.onValueChanged(this)
        }
    }
}

internal abstract class CompositeSetValueImpl<T>(
    tToValue: (T) -> Value<T> // should override onChanged
) : CompositeSetValue<T> {
    private val internalSet: MutableSet<Value<T>> = mutableSetOf()

    private var _value: Set<T> = internalSet.shadowMap({ it.value }, tToValue).observable { onChanged() }

    override var value: Set<T>
        get() = _value
        set(v) {
            if (_value != v) {
                onChanged()
                _value = v
            }
        }

    protected abstract fun onChanged()
}


// type inference bug
internal fun <T> Setting.createCompositeListValueImpl(tToValue: (T) -> Value<T>): CompositeListValueImpl<T> {
    return object : CompositeListValueImpl<T>(tToValue) {
        override fun onChanged() {
            this@createCompositeListValueImpl.onValueChanged(this)
        }
    }
}

internal abstract class CompositeListValueImpl<T>(
    tToValue: (T) -> Value<T> // should override onChanged
) : CompositeListValue<T> {
    private val internalList: MutableList<Value<T>> = mutableListOf()

    private var _value: List<T> = internalList.shadowMap({ it.value }, tToValue).observable { onChanged() }

    override var value: List<T>
        get() = _value
        set(v) {
            if (_value != v) {
                onChanged()
                _value = v
            }
        }

    protected abstract fun onChanged()
}

// workaround to a type inference bug
internal fun <K, V> Setting.createCompositeMapValueImpl(
    kToValue: (K) -> Value<K>,
    vToValue: (V) -> Value<V>
): CompositeMapValueImpl<K, V> {
    return object : CompositeMapValueImpl<K, V>(kToValue, vToValue) {
        override fun onChanged() {
            this@createCompositeMapValueImpl.onValueChanged(this)
        }
    }
}

internal abstract class CompositeMapValueImpl<K, V>(
    kToValue: (K) -> Value<K>, // should override onChanged
    vToValue: (V) -> Value<V> // should override onChanged
) : CompositeMapValue<K, V> {
    private val internalList: MutableMap<Value<K>, Value<V>> = mutableMapOf()

    private var _value: Map<K, V> =
        internalList.shadowMap({ it.value }, kToValue, { it.value }, vToValue).observable { onChanged() }
    override var value: Map<K, V>
        get() = _value
        set(v) {
            if (_value != v) {
                onChanged()
                _value = v
            }
        }

    protected abstract fun onChanged()
}