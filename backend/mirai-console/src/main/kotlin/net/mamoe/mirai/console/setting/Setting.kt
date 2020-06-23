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
import net.mamoe.mirai.console.setting.internal.cast
import net.mamoe.mirai.console.setting.internal.valueFromKTypeImpl
import net.mamoe.mirai.console.setting.internal.valueImpl
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.yamlkt.Yaml
import net.mamoe.yamlkt.YamlConfiguration
import net.mamoe.yamlkt.YamlConfiguration.ListSerialization.FLOW_SEQUENCE
import net.mamoe.yamlkt.YamlConfiguration.MapSerialization.FLOW_MAP
import java.util.*
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.typeOf


// TODO: 2020/6/21 move to JvmPlugin to inherit SettingStorage and CoroutineScope for saving
// Shows public APIs such as deciding when to auto-save.
abstract class Setting : SettingImpl() {

    operator fun <T> SerializerAwareValue<T>.provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): SerializerAwareValue<T> {
        val name = property.serialName
        valueNodes.put(name, Node(name, this, this.serializer))
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
    internal fun findNodeInstance(name: String): Node<*>? = valueNodes[name]

    internal class Node<T>(
        val serialName: String,
        val value: Value<T>,
        val updaterSerializer: KSerializer<Unit>
    )

    internal val valueNodes: MutableMap<String, Node<*>> = Collections.synchronizedMap(mutableMapOf())

    internal open val updaterSerializer: KSerializer<Unit> by lazy {
        val actual = MapSerializer(String.serializer(), String.serializer())

        object : KSerializer<Unit> {
            override val descriptor: SerialDescriptor
                get() = actual.descriptor

            override fun deserialize(decoder: Decoder) {
                actual.deserialize(decoder)
            }

            override fun serialize(encoder: Encoder, value: Unit) {
                TODO()
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
        val allFlow = Yaml(
            YamlConfiguration(
                nonStrictNullability = true,
                nonStrictNumber = true,
                mapSerialization = FLOW_MAP,
                listSerialization = FLOW_SEQUENCE,
                classSerialization = FLOW_MAP
            )
        )
    }
}


//// region Setting.value primitives CODEGEN ////

// TODO: 2020/6/19 CODEGEN

fun Setting.value(default: Int): SerializerAwareValue<Int> = valueImpl(default)

//// endregion Setting.value primitives CODEGEN ////


/**
 * Creates a [Value] with reified type.
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@LowPriorityInOverloadResolution
@MiraiExperimentalAPI
@OptIn(ExperimentalStdlibApi::class) // stable in 1.4
inline fun <reified T> Setting.valueReified(default: T): SerializableValue<T> = valueFromKTypeImpl(typeOf<T>()).cast()

@MiraiExperimentalAPI
fun <T> Setting.valueFromKType(type: KType): SerializableValue<T> = valueFromKTypeImpl(type).cast()