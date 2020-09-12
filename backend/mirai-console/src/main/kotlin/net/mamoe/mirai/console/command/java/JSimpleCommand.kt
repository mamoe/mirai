/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.description.CommandArgumentContext
import net.mamoe.mirai.console.permission.Permission

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
    vararg names: String,
    basePermission: Permission,
) : SimpleCommand(owner, *names, parentPermission = basePermission) {
    public override var description: String = super.description
        protected set

    public override var permission: Permission = super.permission
        protected set
    public override var prefixOptional: Boolean = super.prefixOptional
        protected set
    public override var context: CommandArgumentContext = super.context
        protected set
}