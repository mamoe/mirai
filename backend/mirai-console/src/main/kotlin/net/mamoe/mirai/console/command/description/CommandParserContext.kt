/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.description.CommandParserContext.ParserPair
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


/**
 * [KClass] 到 [CommandArgParser] 的匹配
 * @see CustomCommandParserContext 自定义
 */
interface CommandParserContext {
    data class ParserPair<T : Any>(
        val klass: KClass<T>,
        val parser: CommandArgParser<T>
    )

    operator fun <T : Any> get(klass: KClass<out T>): CommandArgParser<T>?

    fun toList(): List<ParserPair<*>>

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
        Friend::class with ExistFriendArgParser
    })
}

object EmptyCommandParserContext : CommandParserContext by CustomCommandParserContext(listOf())

/**
 * 合并两个 [CommandParserContext], [replacer] 将会替换 [this] 中重复的 parser.
 */
operator fun CommandParserContext.plus(replacer: CommandParserContext): CommandParserContext {
    if (replacer == EmptyCommandParserContext) return this
    if (this == EmptyCommandParserContext) return replacer
    return object : CommandParserContext {
        override fun <T : Any> get(klass: KClass<out T>): CommandArgParser<T>? = replacer[klass] ?: this@plus[klass]
        override fun toList(): List<ParserPair<*>> = replacer.toList() + this@plus.toList()
    }
}

/**
 * 合并 [this] 与 [replacer], [replacer] 将会替换 [this] 中重复的 parser.
 */
operator fun CommandParserContext.plus(replacer: List<ParserPair<*>>): CommandParserContext {
    if (replacer.isEmpty()) return this
    if (this == EmptyCommandParserContext) return CustomCommandParserContext(replacer)
    return object : CommandParserContext {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> get(klass: KClass<out T>): CommandArgParser<T>? =
            replacer.firstOrNull { klass.isSubclassOf(it.klass) }?.parser as CommandArgParser<T>? ?: this@plus[klass]

        override fun toList(): List<ParserPair<*>> = replacer.toList() + this@plus.toList()
    }
}

@Suppress("UNCHECKED_CAST")
open class CustomCommandParserContext(val list: List<ParserPair<*>>) : CommandParserContext {

    override fun <T : Any> get(klass: KClass<out T>): CommandArgParser<T>? =
        this.list.firstOrNull { klass.isSubclassOf(it.klass) }?.parser as CommandArgParser<T>?

    override fun toList(): List<ParserPair<*>> {
        return list
    }
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
    return CustomCommandParserContext(CommandParserContextBuilder().apply(block).distinctByReversed { it.klass })
}

/**
 * @see CommandParserContext
 */
class CommandParserContextBuilder : MutableList<ParserPair<*>> by mutableListOf() {
    @JvmName("add")
    inline infix fun <T : Any> KClass<T>.with(parser: CommandArgParser<T>): ParserPair<*> =
        ParserPair(this, parser).also { add(it) }

    inline infix fun <reified T : Any> auto(parser: CommandArgParser<T>): ParserPair<*> =
        ParserPair(T::class, parser).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String, sender: CommandSender) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser(parser)).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgParser<T>.(s: String) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser { s: String, _: CommandSender -> parser(s) }).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @MiraiExperimentalAPI
    @JvmSynthetic
    inline infix fun <reified T : Any> auto(
        crossinline parser: CommandArgParser<*>.(s: String) -> T
    ): ParserPair<*> = T::class with CommandArgParser { s: String, _: CommandSender -> parser(s) }

    /**
     * 添加一个指令解析器
     */
    @MiraiExperimentalAPI
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    inline infix fun <reified T : Any> auto(
        crossinline parser: CommandArgParser<*>.(s: String, sender: CommandSender) -> T
    ): ParserPair<*> = T::class with CommandArgParser(parser)
}


@PublishedApi
internal inline fun <T, K> List<T>.distinctByReversed(selector: (T) -> K): List<T> {
    val set = HashSet<K>()
    val list = ArrayList<T>()
    for (i in this.indices.reversed()) {
        val element = this[i]
        if (set.add(element.let(selector))) {
            list.add(element)
        }
    }
    return list
}