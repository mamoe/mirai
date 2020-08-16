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

import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.internal.command.AbstractReflectionCommand
import net.mamoe.mirai.console.internal.command.CompositeCommandSubCommandAnnotationResolver
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.reflect.KClass

/**
 * 复合指令.
 */
@ConsoleExperimentalAPI
public abstract class CompositeCommand @JvmOverloads constructor(
    owner: CommandOwner,
    vararg names: String,
    description: String = "no description available",
    permission: CommandPermission = CommandPermission.Default,
    prefixOptional: Boolean = false,
    overrideContext: CommandArgumentContext = EmptyCommandArgumentContext
) : Command, AbstractReflectionCommand(owner, names, description, permission, prefixOptional),
    CommandArgumentContextAware {
    /**
     * [CommandArgumentParser] 的环境
     */
    public final override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

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

    public final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        matchSubCommand(args)?.parseAndExecute(this, args, true) ?: kotlin.run {
            defaultSubCommand.onCommand(this, args)
        }
    }


    internal override suspend fun CommandSender.onDefault(rawArgs: Array<out Any>) {
        sendMessage(usage)
    }

    internal final override val subCommandAnnotationResolver: SubCommandAnnotationResolver
        get() = CompositeCommandSubCommandAnnotationResolver
}
