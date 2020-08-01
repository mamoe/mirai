/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
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
 * this output type of that arg
 * input is always String
 */
public interface CommandArgParser<out T : Any> {
    public fun parse(raw: String, sender: CommandSender): T

    @JvmDefault
    public fun parse(raw: SingleMessage, sender: CommandSender): T = parse(raw.content, sender)
}

public fun <T : Any> CommandArgParser<T>.parse(raw: Any, sender: CommandSender): T {
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
public inline fun CommandArgParser<*>.illegalArgument(message: String, cause: Throwable? = null): Nothing {
    throw ParserException(message, cause)
}

@JvmSynthetic
public inline fun CommandArgParser<*>.checkArgument(
    condition: Boolean,
    crossinline message: () -> String = { "Check failed." }
) {
    contract {
        returns() implies condition
    }
    if (!condition) illegalArgument(message())
}

/**
 * 创建匿名 [CommandArgParser]
 */
@Suppress("FunctionName")
@JvmSynthetic
public inline fun <T : Any> CommandArgParser(
    crossinline stringParser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
): CommandArgParser<T> = object : CommandArgParser<T> {
    override fun parse(raw: String, sender: CommandSender): T = stringParser(raw, sender)
}

/**
 * 创建匿名 [CommandArgParser]
 */
@Suppress("FunctionName")
@JvmSynthetic
public inline fun <T : Any> CommandArgParser(
    crossinline stringParser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T,
    crossinline messageParser: CommandArgParser<T>.(m: SingleMessage, sender: CommandSender) -> T
): CommandArgParser<T> = object : CommandArgParser<T> {
    override fun parse(raw: String, sender: CommandSender): T = stringParser(raw, sender)
    override fun parse(raw: SingleMessage, sender: CommandSender): T = messageParser(raw, sender)
}


/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范.
 */
public class ParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)