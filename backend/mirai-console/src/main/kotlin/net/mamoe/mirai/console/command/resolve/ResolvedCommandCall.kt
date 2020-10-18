/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.descriptor.CommandSignatureVariant
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.command.parse.mapToType
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * The resolved [CommandCall].
 *
 * @see ResolvedCommandCallImpl
 */
@ExperimentalCommandDescriptors
public interface ResolvedCommandCall {
    public val caller: CommandSender

    /**
     * The callee [Command]
     */
    public val callee: Command

    /**
     * The callee [CommandSignatureVariant], specifically a sub command from [CompositeCommand]
     */
    public val calleeSignature: CommandSignatureVariant

    /**
     * Original arguments
     */
    public val rawValueArguments: List<CommandValueArgument>

    /**
     * Resolved value arguments arranged mapping the [CommandSignatureVariant.valueParameters] by index.
     */
    @ConsoleExperimentalApi
    public val resolvedValueArguments: List<Any?>

    public companion object {
        @JvmStatic
        @ExperimentalCommandDescriptors
        public suspend fun ResolvedCommandCall.call() {
            return this.calleeSignature.call(this)
        }
    }
}

@ExperimentalCommandDescriptors
public class ResolvedCommandCallImpl(
    override val caller: CommandSender,
    override val callee: Command,
    override val calleeSignature: CommandSignatureVariant,
    override val rawValueArguments: List<CommandValueArgument>,
) : ResolvedCommandCall {
    override val resolvedValueArguments: List<Any?> by lazy(PUBLICATION) {
        calleeSignature.valueParameters.zip(rawValueArguments).map { (parameter, argument) ->
            argument.mapToType(parameter.type)
            // TODO: 2020/10/17 consider vararg and optional
        }
    }
}