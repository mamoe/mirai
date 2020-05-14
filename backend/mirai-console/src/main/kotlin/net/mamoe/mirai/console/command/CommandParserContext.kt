/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.AbstractCommandParserContext.ParserPair
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass


/**
 * [KClass] 到 [CommandArgParser] 的匹配
 * @see AbstractCommandParserContext
 */
interface CommandParserContext {
    operator fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>?

    /**
     * 内建的默认 [CommandArgParser]
     */
    object Builtins : CommandParserContext by (CommandParserContext {
        Int::class with IntArgParser
        Byte::class with ByteArgParser
        Short::class with ShortArgParser
        Boolean::class with BooleanArgParser
        String::class with StringArgParser
        Long::class with LongArgParser
        Double::class with DoubleArgParser
        Float::class with FloatArgParser

        Member::class with ExistMemberArgParser
        Group::class with ExistGroupArgParser
        Bot::class with ExistBotArgParser
    })
}

fun <T : Any> CommandParserContext.parserFor(param: CommandParam<T>): CommandArgParser<T>? =
    param.overrideParser ?: this[param.type]

fun <T : Any> CommandDescriptor.parserFor(param: CommandParam<T>): CommandArgParser<T>? =
    param.overrideParser ?: this.context.parserFor(param)

fun <T : Any> Command.parserFor(param: CommandParam<T>): CommandArgParser<T>? =
    param.overrideParser ?: this.descriptor.parserFor(param)

/**
 * 合并两个 [CommandParserContext], [replacer] 将会替换 [this] 中重复的 parser.
 */
operator fun CommandParserContext.plus(replacer: CommandParserContext): CommandParserContext {
    return object : CommandParserContext {
        override fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>? = replacer[klass] ?: this@plus[klass]
    }
}

@Suppress("UNCHECKED_CAST")
open class AbstractCommandParserContext(val list: List<ParserPair<*>>) : CommandParserContext {
    class ParserPair<T : Any>(
        val klass: KClass<T>,
        val parser: CommandArgParser<T>
    )

    override fun <T : Any> get(klass: KClass<T>): CommandArgParser<T>? =
        this.list.firstOrNull { it.klass == klass }?.parser as CommandArgParser<T>?
}

/**
 * 构建一个 [CommandParserContext].
 *
 * ```
 * CommandParserContext {
 *     Int::class with IntArgParser
 *     Member::class with ExistMemberArgParser
 *     Group::class with { s: String, sender: CommandSender ->
 *          Bot.getInstance(s.toLong()).getGroup(s.toLong())
 *     }
 *     Bot::class with { s: String ->
 *          Bot.getInstance(s.toLong())
 *     }
 * }
 * ```
 */
@Suppress("FunctionName")
@JvmSynthetic
inline fun CommandParserContext(block: CommandParserContextBuilder.() -> Unit): CommandParserContext {
    return AbstractCommandParserContext(
        CommandParserContextBuilder().apply(block).distinctByReversed { it.klass })
}

/**
 * @see CommandParserContext
 */
class CommandParserContextBuilder : MutableList<ParserPair<*>> by mutableListOf() {
    @JvmName("add")
    inline infix fun <T : Any> KClass<T>.with(parser: CommandArgParser<T>): ParserPair<*> =
        ParserPair(this, parser)

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser(parser))

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser { s: String, _: CommandSender -> parser(s) })
}


@PublishedApi
internal inline fun <T, K> Iterable<T>.distinctByReversed(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (i in list.indices.reversed()) {
        val element = list[i]
        if (set.add(element.let(selector))) {
            list.add(element)
        }
    }
    return list
}