/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ExperimentalStdlibApi::class)

package net.mamoe.mirai.console.command.parse

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.resolve.CommandCallResolver
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.message.data.MessageChain

/**
 * Unresolved [CommandCall].
 *
 * ### Implementation details
 * [CommandCall] should be _immutable_,
 * meaning all of its properties must be *pure* and should be implemented as an immutable property, or delegated by a lazy initializer.
 *
 * @see CommandCallParser
 * @see CommandCallResolver
 *
 * @see ResolvedCommandCall
 */
@ExperimentalCommandDescriptors
public interface CommandCall {
    /**
     * The [CommandSender] responsible to this call.
     */
    public val caller: CommandSender

    /**
     * One of callee [Command]'s [Command.allNames].
     *
     * Generally [CommandCallResolver] use [calleeName] to find target [Command] registered in [CommandManager]
     */
    public val calleeName: String

    /**
     * Explicit value arguments parsed from raw [MessageChain] or implicit ones deduced by the [CommandCallResolver].
     */
    public val valueArguments: List<CommandValueArgument>

    // maybe add contextual arguments, i.e. from MessageMetadata
}

@ExperimentalCommandDescriptors
public class CommandCallImpl(
    override val caller: CommandSender,
    override val calleeName: String,
    override val valueArguments: List<CommandValueArgument>,
) : CommandCall