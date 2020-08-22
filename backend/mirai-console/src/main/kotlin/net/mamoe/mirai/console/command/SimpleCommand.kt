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

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.description.*
import net.mamoe.mirai.console.internal.command.AbstractReflectionCommand
import net.mamoe.mirai.console.internal.command.SimpleCommandSubCommandAnnotationResolver

/**
 * 简单的, 支持参数自动解析的指令.
 *
 * 要查看指令解析流程, 参考 [CommandManager.executeCommand]
 * 要查看参数解析方式, 参考 [CommandArgumentParser]
 *
 * Kotlin 实现:
 * ```
 * object MySimpleCommand : SimpleCommand(
 *     MyPlugin, "tell",
 *     description = "Message somebody",
 *     usage = "/tell <target> <message>"  // usage 如不设置则自动根据带有 @Handler 的方法生成
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

    public override val context: CommandArgumentContext = CommandArgumentContext.Builtins + overrideContext

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

/**
 * Java 实现:
 * ```java
 * public final class MySimpleCommand extends JSimpleCommand {
 *     private MySimpleCommand() {
 *         super(MyPlugin.INSTANCE, "tell")
 *         // 可选设置如下属性
 *         setDescription("这是一个测试指令")
 *         setUsage("/tell <target> <message>") // 如不设置则自动根据带有 @Handler 的方法生成
 *         setPermission(CommandPermission.Operator.INSTANCE)
 *         setPrefixOptional(true)
 *     }
 *
 *     @Handler
 *     public void onCommand(CommandSender sender, User target, String message) {
 *         target.sendMessage(message)
 *     }
 * }
 * ```
 *
 * @see SimpleCommand
 * @see [CommandManager.executeCommand]
 */
public abstract class JSimpleCommand(
    owner: CommandOwner,
    vararg names: String
) : SimpleCommand(owner, *names) {
    public override var description: String = super.description
        protected set
    public override var permission: CommandPermission = super.permission
        protected set
    public override var prefixOptional: Boolean = super.prefixOptional
        protected set
    public override var context: CommandArgumentContext = super.context
        protected set
}