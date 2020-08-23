/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.internal.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.PluginData.ValueNode
import net.mamoe.mirai.console.data.Value
import net.mamoe.yamlkt.YamlNullableDynamicSerializer

/**
 * Internal implementation for [PluginData] including:
 * - Reflection on Kotlin properties and Java fields
 * - Auto-saving
 */
internal abstract class PluginDataImpl {
    internal fun findNodeInstance(name: String): ValueNode<*>? = valueNodes.firstOrNull { it.valueName == name }

    internal abstract val valueNodes: MutableList<ValueNode<*>>

    internal open val updaterSerializer: KSerializer<Unit> = object : KSerializer<Unit> {
        override val descriptor: SerialDescriptor get() = dataUpdaterSerializerDescriptor

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(decoder: Decoder) {
            val descriptor = descriptor
            with(decoder.beginStructure(descriptor)) {
                if (decodeSequentially()) {
                    var index = 0
                    repeat(decodeCollectionSize(descriptor)) {
                        val valueName = decodeSerializableElement(descriptor, index++, String.serializer())
                        val node = findNodeInstance(valueName)
                        if (node == null) {
                            decodeSerializableElement(descriptor, index++, YamlNullableDynamicSerializer)
                        } else {
                            decodeSerializableElement(descriptor, index++, node.updaterSerializer)
                        }
                    }
                } else {
                    outerLoop@ while (true) {
                        var valueName: String? = null
                        innerLoop@ while (true) {
                            val index = decodeElementIndex(descriptor)
                            if (index == CompositeDecoder.DECODE_DONE) {
                                check(valueName == null) { "name must be null at this moment." }
                                break@outerLoop
                            }

                            if (!index.isOdd()) { // key
                                check(valueName == null) { "name must be null at this moment" }
                                valueName = decodeSerializableElement(descriptor, index, String.serializer())
                            } else {
                                check(valueName != null) { "name must not be null at this moment" }

                                val node = findNodeInstance(valueName)
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

                // val vSerializer = dataUpdaterSerializerTypeArguments[1] as KSerializer<Any?>
                valueNodes.forEach { (valueName, _, valueSerializer) ->
                    encodeSerializableElement(descriptor, index++, String.serializer(), valueName)
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
        private val dataUpdaterSerializerTypeArguments = arrayOf(String.serializer(), YamlNullableDynamicSerializer)
        private val dataUpdaterSerializerDescriptor =
            MapSerializer(dataUpdaterSerializerTypeArguments[0], dataUpdaterSerializerTypeArguments[1]).descriptor
    }
}