/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.java

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * 为 Java 用户添加协程帮助的 [Command].
 *
 * 注意, [JSimpleCommand], [JCompositeCommand], [JRawCommand] 都不实现这个接口. [JCommand] 只设计为 Java 使用者自己实现 [Command] 相关内容.
 *
 * @see Command
 */
@ConsoleExperimentalApi("Not yet supported")
public interface JCommand : Command {
    // TODO: 2020/10/18 JCommand
}