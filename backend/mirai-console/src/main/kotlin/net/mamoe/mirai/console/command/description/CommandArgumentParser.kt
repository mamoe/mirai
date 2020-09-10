/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageContent
import net.mamoe.mirai.message.data.SingleMessage
import net.mamoe.mirai.message.data.content
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 指令参数解析器. 用于解析字符串或 [SingleMessage] 到特定参数类型.
 *
 * ### 参数解析
 *
 * 如 [SimpleCommand] 中的示例:
 * ```
 * suspend fun CommandSender.mute(target: Member, duration: Int)
 * ```
 * [CommandManager] 总是从 [SimpleCommand.context] 搜索一个 [T] 为 [Member] 的 [CommandArgumentParser], 并调用其 [CommandArgumentParser.parse]
 *
 * ### 内建指令解析器
 * - 基础类型: [ByteArgumentParser], [ShortArgumentParser], [IntArgumentParser], [LongArgumentParser]
 * [FloatArgumentParser], [DoubleArgumentParser],
 * [BooleanArgumentParser], [StringArgumentParser]
 *
 * - [Bot]: [ExistingBotArgumentParser]
 * - [Friend]: [ExistingFriendArgumentParser]
 * - [Group]: [ExistingGroupArgumentParser]
 * - [Member]: [ExistingMemberArgumentParser]
 * - [User]: [ExistingUserArgumentParser]
 * - [Contact]: [ExistingContactArgumentParser]
 *
 *
 * @see SimpleCommand 简单指令
 * @see CompositeCommand 复合指令
 *
 * @see buildCommandArgumentContext 指令参数环境, 即 [CommandArgumentParser] 的集合
 */
public interface CommandArgumentParser<out T : Any> {
    /**
     * 解析一个字符串为 [T] 类型参数
     *
     * **实现提示**: 在解析时遇到意料之中的问题, 如无法找到目标群员, 可抛出 [CommandArgumentParserException].
     * 此异常将会被特殊处理, 不会引发一个错误, 而是作为指令调用成功的情况, 将错误信息发送给用户.
     *
     * @throws CommandArgumentParserException 当解析时遇到*意料之中*的问题时抛出.
     *
     * @see CommandArgumentParserException
     */
    @Throws(CommandArgumentParserException::class)
    public fun parse(raw: String, sender: CommandSender): T

    /**
     * 解析一个消息内容元素为 [T] 类型参数
     *
     * **实现提示**: 在解析时遇到意料之中的问题, 如无法找到目标群员, 可抛出 [CommandArgumentParserException].
     * 此异常将会被特殊处理, 不会引发一个错误, 而是作为指令调用成功的情况, 将错误信息发送给用户.
     *
     * @throws CommandArgumentParserException 当解析时遇到*意料之中*的问题时抛出.
     *
     * @see CommandArgumentParserException
     */
    @Throws(CommandArgumentParserException::class)
    @JvmDefault
    public fun parse(raw: MessageContent, sender: CommandSender): T = parse(raw.content, sender)
}

/**
 * 使用原 [this] 解析, 成功后使用 [mapper] 映射为另一个类型.
 */
public fun <T : Any, R : Any> CommandArgumentParser<T>.map(
    mapper: CommandArgumentParser<R>.(T) -> R
): CommandArgumentParser<R> = MappingCommandArgumentParser(this, mapper)

private class MappingCommandArgumentParser<T : Any, R : Any>(
    private val original: CommandArgumentParser<T>,
    private val mapper: CommandArgumentParser<R>.(T) -> R
) : CommandArgumentParser<R> {
    override fun parse(raw: String, sender: CommandSender): R = mapper(original.parse(raw, sender))
    override fun parse(raw: MessageContent, sender: CommandSender): R = mapper(original.parse(raw, sender))
}

/**
 * 解析一个字符串或 [SingleMessage] 为 [T] 类型参数
 *
 * @throws IllegalArgumentException 当 [raw] 既不是 [SingleMessage], 也不是 [String] 时抛出.
 */
@JvmSynthetic
@Throws(IllegalArgumentException::class)
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

/**
 * 抛出一个 [CommandArgumentParserException] 的捷径
 *
 * @throws CommandArgumentParserException
 */
@Suppress("unused")
@JvmSynthetic
@Throws(CommandArgumentParserException::class)
public inline fun CommandArgumentParser<*>.illegalArgument(message: String, cause: Throwable? = null): Nothing {
    throw CommandArgumentParserException(message, cause)
}

/**
 * 检查参数 [condition]. 当它为 `false` 时调用 [message] 并以其返回值作为消息, 抛出异常 [CommandArgumentParserException]
 *
 * @throws CommandArgumentParserException
 */
@Throws(CommandArgumentParserException::class)
@JvmSynthetic
public inline fun CommandArgumentParser<*>.checkArgument(
    condition: Boolean,
    crossinline message: () -> String = { "Check failed." }
) {
    contract {
        returns() implies condition
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }
    if (!condition) illegalArgument(message())
}