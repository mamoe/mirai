/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command.resolve

import net.mamoe.mirai.console.command.CommandExecuteResult
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.safeCast
import org.jetbrains.annotations.Contract
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@ExperimentalCommandDescriptors
public class CommandResolveResult private constructor(
    internal val value: Any?,
) {
    @get:Contract(pure = true)
    public val call: ResolvedCommandCall?
        get() = value.safeCast()

    @get:Contract(pure = true)
    public val failure: CommandExecuteResult.Failure?
        get() = value.safeCast()

    public constructor(call: ResolvedCommandCall) : this(call as Any?)
    public constructor(failure: CommandExecuteResult.Failure) : this(failure as Any)
}

@ExperimentalCommandDescriptors
public inline fun <R> CommandResolveResult.fold(
    onSuccess: (ResolvedCommandCall?) -> R,
    onFailure: (CommandExecuteResult.Failure) -> R,
): R {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    failure?.let(onFailure)?.let { return it }
    return call.let(onSuccess)
}


@ExperimentalCommandDescriptors
public inline fun CommandResolveResult.getOrElse(
    onFailure: (CommandExecuteResult.Failure) -> ResolvedCommandCall?,
): ResolvedCommandCall {
    contract {
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    failure?.let(onFailure)?.let { return it }
    return call!!
}

/**
 * The resolver converting a [CommandCall] into [ResolvedCommandCall] based on registered []
 *
 * @see CommandCallResolverProvider The provider to instances of this class
 * @see BuiltInCommandCallResolver The builtin implementation
 */
@ExperimentalCommandDescriptors
public interface CommandCallResolver {
    public fun resolve(call: CommandCall): CommandResolveResult

    public companion object {
        @JvmName("resolveCall")
        @ConsoleExperimentalApi
        @ExperimentalCommandDescriptors
        public fun CommandCall.resolve(): CommandResolveResult {
            return GlobalComponentStorage.getExtensions(CommandCallResolverProvider).first()
                .extension.instance
                .resolve(this@resolve)
        }
    }
}