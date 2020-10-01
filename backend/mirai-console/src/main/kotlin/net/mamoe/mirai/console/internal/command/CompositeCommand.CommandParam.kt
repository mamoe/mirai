/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.console.internal.command

import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentParser
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

/*
internal fun Parameter.toCommandParam(): CommandParameter<*> {
    val name = getAnnotation(CompositeCommand.Name::class.java)
    return CommandParameter(
        name?.value ?: this.name
        ?: throw IllegalArgumentException("Cannot construct CommandParam from a unnamed param"),
        this.type.kotlin,
        null
    )
}
*/

/**
 * 指令形式参数.
 */
internal data class CommandParameter<T : Any>(
    /**
     * 参数名. 不允许重复.
     */
    val name: String,
    /**
     * 参数类型. 将从 [CompositeCommand.context] 中寻找 [CommandArgumentParser] 解析.
     */
    val type: KClass<T>, // exact type
    val parameter: KParameter, // source parameter
) {
    constructor(name: String, type: KClass<T>, parameter: KParameter, parser: CommandArgumentParser<T>) : this(
        name, type, parameter
    ) {
        this._overrideParser = parser
    }

    @Suppress("PropertyName")
    @JvmField
    internal var _overrideParser: CommandArgumentParser<T>? = null


    /**
     * 覆盖的 [CommandArgumentParser].
     *
     * 如果非 `null`, 将不会从 [CommandArgumentContext] 寻找 [CommandArgumentParser]
     */
    val overrideParser: CommandArgumentParser<T>? get() = _overrideParser
}

