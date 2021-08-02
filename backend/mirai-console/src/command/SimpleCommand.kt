/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.java.JSimpleCommand
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.CommandReflector
import net.mamoe.mirai.console.internal.command.IllegalCommandDeclarationException
import net.mamoe.mirai.console.internal.command.SimpleCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * 简单的, 支持参数自动解析的指令.
 *
 * 要查看指令解析流程, 参考 [CommandManager.executeCommand].
 * 要查看参数解析方式, 参考 [CommandValueArgumentParser].
 *
 * Java 示例查看 [JSimpleCommand].
 *
 * Kotlin 示例:
 * ```
 * object MySimpleCommand : SimpleCommand(
 *     MyPlugin, "tell",
 *     description = "Message somebody"
 * ) {
 *     @Handler
 *     suspend fun CommandSender.onCommand(target: User, message: String) {
 *         target.sendMessage(message)
 *     }
 * }
 * ```
 *
 * @see JSimpleCommand Java 实现
 * @see [CommandManager.executeCommand]
 */
public abstract class SimpleCommand(
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER) owner: CommandOwner,
    @ResolveContext(COMMAND_NAME) primaryName: String,
    @ResolveContext(COMMAND_NAME) vararg secondaryNames: String,
    description: String = "no description available",
    parentPermission: Permission = owner.parentPermission,
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext,
) : Command, AbstractCommand(owner, primaryName, secondaryNames = secondaryNames, description, parentPermission),
    CommandArgumentContextAware {

    private val reflector by lazy { CommandReflector(this, SimpleCommandSubCommandAnnotationResolver) }

    @ExperimentalCommandDescriptors
    public final override val overloads: List<@JvmWildcard CommandSignatureFromKFunction> by lazy {
        reflector.findSubCommands().also {
            reflector.validate(it)
            if (it.isEmpty())
                throw IllegalCommandDeclarationException(
                    this,
                    "SimpleCommand must have at least one subcommand, whereas zero present."
                )
        }
    }

    /**
     * 自动根据带有 [Handler] 注解的函数签名生成 [usage]. 也可以被覆盖.
     */
    public override val usage: String by lazy {
        @OptIn(ExperimentalCommandDescriptors::class)
        reflector.generateUsage(overloads)
    }

    /**
     * 标注指令处理器
     */
    @Target(FUNCTION)
    protected annotation class Handler

    /** 参数名, 将参与构成 [usage] */
    @ConsoleExperimentalApi("Classname might change")
    @Target(VALUE_PARAMETER)
    protected annotation class Name(val value: String)

    /**
     * 指令参数环境. 默认为 [CommandArgumentContext.Builtins] `+` `overrideContext`
     */
    public override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext
}

