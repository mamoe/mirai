/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.setting.internal

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.setting.SerialName
import net.mamoe.mirai.console.setting.Setting
import net.mamoe.mirai.console.setting.Value
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlConfiguration
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal abstract class SettingImpl {

    @JvmField
    internal var valueList: MutableList<Pair<Value<*>, Setting.PropertyInfo>> = mutableListOf()

    @JvmField
    internal var built: Boolean = false

    internal val updaterSerializer: KSerializer<SettingSerializerMark> by lazy {
        built = true
        SettingUpdaterSerializer(this as Setting)
    }

    internal val kotlinSerializer: KSerializer<Setting> by lazy {
        object : KSerializer<Setting> {
            override val descriptor: SerialDescriptor
                get() = this@SettingImpl.updaterSerializer.descriptor

            override fun deserialize(decoder: Decoder): Setting {
                this@SettingImpl.updaterSerializer.deserialize(decoder)
                return this@SettingImpl as Setting
            }

            override fun serialize(encoder: Encoder, value: Setting) {
                this@SettingImpl.updaterSerializer.serialize(
                    encoder,
                    SettingSerializerMark
                )
            }
        }
    }

    abstract fun onElementChanged(value: Value<*>)

    companion object {
        @JvmStatic
        internal val yamlForToString =
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
        @OptIn(MiraiExperimentalAPI::class)
        SerialDescriptor(instance.serialName) {
            for ((value, prop) in instance.valueList) {
                val (serialName, annotations) = prop
                element(serialName, value.serializer.descriptor, annotations, true)
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
                val value = instance.valueList[index].first as Value<Any>
                value.value = this.decodeSerializableElement(
                    descriptor,
                    index,
                    value.serializer
                )
            }
        }
        SettingSerializerMark
    }

    private val emptyList = emptyList<String>()
    private val emptyListSerializer = ListSerializer(String.serializer())

    override fun serialize(encoder: Encoder, value: SettingSerializerMark) {
        if (instance.valueList.isEmpty()) {
            emptyListSerializer.serialize(encoder, emptyList)
        } else encoder.encodeStructure(descriptor) {
            instance.valueList.forEachIndexed { index, (value, _) ->
                @Suppress("UNCHECKED_CAST") // erased, no problem.
                this.encodeElementSmart(descriptor, index, value)
            }
        }
    }

}

// until https://github.com/Him188/yamlkt/issues/2 fixed
internal fun <T : Any> CompositeEncoder.encodeElementSmart(
    descriptor: SerialDescriptor,
    index: Int,
    value: Value<T>
) {
    when (value.value::class) {
        String::class -> this.encodeStringElement(descriptor, index, value.value as String)
        Int::class -> this.encodeIntElement(descriptor, index, value.value as Int)
        Byte::class -> this.encodeByteElement(descriptor, index, value.value as Byte)
        Char::class -> this.encodeCharElement(descriptor, index, value.value as Char)
        Long::class -> this.encodeLongElement(descriptor, index, value.value as Long)
        Float::class -> this.encodeFloatElement(descriptor, index, value.value as Float)
        Double::class -> this.encodeDoubleElement(descriptor, index, value.value as Double)
        Boolean::class -> this.encodeBooleanElement(descriptor, index, value.value as Boolean)
        else ->
            @Suppress("UNCHECKED_CAST")
            this.encodeSerializableElement(descriptor, index, value.serializer as KSerializer<Any>, value.value)
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

internal inline fun <E, R> KSerializer<E>.map(
    crossinline serializer: (R) -> E,
    crossinline deserializer: (E) -> R
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = this@map.descriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let(deserializer)
        override fun serialize(encoder: Encoder, value: R) = this@map.serialize(encoder, value.let(serializer))
    }
}
