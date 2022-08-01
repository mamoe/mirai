/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.GroupedCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.internal.command.SubCommandReflector
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

public abstract class AbstractSubCommandGroup(
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext,
) : CommandArgumentContextAware, SubCommandGroup {

    private val reflector by lazy { SubCommandReflector(this, GroupedCommandSubCommandAnnotationResolver) }

    @ExperimentalCommandDescriptors
    public final override val overloads: List<CommandSignatureFromKFunction> by lazy {
        reflector.findSubCommands().also {
            reflector.validate(it)
        }
    }

    /**
     * 标记一个属性为子指令集合
     */
    @Retention(RUNTIME)
    @Target(PROPERTY)
    protected annotation class AnotherCombinedCommand(
    )

    /**
     * 标记一个函数为子指令, 当 [value] 为空时使用函数名.
     * @param value 子指令名
     */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class AnotherSubCommand(
        @ResolveContext(COMMAND_NAME) vararg val value: String = [],
    )

    /** 指令描述 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class AnotherDescription(val value: String)

    /** 参数名, 由具体Command决定用途 */
    @ConsoleExperimentalApi("Classname might change")
    @Retention(RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    protected annotation class AnotherName(val value: String)

    /**
     * 智能参数解析环境
     */ // open since 2.12
    public override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

}


