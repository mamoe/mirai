/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.internal.setting

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.console.setting.AbstractSetting.ValueNode
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.Value
import net.mamoe.yamlkt.YamlNullableDynamicSerializer
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal val KProperty<*>.serialName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name

/**
 * Internal implementation for [Setting] including:
 * - Reflection on Kotlin properties and Java fields
 * - Auto-saving
 */
internal abstract class SettingImpl {
    internal fun findNodeInstance(name: String): ValueNode<*>? = valueNodes.firstOrNull { it.serialName == name }

    internal abstract val valueNodes: MutableList<ValueNode<*>>

    internal open val updaterSerializer: KSerializer<Unit> = object : KSerializer<Unit> {
        override val descriptor: SerialDescriptor get() = settingUpdaterSerializerDescriptor

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(decoder: Decoder) {
            val descriptor = descriptor
            with(decoder.beginStructure(descriptor)) {
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
                            if (index == CompositeDecoder.DECODE_DONE) {
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
            with(encoder.beginCollection(descriptor, valueNodes.size)) {
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
    abstract fun onValueChanged(value: Value<*>)

    companion object {
        private val settingUpdaterSerializerTypeArguments = arrayOf(String.serializer(), YamlNullableDynamicSerializer)
        private val settingUpdaterSerializerDescriptor =
            MapSerializer(settingUpdaterSerializerTypeArguments[0], settingUpdaterSerializerTypeArguments[1]).descriptor
    }
}