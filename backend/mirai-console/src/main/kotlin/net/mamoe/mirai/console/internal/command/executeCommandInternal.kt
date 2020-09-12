/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain

@JvmSynthetic
@Throws(CommandExecutionException::class)
internal suspend fun CommandSender.executeCommandInternal(
    command: Command,
    args: MessageChain,
    commandName: String,
    checkPermission: Boolean,
): CommandExecuteResult {
    if (checkPermission && !command.permission.testPermission(this)) {
        return CommandExecuteResult.PermissionDenied(command, commandName)
    }

    kotlin.runCatching {
        command.onCommand(this, args)
    }.fold(
        onSuccess = {
            return CommandExecuteResult.Success(
                commandName = commandName,
                command = command,
                args = args
            )
        },
        onFailure = {
            return CommandExecuteResult.ExecutionFailed(
                commandName = commandName,
                command = command,
                exception = it,
                args = args
            )
        }
    )
}


@JvmSynthetic
internal suspend fun CommandSender.executeCommandInternal(
    messages: Any,
    commandName: String,
    checkPermission: Boolean,
): CommandExecuteResult {
    val command =
        CommandManagerImpl.matchCommand(commandName) ?: return CommandExecuteResult.CommandNotFound(commandName)
    val args = messages.flattenCommandComponents()

    return executeCommandInternal(command, args.drop(1).asMessageChain(), commandName, checkPermission)
}
