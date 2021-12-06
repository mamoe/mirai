/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.command.resolve.InterceptedReason
import net.mamoe.mirai.console.command.resolve.ResolvedCommandCall
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.contracts.contract

/**
 * 指令的执行返回
 */
@ExperimentalCommandDescriptors
public sealed class CommandExecuteResult {
    /** 指令执行时发生的错误 (如果有) */
    public abstract val exception: Throwable?

    /** 尝试执行的指令 (如果匹配到) */
    public abstract val command: Command?

    /** 解析的 [CommandCall] (如果匹配到) */
    public abstract val call: CommandCall?

    /** 解析的 [ResolvedCommandCall] (如果匹配到) */
    public abstract val resolvedCall: ResolvedCommandCall?

    // abstract val to allow smart casting

    /** 指令执行成功 */
    public class Success(
        /** 尝试执行的指令 */
        public override val command: Command,
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall,
        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall,
    ) : CommandExecuteResult() {
        /** 指令执行时发生的错误, 总是 `null` */
        public override val exception: Nothing? get() = null
    }

    /** 指令执行失败 */
    public abstract class Failure : CommandExecuteResult()

    /** 执行执行时发生了一个非法参数错误 */
    public class IllegalArgument(
        /** 指令执行时发生的错误 */
        public override val exception: IllegalCommandArgumentException,
        /** 尝试执行的指令 */
        public override val command: Command,
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall,
        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall,
    ) : Failure()

    /** 指令方法调用过程出现了错误 */
    public class ExecutionFailed(
        /** 指令执行时发生的错误 */
        public override val exception: Throwable,
        /** 尝试执行的指令 */
        public override val command: Command,
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall,
        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall,
    ) : Failure()

    /** 没有匹配的指令 */
    public class UnresolvedCommand(
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall?,
    ) : Failure() {
        /** 指令执行时发生的错误, 总是 `null` */
        public override val exception: Nothing? get() = null

        /** 尝试执行的指令, 总是 `null` */
        public override val command: Nothing? get() = null

        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall? get() = null
    }

    /** 没有匹配的指令 */
    public class Intercepted(
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall?,
        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall?,
        /** 尝试执行的指令 (如果匹配到) */
        public override val command: Command?,
        /** 拦截原因 */
        public val reason: InterceptedReason,
    ) : Failure() {
        /** 指令执行时发生的错误, 总是 `null` */
        public override val exception: Nothing? get() = null
    }

    /** 权限不足 */
    public class PermissionDenied(
        /** 尝试执行的指令 */
        public override val command: Command,
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall,
        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall,
    ) : Failure() {
        /** 指令执行时发生的错误, 总是 `null` */
        public override val exception: Nothing? get() = null
    }

    /** 没有匹配的指令 */
    public class UnmatchedSignature(
        /** 尝试执行的指令 */
        public override val command: Command,
        /** 解析的 [CommandCall] (如果匹配到) */
        public override val call: CommandCall,
        /** 尝试执行的指令 */
        @ExperimentalCommandDescriptors
        @ConsoleExperimentalApi
        public val failureReasons: List<UnmatchedCommandSignature>,
    ) : Failure() {
        /** 指令执行时发生的错误, 总是 `null` */
        public override val exception: Nothing? get() = null

        /** 解析的 [ResolvedCommandCall] (如果匹配到) */
        public override val resolvedCall: ResolvedCommandCall? get() = null
    }
}

@ExperimentalCommandDescriptors
@ConsoleExperimentalApi
public class UnmatchedCommandSignature(
    public val signature: CommandSignature,
    public val failureReason: FailureReason,
)

@ExperimentalCommandDescriptors
@ConsoleExperimentalApi
public sealed class FailureReason {
    public class InapplicableReceiverArgument(
        public override val parameter: CommandReceiverParameter<*>,
        public val argument: CommandSender,
    ) : InapplicableArgument()

    public class InapplicableValueArgument(
        public override val parameter: CommandValueParameter<*>,
        public val argument: CommandValueArgument,
    ) : InapplicableArgument()

    public abstract class InapplicableArgument : FailureReason() {
        public abstract val parameter: CommandParameter<*>
    }

    public abstract class ArgumentLengthMismatch : FailureReason()

    public data class ResolutionAmbiguity(
        /**
         * Including [self][UnmatchedCommandSignature.signature].
         */
        public val allCandidates: List<CommandSignature>,
    ) : FailureReason()

    public object TooManyArguments : ArgumentLengthMismatch()
    public object NotEnoughArguments : ArgumentLengthMismatch()
}

/**
 * 当 [this] 为 [CommandExecuteResult.Success] 时返回 `true`
 */
@ExperimentalCommandDescriptors
@JvmSynthetic
public fun CommandExecuteResult.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is CommandExecuteResult.Success)
        returns(false) implies (this@isSuccess !is CommandExecuteResult.Success)
    }
    return this is CommandExecuteResult.Success
}

/**
 * 当 [this] 为 [CommandExecuteResult.ExecutionFailed], [CommandExecuteResult.IllegalArgument] , [CommandExecuteResult.UnmatchedSignature] 或 [CommandExecuteResult.UnresolvedCommand] 时返回 `true`
 */
@ExperimentalCommandDescriptors
@JvmSynthetic
public fun CommandExecuteResult.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure !is CommandExecuteResult.Success)
        returns(false) implies (this@isFailure is CommandExecuteResult.Success)
    }
    return this !is CommandExecuteResult.Success
}