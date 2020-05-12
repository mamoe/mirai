@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.GroupContactCommandSender
import net.mamoe.mirai.contact.Group

/**
 * this output type of that arg
 * input is always String
 */
abstract class CommandArgParser<T : Any> {
    abstract fun parse(s: String, sender: CommandSender): T
    protected inline fun parseError(message: String, cause: Throwable? = null): Nothing {
        throw ParserException(message, cause)
    }
}

@Suppress("FunctionName")
inline fun <T : Any> CommandArgParser(
    crossinline parser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
): CommandArgParser<T> {
    return object : CommandArgParser<T>() {
        override fun parse(s: String, sender: CommandSender): T = parser(s, sender)
    }
}

/**
 * 在解析参数时遇到的 _正常_ 错误. 如参数不符合规范.
 */
class ParserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

inline fun Int.Companion.parser(): CommandArgParser<Int> = IntArgParser
inline fun Long.Companion.parser(): CommandArgParser<Long> = LongArgParser
inline fun Byte.Companion.parser(): CommandArgParser<Byte> = ByteArgParser
inline fun Short.Companion.parser(): CommandArgParser<Short> = ShortArgParser
inline fun Float.Companion.parser(): CommandArgParser<Float> = FloatArgParser
inline fun Double.Companion.parser(): CommandArgParser<Double> = DoubleArgParser


object IntArgParser : CommandArgParser<Int>() {
    override fun parse(s: String, sender: CommandSender): Int = s.toIntOrNull() ?: parseError("无法解析 $s 为整数")
}

object LongArgParser : CommandArgParser<Long>() {
    override fun parse(s: String, sender: CommandSender): Long = s.toLongOrNull() ?: parseError("无法解析 $s 为长整数")
}

object ShortArgParser : CommandArgParser<Short>() {
    override fun parse(s: String, sender: CommandSender): Short = s.toShortOrNull() ?: parseError("无法解析 $s 为短整数")
}

object ByteArgParser : CommandArgParser<Byte>() {
    override fun parse(s: String, sender: CommandSender): Byte = s.toByteOrNull() ?: parseError("无法解析 $s 为字节")
}

object DoubleArgParser : CommandArgParser<Double>() {
    override fun parse(s: String, sender: CommandSender): Double =
        s.toDoubleOrNull() ?: parseError("无法解析 $s 为小数")
}

object FloatArgParser : CommandArgParser<Float>() {
    override fun parse(s: String, sender: CommandSender): Float =
        s.toFloatOrNull() ?: parseError("无法解析 $s 为小数")
}

object StringArgParser : CommandArgParser<String>() {
    override fun parse(s: String, sender: CommandSender): String = s
}

/**
 * require a bot that already login in console
 * input: Bot UIN
 * output: Bot
 * errors: String->Int convert, Bot Not Exist
 */
object ExistBotArgParser : CommandArgParser<Bot>() {
    override fun parse(s: String, sender: CommandSender): Bot {
        val uin = s.toLongOrNull() ?: parseError("无法识别机器人账号 $s")
        return try {
            Bot.getInstance(uin)
        } catch (e: NoSuchElementException) {
            error("无法找到 Bot $uin")
        }
    }
}


object ExistGroupArgParser : CommandArgParser<Group>() {
    override fun parse(s: String, sender: CommandSender): Group {
        if ((s == "" || s == "~") && sender is GroupContactCommandSender) {
            return sender.contact as Group
        }

        val code = s.toLongOrNull() ?: parseError("无法识别群号码 $s")
        TODO()
    }
}
