/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.permission.Permission

/**
 * Java 实现:
 * ```java
 * public final class MySimpleCommand extends JSimpleCommand {
 *     public static final MySimpleCommand INSTANCE = new MySimpleCommand();
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
public abstract class JSimpleCommand @JvmOverloads constructor(
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER) owner: CommandOwner,
    @ResolveContext(COMMAND_NAME) primaryName: String,
    @ResolveContext(COMMAND_NAME) vararg secondaryNames: String,
    basePermission: Permission = owner.parentPermission,
) : SimpleCommand(owner, primaryName, secondaryNames = secondaryNames, parentPermission = basePermission) {
    public override var description: String = super.description
        protected set
    public override var permission: Permission = super.permission
        protected set

    @ExperimentalCommandDescriptors
    public override var prefixOptional: Boolean = super.prefixOptional
        protected set
    public override var context: CommandArgumentContext = super.context
        protected set
}