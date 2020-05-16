/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.setting

import kotlinx.serialization.*
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlConfiguration
import net.mamoe.yamlkt.YamlConfiguration.ListSerialization.FLOW_SEQUENCE
import net.mamoe.yamlkt.YamlConfiguration.MapSerialization.FLOW_MAP
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

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

typealias SerialName = kotlinx.serialization.SerialName

/**
 * 配置的基类
 */
abstract class Setting {
    open val serialName: String
        get() = this::class.findAnnotation<SerialName>()?.value
            ?: this::class.qualifiedName
            ?: error("Names should be assigned to anonymous classes manually by overriding serialName")


    @JvmSynthetic
    operator fun <T : Any> Value<T>.provideDelegate(
        thisRef: Setting,
        property: KProperty<*>
    ): ReadWriteProperty<Setting, T> {
        if (built) error("The Setting is already serialized so it's structure is immutable.")
        valueList.add(this to property)
        return this
    }

    @JvmField
    internal var valueList: MutableList<Pair<Value<*>, KProperty<*>>> = mutableListOf()
    private var built: Boolean = false

    internal val updaterSerializer: KSerializer<SettingSerializerMark> by lazy {
        built = true
        SettingUpdaterSerializer(this)
    }

    internal val kotlinSerializer: KSerializer<Setting> by lazy {
        object : KSerializer<Setting> {
            override val descriptor: SerialDescriptor
                get() = this@Setting.updaterSerializer.descriptor

            override fun deserialize(decoder: Decoder): Setting {
                this@Setting.updaterSerializer.deserialize(decoder)
                return this@Setting
            }

            override fun serialize(encoder: Encoder, value: Setting) {
                this@Setting.updaterSerializer.serialize(encoder, SettingSerializerMark)
            }
        }
    }

    internal fun onElementChanged(value: Value<*>) {
        println("my value changed!")
    }

    companion object {
        private val yaml =
            Yaml(configuration = YamlConfiguration(classSerialization = FLOW_MAP, listSerialization = FLOW_SEQUENCE))
    }

    override fun toString(): String = yaml.stringify(this.serializer, this)
}

@Suppress("UNCHECKED_CAST")
val <T : Setting> T.serializer: KSerializer<T>
    get() = kotlinSerializer as KSerializer<T>

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
                this.decodeSerializableElement(
                    value.serializer.descriptor,
                    index,
                    value.serializer as KSerializer<Any>
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


fun <T : Setting> Setting.value(default: T): Value<T> = valueImpl(default)
inline fun <T : Setting> Setting.value(default: T, crossinline initializer: T.() -> Unit): Value<T> =
    value(default).also { it.value.apply(initializer) }
