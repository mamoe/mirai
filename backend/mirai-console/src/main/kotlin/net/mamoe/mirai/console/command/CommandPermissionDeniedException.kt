/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.Command.Companion.primaryName

/**
 * 在 [executeCommand] 中, [CommandSender] 未拥有 [Command.permission] 所要求的权限时抛出的异常.
 *
 * 总是作为 [CommandExecutionException.cause].
 */
public class CommandPermissionDeniedException(
    /**
     * 执行过程发生异常的指令
     */
    public val command: Command
) : RuntimeException("Permission denied while executing command '${command.primaryName}'") {
    public override fun toString(): String =
        "CommandPermissionDeniedException(command=$command)"
}