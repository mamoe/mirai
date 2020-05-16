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
abstract class CommandArgParser<out T : Any> {
    abstract fun parse(raw: String, sender: CommandSender): T
    open fun parse(raw: SingleMessage, sender: CommandSender): T = parse(raw.content, sender)
}

fun <T : Any> CommandArgParser<T>.parse(raw: Any, sender: CommandSender): T {
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
inline fun CommandArgParser<*>.illegalArgument(message: String, cause: Throwable? = null): Nothing {
    throw ParserException(message, cause)
}

@JvmSynthetic
inline fun CommandArgParser<*>.checkArgument(
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
inline fun <T : Any> CommandArgParser(
    crossinline parser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
): CommandArgParser<T> = object : CommandArgParser<T>() {
    override fun parse(raw: String, sender: CommandSender): T = parser(raw, sender)
}


/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范.
 */
class ParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)