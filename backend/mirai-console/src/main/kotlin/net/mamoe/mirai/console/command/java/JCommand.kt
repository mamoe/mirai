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
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.SingleMessage

/**
 * 为 Java 用户添加协程帮助的 [Command].
 *
 * 注意, [JSimpleCommand], [JCompositeCommand], [JRawCommand] 都不实现这个接口. [JCommand] 只设计为 Java 使用者自己实现 [Command] 相关内容.
 *
 * @see Command
 */
public interface JCommand : Command {
    public override suspend fun CommandSender.onCommand(args: MessageChain) {
        withContext(Dispatchers.IO) { onCommand(this@onCommand, args) }
    }

    /**
     * 在指令被执行时调用.
     *
     * @param args 指令参数. 数组元素类型可能是 [SingleMessage] 或 [String]. 且已经以 ' ' 分割.
     *
     * @see CommandManager.executeCommand 查看更多信息
     */
    public fun onCommand(sender: CommandSender, args: MessageChain) // overrides bridge
}