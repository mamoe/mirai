/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.util.semver

import net.mamoe.mirai.console.util.SemVersion
import kotlin.math.max
import kotlin.math.min

internal object RangeTokenReader {
    enum class TokenType {
        STRING,

        /* 左括号 */
        LEFT,

        /* 右括号 */
        RIGHT,

        /* || */
        OR,

        /* && */
        AND,
        GROUP
    }

    sealed class Token {
        abstract val type: TokenType
        abstract val value: String
        abstract val position: Int

        class LeftBracket(override val position: Int) : Token() {
            override val type: TokenType get() = TokenType.LEFT
            override val value: String get() = "{"

            override fun toString(): String = "LB{"
        }

        class RightBracket(override val position: Int) : Token() {
            override val type: TokenType get() = TokenType.RIGHT
            override val value: String get() = "}"

            override fun toString(): String = "RB}"
        }

        class Or(override val position: Int) : Token() {
            override val type: TokenType get() = TokenType.OR
            override val value: String get() = "||"
            override fun toString(): String = "OR||"
        }

        class And(override val position: Int) : Token() {
            override val type: TokenType get() = TokenType.AND
            override val value: String get() = "&&"

            override fun toString(): String = "AD&&"
        }

        class Group(val values: List<Token>, override val position: Int) : Token() {
            override val type: TokenType get() = TokenType.GROUP
            override val value: String get() = ""
        }

        class Raw(val source: String, val start: Int, val end: Int) : Token() {
            override val value: String get() = source.substring(start, end)
            override val position: Int
                get() = start
            override val type: TokenType get() = TokenType.STRING

            override fun toString(): String = "R:$value"
        }
    }

    fun parseToTokens(source: String): List<Token> = ArrayList<Token>(
        max(source.length / 3, 16)
    ).apply {
        var index = 0
        var position = 0
        fun flushOld() {
            if (position > index) {
                val id = index
                index = position
                for (i in id until position) {
                    if (!source[i].isWhitespace()) {
                        add(Token.Raw(source, id, position))
                        return
                    }
                }
            }
        }

        val iterator = source.indices.iterator()
        for (i in iterator) {
            position = i
            when (source[i]) {
                '{' -> {
                    flushOld()
                    add(Token.LeftBracket(i))
                    index = i + 1
                }
                '|' -> {
                    if (source.getOrNull(i + 1) == '|') {
                        flushOld()
                        add(Token.Or(i))
                        index = i + 2
                        iterator.nextInt()
                    }
                }
                '&' -> {
                    if (source.getOrNull(i + 1) == '&') {
                        flushOld()
                        add(Token.And(i))
                        index = i + 2
                        iterator.nextInt()
                    }
                }
                '}' -> {
                    flushOld()
                    add(Token.RightBracket(i))
                    index = i + 1
                }
            }
        }
        position = source.length
        flushOld()
    }

    fun collect(source: String, tokens: Iterator<Token>, root: Boolean): List<Token> = ArrayList<Token>().apply {
        tokens.forEach { token ->
            if (token is Token.LeftBracket) {
                add(Token.Group(collect(source, tokens, false), token.position))
            } else if (token is Token.RightBracket) {
                if (root) {
                    throw IllegalArgumentException("Syntax error: Unexpected }, ${buildMsg(source, token.position)}")
                } else {
                    return@apply
                }
            } else add(token)
        }
        if (!root) {
            throw IllegalArgumentException("Syntax error: Excepted }, ${buildMsg(source, source.length)}")
        }
    }

    private fun buildMsg(source: String, position: Int): String {
        val ed = min(position + 10, source.length)
        val st = max(0, position - 10)
        return buildString {
            append('`')
            if (st != 0) append("...")
            append(source, st, ed)
            if (ed != source.length) append("...")
            append("` at ").append(position)
        }
    }

    fun check(source: String, tokens: Iterator<Token>, group: Token.Group?) {
        if (!tokens.hasNext()) {
            throw IllegalArgumentException("Syntax error: empty rule, ${buildMsg(source, group?.position ?: 0)}")
        }
        var type = false
        do {
            val next = tokens.next()
            if (type) {
                if (next is Token.Group || next is Token.Raw) {
                    throw IllegalArgumentException("Syntax error: Except logic but got expression, ${buildMsg(source, next.position)}")
                }
            } else {
                if (next is Token.Or || next is Token.And) {
                    throw IllegalArgumentException("Syntax error: Except expression but got logic, ${buildMsg(source, next.position)}")
                }
                if (next is Token.Group) {
                    check(source, next.values.iterator(), next)
                }
            }
            type = !type
        } while (tokens.hasNext())
        if (!type) {
            throw IllegalArgumentException("Syntax error: Except more expression, ${buildMsg(source, group?.values?.last()?.position ?: source.length)}")
        }
    }

    fun parse(source: String, token: Token): RequirementInternal {
        return when (token) {
            is Token.Group -> {
                if (token.values.size == 1) {
                    parse(source, token.values.first())
                } else {
                    val logic = token.values.asSequence().map { it.type }.filter {
                        it == TokenType.OR || it == TokenType.AND
                    }.toSet()
                    if (logic.size == 2) {
                        throw IllegalArgumentException("Syntax error: || and && cannot use in one group, ${buildMsg(source, token.position)}")
                    }
                    val rules = token.values.asSequence().filter {
                        it is Token.Raw || it is Token.Group
                    }.map { parse(source, it) }.toList()
                    when (logic.first()) {
                        TokenType.OR -> {
                            return object : RequirementInternal {
                                override fun test(version: SemVersion): Boolean {
                                    rules.forEach { if (it.test(version)) return true }
                                    return false
                                }
                            }
                        }
                        TokenType.AND -> {
                            return object : RequirementInternal {
                                override fun test(version: SemVersion): Boolean {
                                    rules.forEach { if (!it.test(version)) return false }
                                    return true
                                }
                            }
                        }
                        else -> throw AssertionError()
                    }
                }
            }
            is Token.Raw -> SemVersionInternal.parseRule(token.value)
            else -> throw AssertionError()
        }
    }

    fun StringBuilder.dump(prefix: String, token: Token) {
        when (token) {
            is Token.LeftBracket -> append("${prefix}LF {\n")

            is Token.RightBracket -> append("${prefix}LR }\n")

            is Token.Or -> append("${prefix}OR ||\n")

            is Token.And -> append("${prefix}AND &&\n")
            is Token.Group -> {
                append("${prefix}GROUP {\n")
                token.values.forEach { dump("$prefix  ", it) }
                append("${prefix}}\n")
            }
            is Token.Raw -> append("${prefix}RAW ${token.value}\n")
        }
    }
}