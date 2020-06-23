@file:Suppress("unused")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.console.command.CommandExecuteResult.CommandExecuteStatus
import net.mamoe.mirai.message.data.Message
import kotlin.contracts.contract

/**
 * 指令的执行返回
 *
 * @see CommandExecuteStatus
 */
sealed class CommandExecuteResult {
    /** 指令最终执行状态 */
    abstract val status: CommandExecuteStatus

    /** 指令执行时发生的错误 (如果有) */
    abstract val exception: Throwable?

    /** 尝试执行的指令 (如果匹配到) */
    abstract val command: Command?

    /** 尝试执行的指令名 (如果匹配到) */
    abstract val commandName: String?

    /** 基础分割后的实际参数列表, 元素类型可能为 [Message] 或 [String] */
    abstract val args: Array<out Any>?

    // abstract val to allow smart casting

    /** 指令执行成功 */
    class Success(
        /** 尝试执行的指令 */
        override val command: Command,
        /** 尝试执行的指令名 */
        override val commandName: String,
        /** 基础分割后的实际参数列表, 元素类型可能为 [Message] 或 [String] */
        override val args: Array<out Any>
    ) : CommandExecuteResult() {
        /** 指令执行时发生的错误, 总是 `null` */
        override val exception: Nothing? get() = null

        /** 指令最终执行状态, 总是 [CommandExecuteStatus.SUCCESSFUL] */
        override val status: CommandExecuteStatus get() = CommandExecuteStatus.SUCCESSFUL
    }

    /** 指令执行过程出现了错误 */
    class ExecutionException(
        /** 指令执行时发生的错误 */
        override val exception: Throwable,
        /** 尝试执行的指令 */
        override val command: Command,
        /** 尝试执行的指令名 */
        override val commandName: String,
        /** 基础分割后的实际参数列表, 元素类型可能为 [Message] 或 [String] */
        override val args: Array<out Any>
    ) : CommandExecuteResult() {
        /** 指令最终执行状态, 总是 [CommandExecuteStatus.EXECUTION_EXCEPTION] */
        override val status: CommandExecuteStatus get() = CommandExecuteStatus.EXECUTION_EXCEPTION
    }

    /** 没有匹配的指令 */
    class CommandNotFound(
        /** 尝试执行的指令名 */
        override val commandName: String
    ) : CommandExecuteResult() {
        /** 指令执行时发生的错误, 总是 `null` */
        override val exception: Nothing? get() = null

        /** 尝试执行的指令, 总是 `null` */
        override val command: Nothing? get() = null

        /** 基础分割后的实际参数列表, 元素类型可能为 [Message] 或 [String] */
        override val args: Nothing? get() = null

        /** 指令最终执行状态, 总是 [CommandExecuteStatus.COMMAND_NOT_FOUND] */
        override val status: CommandExecuteStatus get() = CommandExecuteStatus.COMMAND_NOT_FOUND
    }

    /**
     * 指令的执行状态
     */
    enum class CommandExecuteStatus {
        /** 指令执行成功 */
        SUCCESSFUL,

        /** 指令执行过程出现了错误 */
        EXECUTION_EXCEPTION,

        /** 没有匹配的指令 */
        COMMAND_NOT_FOUND
    }
}


@Suppress("RemoveRedundantQualifierName")
typealias CommandExecuteStatus = CommandExecuteResult.CommandExecuteStatus

/**
 * 当 [this] 为 [CommandExecuteResult.Success] 时返回 `true`
 */
@JvmSynthetic
fun CommandExecuteResult.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is CommandExecuteResult.Success)
        returns(false) implies (this@isSuccess !is CommandExecuteResult.Success)
    }
    return this is CommandExecuteResult.Success
}

/**
 * 当 [this] 为 [CommandExecuteResult.ExecutionException] 时返回 `true`
 */
@JvmSynthetic
fun CommandExecuteResult.isExecutionException(): Boolean {
    contract {
        returns(true) implies (this@isExecutionException is CommandExecuteResult.ExecutionException)
        returns(false) implies (this@isExecutionException !is CommandExecuteResult.ExecutionException)
    }
    return this is CommandExecuteResult.ExecutionException
}

/**
 * 当 [this] 为 [CommandExecuteResult.ExecutionException] 时返回 `true`
 */
@JvmSynthetic
fun CommandExecuteResult.isCommandNotFound(): Boolean {
    contract {
        returns(true) implies (this@isCommandNotFound is CommandExecuteResult.CommandNotFound)
        returns(false) implies (this@isCommandNotFound !is CommandExecuteResult.CommandNotFound)
    }
    return this is CommandExecuteResult.CommandNotFound
}

/**
 * 当 [this] 为 [CommandExecuteResult.ExecutionException] 或 [CommandExecuteResult.CommandNotFound] 时返回 `true`
 */
@JvmSynthetic
fun CommandExecuteResult.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure !is CommandExecuteResult.Success)
        returns(false) implies (this@isFailure is CommandExecuteResult.Success)
    }
    return this !is CommandExecuteResult.Success
}