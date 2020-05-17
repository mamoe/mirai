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

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

typealias SerialName = kotlinx.serialization.SerialName

/**
 * 配置的基类. 所有配置必须拥有一个无参构造器, 以用于在 [MutableList] 与 [MutableMap] 中动态识别类型
 */
@Suppress("EXPOSED_SUPER_CLASS")
abstract class Setting : AbstractSetting() {
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

    override fun toString(): String = yaml.stringify(this.serializer, this)
}

@Suppress("UNCHECKED_CAST")
val <T : Setting> T.serializer: KSerializer<T>
    get() = kotlinSerializer as KSerializer<T>


fun <T : Setting> Setting.value(default: T): Value<T> {
    require(this::class != default::class) {
        "Recursive nesting is prohibited"
    }
    return valueImpl(default)
}

inline fun <T : Setting> Setting.value(default: T, crossinline initializer: T.() -> Unit): Value<T> =
    value(default).also { it.value.apply(initializer) }

inline fun <reified T : Setting> Setting.value(default: List<T>): SettingListValue<T> = valueImpl(default)

@JvmName("valueMutable")
inline fun <reified T : Setting> Setting.value(default: MutableList<T>): MutableSettingListValue<T> = valueImpl(default)


inline fun <reified T : Setting> Setting.value(default: Set<T>): SettingSetValue<T> = valueImpl(default)

@JvmName("valueMutable")
inline fun <reified T : Setting> Setting.value(default: MutableSet<T>): MutableSettingSetValue<T> = valueImpl(default)
