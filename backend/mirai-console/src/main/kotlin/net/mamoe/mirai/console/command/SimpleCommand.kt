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

import net.mamoe.mirai.console.command.description.CommandParserContext
import net.mamoe.mirai.console.command.description.CommandParserContextAware
import net.mamoe.mirai.console.command.description.EmptyCommandParserContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.console.command.internal.AbstractReflectionCommand
import net.mamoe.mirai.console.command.internal.SimpleCommandSubCommandAnnotationResolver

abstract class SimpleCommand @JvmOverloads constructor(
    owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false,
    overrideContext: CommandParserContext = EmptyCommandParserContext
) : Command, AbstractReflectionCommand(owner, names, description, permission, prefixOptional),
    CommandParserContextAware {

    /**
     * 标注指令处理器
     */
    protected annotation class Handler

    final override val context: CommandParserContext = CommandParserContext.Builtins + overrideContext

    override fun checkSubCommand(subCommands: Array<SubCommandDescriptor>) {
        super.checkSubCommand(subCommands)
        check(subCommands.size == 1) { "There can only be exactly one function annotated with Handler at this moment as overloading is not yet supported." }
    }

    @Deprecated("prohibited", level = DeprecationLevel.HIDDEN)
    final override suspend fun CommandSender.onDefault(rawArgs: Array<out Any>) = error("shouldn't be reached")

    final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        subCommands.single().parseAndExecute(this, args, false)
    }

    final override val subCommandAnnotationResolver: SubCommandAnnotationResolver
        get() = SimpleCommandSubCommandAnnotationResolver
}