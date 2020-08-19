/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.command.description

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.description.CommandArgumentContext.ParserPair
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.internal.LowPriorityInOverloadResolution
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


/**
 * [CommandArgumentParser] 的集合, 用于 [CompositeCommand] 和 [SimpleCommand].
 *
 * @see SimpleCommandArgumentContext 简单实现
 * @see EmptyCommandArgumentContext 空实现, 类似 [emptyList]
 * @see CommandArgumentContext.EMPTY 空实现的另一种获取方式.
 *
 * @see CommandArgumentContext.Builtins 内建 [CommandArgumentParser]
 *
 * @see CommandArgumentContext DSL
 */
public interface CommandArgumentContext {
    /**
     * [KClass] 到 [CommandArgumentParser] 的匹配
     */
    public data class ParserPair<T : Any>(
        val klass: KClass<T>,
        val parser: CommandArgumentParser<T>
    )

    public operator fun <T : Any> get(klass: KClass<out T>): CommandArgumentParser<T>?

    public fun toList(): List<ParserPair<*>>

    public companion object {
        /**
         * For Java callers.
         *
         * @see [EmptyCommandArgumentContext]
         */
        @JvmStatic
        public val EMPTY: CommandArgumentContext = EmptyCommandArgumentContext
    }

    /**
     * 内建的默认 [CommandArgumentParser]
     */
    public object Builtins : CommandArgumentContext by (CommandArgumentContext {
        Int::class with IntArgumentParser
        Byte::class with ByteArgumentParser
        Short::class with ShortArgumentParser
        Boolean::class with BooleanArgumentParser
        String::class with StringArgumentParser
        Long::class with LongArgumentParser
        Double::class with DoubleArgumentParser
        Float::class with FloatArgumentParser

        Member::class with ExistMemberArgumentParser
        Group::class with ExistGroupArgumentParser
        Bot::class with ExistBotArgumentParser
        Friend::class with ExistFriendArgumentParser
    })
}

/**
 * 拥有 [CommandArgumentContext] 的类
 *
 * @see SimpleCommand
 * @see CompositeCommand
 */
public interface CommandArgumentContextAware {
    /**
     * [CommandArgumentParser] 的集合
     */
    public val context: CommandArgumentContext
}

public object EmptyCommandArgumentContext : CommandArgumentContext by SimpleCommandArgumentContext(listOf())

/**
 * 合并两个 [CommandArgumentContext], [replacer] 将会替换 [this] 中重复的 parser.
 */
public operator fun CommandArgumentContext.plus(replacer: CommandArgumentContext): CommandArgumentContext {
    if (replacer == EmptyCommandArgumentContext) return this
    if (this == EmptyCommandArgumentContext) return replacer
    return object : CommandArgumentContext {
        override fun <T : Any> get(klass: KClass<out T>): CommandArgumentParser<T>? =
            replacer[klass] ?: this@plus[klass]

        override fun toList(): List<ParserPair<*>> = replacer.toList() + this@plus.toList()
    }
}

/**
 * 合并 [this] 与 [replacer], [replacer] 将会替换 [this] 中重复的 parser.
 */
public operator fun CommandArgumentContext.plus(replacer: List<ParserPair<*>>): CommandArgumentContext {
    if (replacer.isEmpty()) return this
    if (this == EmptyCommandArgumentContext) return SimpleCommandArgumentContext(replacer)
    return object : CommandArgumentContext {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> get(klass: KClass<out T>): CommandArgumentParser<T>? =
            replacer.firstOrNull { klass.isSubclassOf(it.klass) }?.parser as CommandArgumentParser<T>?
                ?: this@plus[klass]

        override fun toList(): List<ParserPair<*>> = replacer.toList() + this@plus.toList()
    }
}

/**
 * 自定义 [CommandArgumentContext]
 *
 * @see CommandArgumentContext
 */
@Suppress("UNCHECKED_CAST")
public class SimpleCommandArgumentContext(
    public val list: List<ParserPair<*>>
) : CommandArgumentContext {
    override fun <T : Any> get(klass: KClass<out T>): CommandArgumentParser<T>? =
        this.list.firstOrNull { klass.isSubclassOf(it.klass) }?.parser as CommandArgumentParser<T>?

    override fun toList(): List<ParserPair<*>> = list
}

/**
 * 构建一个 [CommandArgumentContext].
 *
 * ```
 * CommandArgumentContext {
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
 *
 * @see CommandArgumentContextBuilder
 * @see CommandArgumentContext
 */
@Suppress("FunctionName")
@JvmSynthetic
public inline fun CommandArgumentContext(block: CommandArgumentContextBuilder.() -> Unit): CommandArgumentContext {
    return SimpleCommandArgumentContext(CommandArgumentContextBuilder().apply(block).distinctByReversed { it.klass })
}

/**
 * @see CommandArgumentContext
 */
public class CommandArgumentContextBuilder : MutableList<ParserPair<*>> by mutableListOf() {
    @JvmName("add") // TODO: 2020/8/19 java class support
    public inline infix fun <T : Any> KClass<T>.with(parser: CommandArgumentParser<T>): ParserPair<*> =
        ParserPair(this, parser).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    public inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgumentParser<T>.(s: String, sender: CommandSender) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser(parser)).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @JvmSynthetic
    public inline infix fun <T : Any> KClass<T>.with(
        crossinline parser: CommandArgumentParser<T>.(s: String) -> T
    ): ParserPair<*> = ParserPair(this, CommandArgParser { s: String, _: CommandSender -> parser(s) }).also { add(it) }

    @JvmSynthetic
    public inline fun <reified T : Any> add(parser: CommandArgumentParser<T>): ParserPair<*> =
        ParserPair(T::class, parser).also { add(it) }

    /**
     * 添加一个指令解析器
     */
    @ConsoleExperimentalAPI
    @JvmSynthetic
    public inline infix fun <reified T : Any> add(
        crossinline parser: CommandArgumentParser<*>.(s: String) -> T
    ): ParserPair<*> = T::class with CommandArgParser { s: String, _: CommandSender -> parser(s) }

    /**
     * 添加一个指令解析器
     */
    @ConsoleExperimentalAPI
    @JvmSynthetic
    @LowPriorityInOverloadResolution
    public inline infix fun <reified T : Any> add(
        crossinline parser: CommandArgumentParser<*>.(s: String, sender: CommandSender) -> T
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