/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.console.setting.internal

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import net.mamoe.mirai.console.setting.*
import net.mamoe.yamlkt.YamlDynamicSerializer
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.typeOf

/// region MUTABLE LIST

@PublishedApi
@JvmName("valueImplSetting")
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Setting> Setting.valueImpl(
    default: List<T>
): SettingListValue<T> = valueImpl(default, T::class.createInstance().serializer)

@PublishedApi
@JvmName("valueImplSettingMutable")
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Setting> Setting.valueImpl(
    default: MutableList<T>
): MutableSettingListValue<T> = valueImpl(default, T::class.createInstance().serializer)


/*
@PublishedApi
@JvmName("valueImpl1")
internal fun <T : Any> Setting.valueImpl(
    default: MutableList<T>,
    valueMapper: (T) -> Value<T>,
    elementSerializer: KSerializer<T>
): MutableListValue<T> = valueImpl(default.mapTo(mutableListOf(), valueMapper), valueMapper, elementSerializer)
*/
internal fun <T : Any> Setting.valueImpl(
    default: MutableList<Value<T>>,
    valueMapper: (T) -> Value<T>,
    elementSerializer: KSerializer<T>
): MutableListValue<T> {
    var internalValue: MutableList<Value<T>> = default

    fun updateShadow(): MutableList<T> =
        internalValue.shadowMap(transform = { it.value }, transformBack = { valueMapper(it) })

    var shadowed: MutableList<T> = updateShadow()


    val delegt = dynamicMutableList { shadowed }
    return object : MutableListValue<T>(), MutableList<T> by delegt {
        override var value: MutableList<Value<T>>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    shadowed = updateShadow()
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<MutableList<Value<T>>> = object : KSerializer<MutableList<Value<T>>> {
            private val delegate = ListSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): MutableList<Value<T>> {
                return delegate.deserialize(decoder).mapTo(mutableListOf(), valueMapper)
            }

            override fun serialize(encoder: Encoder, value: MutableList<Value<T>>) {
                delegate.serialize(encoder, value.map { it.value })
            }

        }
    }
}

@PublishedApi
internal fun <T : Setting> Setting.valueImpl(
    default: MutableList<T>,
    elementSerializer: KSerializer<T>
): MutableSettingListValue<T> {
    var internalValue: MutableList<T> = default

    val delegt = dynamicMutableList { internalValue }
    return object : MutableSettingListValue<T>(), MutableList<T> by delegt {
        override var value: MutableList<T>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<MutableList<T>> = object : KSerializer<MutableList<T>> {
            private val delegate = ListSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): MutableList<T> {
                return delegate.deserialize(decoder).toMutableList() // TODO: 2020/5/17 ATTACH OBSERVER 
            }

            override fun serialize(encoder: Encoder, value: MutableList<T>) {
                delegate.serialize(encoder, value)
            }

        }
    }
}

@PublishedApi
internal fun <T : Setting> Setting.valueImpl(
    default: List<T>,
    elementSerializer: KSerializer<T>
): SettingListValue<T> {
    var internalValue: List<T> = default

    val delegt = dynamicList { internalValue }
    return object : SettingListValue<T>(), List<T> by delegt {
        override var value: List<T>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<List<T>> = object : KSerializer<List<T>> {
            private val delegate = ListSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): List<T> {
                return delegate.deserialize(decoder) // TODO: 2020/5/17 ATTACH OBSERVER
            }

            override fun serialize(encoder: Encoder, value: List<T>) {
                delegate.serialize(encoder, value)
            }

        }
    }
}

@PublishedApi
internal fun <T : Setting> Setting.valueImpl(
    default: Set<T>,
    elementSerializer: KSerializer<T>
): SettingSetValue<T> {
    var internalValue: Set<T> = default

    val delegt = dynamicSet { internalValue }
    return object : SettingSetValue<T>(), Set<T> by delegt {
        override var value: Set<T>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<Set<T>> = object : KSerializer<Set<T>> {
            private val delegate = SetSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): Set<T> {
                return delegate.deserialize(decoder) // TODO: 2020/5/17 ATTACH OBSERVER
            }

            override fun serialize(encoder: Encoder, value: Set<T>) {
                delegate.serialize(encoder, value)
            }

        }
    }
}

@PublishedApi
internal fun <T : Setting> Setting.valueImpl(
    default: MutableSet<T>,
    elementSerializer: KSerializer<T>
): MutableSettingSetValue<T> {
    var internalValue: MutableSet<T> = default

    val delegt = dynamicMutableSet { internalValue }
    return object : MutableSettingSetValue<T>(), MutableSet<T> by delegt {
        override var value: MutableSet<T>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<MutableSet<T>> = object : KSerializer<MutableSet<T>> {
            private val delegate = SetSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): MutableSet<T> {
                return delegate.deserialize(decoder).toMutableSet() // TODO: 2020/5/17 ATTACH OBSERVER 
            }

            override fun serialize(encoder: Encoder, value: MutableSet<T>) {
                delegate.serialize(encoder, value)
            }

        }
    }
}

// endregion


// region MUTABLE SET

@PublishedApi
@JvmName("valueImplSetting")
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Setting> Setting.valueImpl(
    default: Set<T>
): SettingSetValue<T> = valueImpl(default, T::class.createInstance().serializer)

@PublishedApi
@JvmName("valueImplSettingMutable")
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Setting> Setting.valueImpl(
    default: MutableSet<T>
): MutableSettingSetValue<T> = valueImpl(default, T::class.createInstance().serializer)

/*
@JvmName("valueImpl1")
@PublishedApi
internal fun <T : Any> Setting.valueImpl(
    default: MutableSet<T>,
    valueMapper: (T) -> Value<T>,
    elementSerializer: KSerializer<T>
): MutableSetValue<T> = valueImpl(default.mapTo(mutableSetOf(), valueMapper), valueMapper, elementSerializer)
*/
@JvmName("valueImplMutable")
internal fun <T : Any> Setting.valueImpl(
    default: MutableSet<Value<T>>,
    valueMapper: (T) -> Value<T>,
    elementSerializer: KSerializer<T>
): MutableSetValue<T> {
    var internalValue: MutableSet<Value<T>> = default

    fun updateShadow(): MutableSet<T> =
        internalValue.shadowMap(transform = { it.value }, transformBack = { valueMapper(it) })

    var shadowed: MutableSet<T> = updateShadow()

    val delegt = dynamicMutableSet { shadowed }
    return object : MutableSetValue<T>(), MutableSet<T> by delegt {
        override var value: MutableSet<Value<T>>
            get() = internalValue
            set(new) {
                if (new != internalValue) {
                    internalValue = new
                    shadowed = updateShadow()
                    onElementChanged(this)
                }
            }
        override val serializer: KSerializer<MutableSet<Value<T>>> = object : KSerializer<MutableSet<Value<T>>> {
            private val delegate = SetSerializer(elementSerializer)
            override val descriptor: SerialDescriptor = delegate.descriptor

            override fun deserialize(decoder: Decoder): MutableSet<Value<T>> {
                return delegate.deserialize(decoder).mapTo(mutableSetOf(), valueMapper)
            }

            override fun serialize(encoder: Encoder, value: MutableSet<Value<T>>) {
                delegate.serialize(encoder, value.mapTo(mutableSetOf()) { it.value })
            }
        }
    }
}

// endregion 

// region DYNAMIC PRIMITIVES AND SERIALIZABLE

/**
 * For primitives and serializable only
 */
@OptIn(ExperimentalStdlibApi::class)
@PublishedApi
@LowPriorityInOverloadResolution
internal inline fun <reified T : Any> Setting.valueImpl(default: T): Value<T> =
    valueImpl(default, T::class)

@PublishedApi
internal fun <T : Any> Setting.valueImpl(default: T, clazz: KClass<out T>): Value<T> {
    if (default is Setting) @Suppress("UNCHECKED_CAST") return valueImpl(default as Setting) as Value<T>

    @OptIn(ImplicitReflectionSerializer::class)
    requireNotNull(clazz.serializerOrNull()) {
        "${clazz.qualifiedName} is not serializable"
    }
    return object : DynamicReferenceValue<T>() {
        override var value: T = default
        override val serializer: KSerializer<T> = object : KSerializer<T> {
            override val descriptor: SerialDescriptor
                get() = YamlDynamicSerializer.descriptor

            override fun deserialize(decoder: Decoder): T =
                YamlDynamicSerializer.deserialize(decoder).smartCastPrimitive(clazz)

            override fun serialize(encoder: Encoder, value: T) = YamlDynamicSerializer.serialize(encoder, value)
        }
    }
}

// endregion