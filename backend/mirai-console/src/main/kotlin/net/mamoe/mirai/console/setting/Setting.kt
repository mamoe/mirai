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

import net.mamoe.mirai.console.setting.internal.cast
import net.mamoe.mirai.console.setting.internal.valueFromKTypeImpl
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf


// Shows public APIs such as deciding when to auto-save.
abstract class Setting : SettingImpl()

/**
 * Internal implementation for [Setting] including:
 * - Reflection on Kotlin properties and Java fields
 * - Auto-saving
 */
// TODO move to internal package.
internal abstract class SettingImpl {
    private class Node<T>(
        val property: KProperty<T>,
        val value: Value<T>,
        val serializer: ValueSerializer<T>
    )

    private val valueNodes: List<Node<*>> = kotlin.run {
        TODO("reflection")
    }
}


//// region Setting.value primitives CODEGEN START ////

// TODO: 2020/6/19 CODEGEN

fun Setting.value(value: Int): IntValue = TODO("codegen")

//// endregion Setting.value primitives CODEGEN END ////


/**
 * Creates a [Value] with [default].
 *
 * @param T reified param type T.
 * Supports only primitives, Kotlin built-in collections,
 * and classes that are serializable with Kotlinx.serialization
 * (typically annotated with [kotlinx.serialization.Serializable])
 */
@LowPriorityInOverloadResolution
@OptIn(ExperimentalStdlibApi::class) // stable in 1.4
inline fun <reified T> Setting.value(default: T): Value<T> = valueFromKTypeImpl(typeOf<T>()).cast()