/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.content
import kotlin.contracts.contract

/**
 * 指令参数解析器.
 *
 * @see CommandArgumentContext
 */
public interface CommandArgumentParser<out T : Any> {
    public fun parse(raw: String, sender: CommandSender): T

    @JvmDefault
    public fun parse(raw: SingleMessage, sender: CommandSender): T = parse(raw.content, sender)
}

public fun <T : Any> CommandArgumentParser<T>.parse(raw: Any, sender: CommandSender): T {
    contract {
        returns() implies (raw is String || raw is SingleMessage)
    }

    return when (raw) {
        is String -> parse(raw, sender)
        is SingleMessage -> parse(raw, sender)
        else -> throw IllegalArgumentException("Illegal raw argument type: ${raw::class.qualifiedName}")
    }
}

@Suppress("unused")
@JvmSynthetic
public inline fun CommandArgumentParser<*>.illegalArgument(message: String, cause: Throwable? = null): Nothing {
    throw ParserException(message, cause)
}

@JvmSynthetic
public inline fun CommandArgumentParser<*>.checkArgument(
    condition: Boolean,
    crossinline message: () -> String = { "Check failed." }
) {
    contract {
        returns() implies condition
    }
    if (!condition) illegalArgument(message())
}

/**
 * 创建匿名 [CommandArgumentParser]
 */
@Suppress("FunctionName")
@JvmSynthetic
public inline fun <T : Any> CommandArgParser(
    crossinline stringParser: CommandArgumentParser<T>.(s: String, sender: CommandSender) -> T
): CommandArgumentParser<T> = object : CommandArgumentParser<T> {
    override fun parse(raw: String, sender: CommandSender): T = stringParser(raw, sender)
}

/**
 * 创建匿名 [CommandArgumentParser]
 */
@Suppress("FunctionName")
@JvmSynthetic
public inline fun <T : Any> CommandArgParser(
    crossinline stringParser: CommandArgumentParser<T>.(s: String, sender: CommandSender) -> T,
    crossinline messageParser: CommandArgumentParser<T>.(m: SingleMessage, sender: CommandSender) -> T
): CommandArgumentParser<T> = object : CommandArgumentParser<T> {
    override fun parse(raw: String, sender: CommandSender): T = stringParser(raw, sender)
    override fun parse(raw: SingleMessage, sender: CommandSender): T = messageParser(raw, sender)
}


/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范.
 */
public class ParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)