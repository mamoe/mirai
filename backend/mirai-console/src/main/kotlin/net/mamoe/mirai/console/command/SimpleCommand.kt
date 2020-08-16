/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPOSED_SUPER_CLASS",
    "NOTHING_TO_INLINE",
    "unused",
    "WRONG_MODIFIER_TARGET", "CANNOT_WEAKEN_ACCESS_PRIVILEGE",
    "WRONG_MODIFIER_CONTAINING_DECLARATION", "RedundantVisibilityModifier"
)

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.description.CommandArgumentContext
import net.mamoe.mirai.console.command.description.CommandArgumentContextAware
import net.mamoe.mirai.console.command.description.EmptyCommandArgumentContext
import net.mamoe.mirai.console.command.description.plus
import net.mamoe.mirai.console.internal.command.AbstractReflectionCommand
import net.mamoe.mirai.console.internal.command.SimpleCommandSubCommandAnnotationResolver

public abstract class SimpleCommand @JvmOverloads constructor(
    owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false,
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext
) : Command, AbstractReflectionCommand(owner, names, description, permission, prefixOptional),
    CommandArgumentContextAware {

    public override val usage: String
        get() = super.usage

    /**
     * 标注指令处理器
     */
    protected annotation class Handler

    public final override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

    public final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        subCommands.single().parseAndExecute(this, args, false)
    }

    internal override fun checkSubCommand(subCommands: Array<SubCommandDescriptor>) {
        super.checkSubCommand(subCommands)
        check(subCommands.size == 1) { "There can only be exactly one function annotated with Handler at this moment as overloading is not yet supported." }
    }

    @Deprecated("prohibited", level = DeprecationLevel.HIDDEN)
    internal override suspend fun CommandSender.onDefault(rawArgs: Array<out Any>) = sendMessage(usage)

    internal final override val subCommandAnnotationResolver: SubCommandAnnotationResolver
        get() = SimpleCommandSubCommandAnnotationResolver
}