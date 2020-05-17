/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting

import kotlinx.serialization.*
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlConfiguration
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal abstract class AbstractSetting {

    @JvmField
    internal var valueList: MutableList<Pair<Value<*>, KProperty<*>>> = mutableListOf()

    @JvmField
    internal var built: Boolean = false

    internal val updaterSerializer: KSerializer<SettingSerializerMark> by lazy {
        built = true
        SettingUpdaterSerializer(this as Setting)
    }

    internal val kotlinSerializer: KSerializer<Setting> by lazy {
        object : KSerializer<Setting> {
            override val descriptor: SerialDescriptor
                get() = this@AbstractSetting.updaterSerializer.descriptor

            override fun deserialize(decoder: Decoder): Setting {
                this@AbstractSetting.updaterSerializer.deserialize(decoder)
                return this@AbstractSetting as Setting
            }

            override fun serialize(encoder: Encoder, value: Setting) {
                this@AbstractSetting.updaterSerializer.serialize(encoder, SettingSerializerMark)
            }
        }
    }

    internal fun onElementChanged(value: Value<*>) {
        println("my value changed!")
    }

    companion object {
        @JvmStatic
        internal val yaml =
            Yaml(
                configuration = YamlConfiguration(
                    nonStrictNullability = true,
                    nonStrictNumber = true,
                    stringSerialization = YamlConfiguration.StringSerialization.NONE,
                    classSerialization = YamlConfiguration.MapSerialization.FLOW_MAP,
                    listSerialization = YamlConfiguration.ListSerialization.FLOW_SEQUENCE
                )
            )
    }
}

internal class SettingUpdaterSerializer(
    private val instance: Setting
) : KSerializer<SettingSerializerMark> {
    override val descriptor: SerialDescriptor by lazy {
        SerialDescriptor(instance.serialName) {
            for ((value, property) in instance.valueList) {
                element(property.serialNameOrPropertyName, value.serializer.descriptor, annotations, true)
            }
        }
    }

    @Suppress("UNCHECKED_CAST") // erased, no problem.
    override fun deserialize(decoder: Decoder): SettingSerializerMark = decoder.decodeStructure(descriptor) {
        if (this.decodeSequentially()) {
            instance.valueList.forEachIndexed { index, (value, _) ->
                val v = value as Value<Any>
                v.value = this.decodeSerializableElement(
                    value.serializer.descriptor,
                    index,
                    v.serializer
                )
            }
        } else {
            while (true) {
                val index = this.decodeElementIndex(descriptor)
                if (index == CompositeDecoder.READ_DONE) return@decodeStructure SettingSerializerMark
                val value = instance.valueList[index].first

                this.decodeSerializableElement(
                    descriptor,
                    index,
                    value.serializer
                )
            }
        }
        SettingSerializerMark
    }

    override fun serialize(encoder: Encoder, value: SettingSerializerMark) = encoder.encodeStructure(descriptor) {
        instance.valueList.forEachIndexed { index, (value, _) ->
            @Suppress("UNCHECKED_CAST") // erased, no problem.
            this.encodeSerializableElement(
                descriptor,
                index,
                value.serializer as KSerializer<Any>,
                value.value
            )
        }
    }

}

internal object SettingSerializerMark

internal val KProperty<*>.serialNameOrPropertyName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name

internal inline fun <E> KSerializer<E>.bind(
    crossinline setter: (E) -> Unit,
    crossinline getter: () -> E
): KSerializer<E> {
    return object : KSerializer<E> {
        override val descriptor: SerialDescriptor get() = this@bind.descriptor
        override fun deserialize(decoder: Decoder): E = this@bind.deserialize(decoder).also { setter(it) }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(encoder: Encoder, value: E) =
            this@bind.serialize(encoder, getter())
    }
}
