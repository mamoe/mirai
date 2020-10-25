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
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser.Companion.parse
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.command.parse.mapToTypeOrNull
import net.mamoe.mirai.console.internal.data.classifierAsKClass
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.cast
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
     *
     * **Implementation details**: Lazy calculation.
     */
    @ConsoleExperimentalApi
    public val resolvedValueArguments: List<ResolvedCommandValueArgument<*>>

    public companion object
}

@ExperimentalCommandDescriptors
public data class ResolvedCommandValueArgument<T>(
    val parameter: CommandValueParameter<T>,
    val value: T,
)

// Don't move into companion, compilation error
@ExperimentalCommandDescriptors
public suspend inline fun ResolvedCommandCall.call() {
    return this@call.calleeSignature.call(this@call)
}

@ExperimentalCommandDescriptors
public class ResolvedCommandCallImpl(
    override val caller: CommandSender,
    override val callee: Command,
    override val calleeSignature: CommandSignatureVariant,
    override val rawValueArguments: List<CommandValueArgument>,
    private val context: CommandArgumentContext,
) : ResolvedCommandCall {
    override val resolvedValueArguments: List<ResolvedCommandValueArgument<*>> by lazy(PUBLICATION) {
        calleeSignature.valueParameters.zip(rawValueArguments).map { (parameter, argument) ->
            val value = argument.mapToTypeOrNull(parameter.type) ?: context[parameter.type.classifierAsKClass()]?.parse(argument.value, caller)
            ?: throw  NoValueArgumentMappingException(argument, parameter.type)
            // TODO: 2020/10/17 consider vararg and optional
            ResolvedCommandValueArgument(parameter.cast(), value)
        }
    }
}