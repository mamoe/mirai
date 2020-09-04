/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.java

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.execute
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

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
    public abstract fun onCommand(sender: CommandSender, args: MessageChain)

    public final override suspend fun CommandSender.onCommand(args: MessageChain) {
        withContext(Dispatchers.IO) { onCommand(this@onCommand, args) }
    }
}