/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.execute
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 无参数解析, 接收原生参数的指令.
 *
 * ### 指令执行流程
 * 继 [CommandManager.executeCommand] 所述第 3 步, [RawCommand] 不会对参数做任何解析.
 *
 * @see JRawCommand 供 Java 用户继承.
 *
 * @see SimpleCommand 简单指令
 * @see CompositeCommand 复合指令
 */
public abstract class RawCommand(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    public override val owner: CommandOwner,
    /** 指令名. 需要至少有一个元素. 所有元素都不能带有空格 */
    public override vararg val names: String,
    /** 用法说明, 用于发送给用户 */
    public override val usage: String = "<no usages given>",
    /** 指令描述, 用于显示在 [BuiltInCommands.Help] */
    public override val description: String = "<no descriptions given>",
    /** 指令权限 */
    public override val permission: CommandPermission = CommandPermission.Default,
    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public override val prefixOptional: Boolean = false
) : Command {
    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数. 数组元素类型可能是 [SingleMessage] 或 [String]. 且已经以 ' ' 分割.
     *
     * @see CommandManager.execute 查看更多信息
     */
    public abstract override suspend fun CommandSender.onCommand(args: Array<out Any>)
}


/**
 * 供 Java 用户继承
 *
 * 请在构造时设置相关属性.
 *
 * ```java
 * public final class MyCommand extends JRawCommand {
 *     public static final MyCommand INSTANCE = new MyCommand();
 *     private MyCommand () {
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
 * @see RawCommand
 */
public abstract class JRawCommand(
    /**
     * 指令拥有者.
     * @see CommandOwner
     */
    public override val owner: CommandOwner,
    /** 指令名. 需要至少有一个元素. 所有元素都不能带有空格 */
    public override vararg val names: String
) : Command {
    /** 用法说明, 用于发送给用户 */
    public override var usage: String = "<no usages given>"
        protected set

    /** 指令描述, 用于显示在 [BuiltInCommands.Help] */
    public final override var description: String = "<no descriptions given>"
        protected set

    /** 指令权限 */
    public final override var permission: CommandPermission = CommandPermission.Default
        protected set

    /** 为 `true` 时表示 [指令前缀][CommandManager.commandPrefix] 可选 */
    public final override var prefixOptional: Boolean = false
        protected set

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数. 数组元素类型可能是 [SingleMessage] 或 [String]. 且已经以 ' ' 分割.
     *
     * @see CommandManager.execute 查看更多信息
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("onCommand")
    public abstract fun onCommand(sender: CommandSender, args: Array<out Any>)

    public final override suspend fun CommandSender.onCommand(args: Array<out Any>) {
        withContext(Dispatchers.IO) { onCommand(this@onCommand, args) }
    }
}