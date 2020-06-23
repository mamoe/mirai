/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.internal.*
import net.mamoe.yamlkt.YamlNullableDynamicSerializer
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation


// TODO: 2020/6/21 move to JvmPlugin to inherit SettingStorage and CoroutineScope for saving
// Shows public APIs such as deciding when to auto-save.
abstract class Setting : SettingImpl() {

    operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> {
        val name = property.serialName
        valueNodes.add(Node(name, this, this.serializer))
        return this
    }

    public override val updaterSerializer: KSerializer<Unit> get() = super.updaterSerializer
}

internal val KProperty<*>.serialName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name

/**
 * Internal implementation for [Setting] including:
 * - Reflection on Kotlin properties and Java fields
 * - Auto-saving
 */
// TODO move to internal package.
internal abstract class SettingImpl {
    internal fun findNodeInstance(name: String): Node<*>? = valueNodes.firstOrNull { it.serialName == name }

    internal data class Node<T>(
        val serialName: String,
        val value: Value<T>,
        val updaterSerializer: KSerializer<Unit>
    )

    internal val valueNodes: MutableList<Node<*>> = mutableListOf()

    internal open val updaterSerializer: KSerializer<Unit> = object : KSerializer<Unit> {
        override val descriptor: SerialDescriptor get() = settingUpdaterSerializerDescriptor

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(decoder: Decoder) {
            val descriptor = descriptor
            with(decoder.beginStructure(descriptor, *settingUpdaterSerializerTypeArguments)) {
                if (decodeSequentially()) {
                    var index = 0
                    repeat(decodeCollectionSize(descriptor)) {
                        val serialName = decodeSerializableElement(descriptor, index++, String.serializer())
                        val node = findNodeInstance(serialName)
                        if (node == null) {
                            decodeSerializableElement(descriptor, index++, YamlNullableDynamicSerializer)
                        } else {
                            decodeSerializableElement(descriptor, index++, node.updaterSerializer)
                        }
                    }
                } else {
                    outerLoop@ while (true) {
                        var serialName: String? = null
                        innerLoop@ while (true) {
                            val index = decodeElementIndex(descriptor)
                            if (index == CompositeDecoder.READ_DONE) {
                                check(serialName == null) { "name must be null at this moment." }
                                break@outerLoop
                            }

                            if (!index.isOdd()) { // key
                                check(serialName == null) { "name must be null at this moment" }
                                serialName = decodeSerializableElement(descriptor, index, String.serializer())
                            } else {
                                check(serialName != null) { "name must not be null at this moment" }

                                val node = findNodeInstance(serialName)
                                if (node == null) {
                                    decodeSerializableElement(descriptor, index, YamlNullableDynamicSerializer)
                                } else {
                                    decodeSerializableElement(descriptor, index, node.updaterSerializer)
                                }


                                break@innerLoop
                            }
                        }

                    }
                }
                endStructure(descriptor)
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(encoder: Encoder, value: Unit) {
            val descriptor = descriptor
            with(encoder.beginCollection(descriptor, valueNodes.size, *settingUpdaterSerializerTypeArguments)) {
                var index = 0

                // val vSerializer = settingUpdaterSerializerTypeArguments[1] as KSerializer<Any?>
                valueNodes.forEach { (serialName, _, valueSerializer) ->
                    encodeSerializableElement(descriptor, index++, String.serializer(), serialName)
                    encodeSerializableElement(descriptor, index++, valueSerializer, Unit)
                }
                endStructure(descriptor)
            }
        }

    }

    /**
     * flatten
     */
    internal fun onValueChanged(value: Value<*>) {
        // TODO: 2020/6/22
    }

    companion object {
        private val settingUpdaterSerializerTypeArguments = arrayOf(String.serializer(), YamlNullableDynamicSerializer)
        private val settingUpdaterSerializerDescriptor =
            MapSerializer(settingUpdaterSerializerTypeArguments[0], settingUpdaterSerializerTypeArguments[1]).descriptor
    }
}


//// region Setting_value_primitives CODEGEN ////

fun Setting.value(default: Byte): SerializerAwareValue<Byte> = valueImpl(default)
fun Setting.value(default: Short): SerializerAwareValue<Short> = valueImpl(default)
fun Setting.value(default: Int): SerializerAwareValue<Int> = valueImpl(default)
fun Setting.value(default: Long): SerializerAwareValue<Long> = valueImpl(default)
fun Setting.value(default: Float): SerializerAwareValue<Float> = valueImpl(default)
fun Setting.value(default: Double): SerializerAwareValue<Double> = valueImpl(default)
fun Setting.value(default: Char): SerializerAwareValue<Char> = valueImpl(default)
fun Setting.value(default: Boolean): SerializerAwareValue<Boolean> = valueImpl(default)
fun Setting.value(default: String): SerializerAwareValue<String> = valueImpl(default)

//// endregion Setting_value_primitives CODEGEN ////


/**
 * Creates a [Value] with reified type, and set default value.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@Suppress("UNCHECKED_CAST")
@LowPriorityInOverloadResolution
inline fun <reified T> Setting.value(default: T): SerializerAwareValue<T> = valueFromKType(typeOf0<T>(), default)

/**
 * Creates a [Value] with reified type, and set default value by reflection to its no-arg public constructor.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@LowPriorityInOverloadResolution
inline fun <reified T> Setting.value(): SerializerAwareValue<T> = value(T::class.createInstance() as T)

/**
 * Creates a [Value] with specified [KType], and set default value.
 */
@Suppress("UNCHECKED_CAST")
fun <T> Setting.valueFromKType(type: KType, default: T): SerializerAwareValue<T> =
    (valueFromKTypeImpl(type) as SerializerAwareValue<Any?>).apply { this.value = default } as SerializerAwareValue<T>

// TODO: 2020/6/24 Introduce class TypeToken for compound types for Java.