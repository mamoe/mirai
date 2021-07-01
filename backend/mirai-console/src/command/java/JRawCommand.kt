/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.COMMAND_NAME
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.RESTRICTED_CONSOLE_COMMAND_OWNER
import net.mamoe.mirai.console.internal.command.findOrCreateCommandPermission
import net.mamoe.mirai.console.permission.Permission

/**
 * 供 Java 用户继承
 *
 * 请在构造时设置相关属性.
 *
 * ```java
 * public final class MyCommand extends JRawCommand {
 *     public static final MyCommand INSTANCE = new MyCommand();
 *     private MyCommand() {
 *         super(MyPluginMain.INSTANCE, "test")
 *         // 可选设置如下属性
 *         setUsage("/test")
 *         setDescription("这是一个测试指令")
 *         setPermission(CommandPermission.Operator.INSTANCE)
 *         setPrefixOptional(true)
 *     }
 *
 *     @Override
 *     public void onCommand(@NotNull CommandSender sender, @NotNull args: Object[]) {
 *         // 处理指令
 *     }
 * }
 * ```
 *
 * @see JRawCommand
 */
public abstract class JRawCommand
@JvmOverloads constructor(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    @ResolveContext(RESTRICTED_CONSOLE_COMMAND_OWNER)
    public override val owner: CommandOwner,
    @ResolveContext(COMMAND_NAME)
    public override val primaryName: String,
    @ResolveContext(COMMAND_NAME)
    public override vararg val secondaryNames: String,
    parentPermission: Permission = owner.parentPermission,
) : Command {
    /** 用法说明, 用于发送给用户 */
    public override var usage: String = "<no usages given>"
        protected set

    /** 指令描述, 用于显示在 [BuiltInCommands.HelpCommand] */
    public final override var description: String = "<no descriptions given>"
        protected set

    /** 指令权限 */
    public final override var permission: Permission = findOrCreateCommandPermission(parentPermission)
        protected set

    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    @ExperimentalCommandDescriptors
    public final override var prefixOptional: Boolean = false
        protected set
}