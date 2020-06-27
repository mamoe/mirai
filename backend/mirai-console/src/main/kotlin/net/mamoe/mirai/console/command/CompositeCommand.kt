/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPOSED_SUPER_CLASS",
    "NOTHING_TO_INLINE",
    "unused",
    "WRONG_MODIFIER_TARGET",
    "WRONG_MODIFIER_CONTAINING_DECLARATION"
)

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.command.internal.AbstractReflectionCommand
import net.mamoe.mirai.console.command.internal.CompositeCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.utils.ConsoleExperimentalAPI
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * 复合指令.
 */
@ConsoleExperimentalAPI
abstract class CompositeCommand @JvmOverloads constructor(
    owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false,
    overrideContext: CommandParserContext = EmptyCommandParserContext
) : Command, AbstractReflectionCommand(owner, names, description, permission, prefixOptional),
    CommandParserContextAware {
    /**
     * [CommandArgParser] 的环境
     */
    final override val context: CommandParserContext = CommandParserContext.Builtins + overrideContext

    /**
     * 标记一个函数为子指令, 当 [value] 为空时使用函数名.
     * @param value 子指令名
     */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class SubCommand(vararg val value: String)

    /** 指定子指令要求的权限 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class Permission(val value: KClass<out CommandPermission>)

    /** 指令描述 */
    @Retention(RUNTIME)
    @Target(FUNCTION)
    protected annotation class Description(val value: String)

    /** 参数名, 将参与构成 [usage] */
    @Retention(RUNTIME)
    @Target(AnnotationTarget.VALUE_PARAMETER)
    protected annotation class Name(val value: String)

    override suspend fun CommandSender.onDefault(rawArgs: Array<out Any>) {
        sendMessage(usage)
    }

    final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        matchSubCommand(args)?.parseAndExecute(this, args) ?: kotlin.run {
            defaultSubCommand.onCommand(this, args)
        }
    }

    final override val subCommandAnnotationResolver: SubCommandAnnotationResolver
        get() = CompositeCommandSubCommandAnnotationResolver
}
