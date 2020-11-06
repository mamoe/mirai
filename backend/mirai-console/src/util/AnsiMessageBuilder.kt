/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.util

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge

public open class AnsiMessageBuilder internal constructor(
    public val builder: StringBuilder
) : Appendable {
    public inline fun <R> builder(action: StringBuilder.() -> R): R = builder.action()
    override fun toString(): String = builder.toString()

    public open fun ansi(code: String): AnsiMessageBuilder = append(code)
    public open fun reset(): AnsiMessageBuilder = append(Color.RESET)
    public open fun white(): AnsiMessageBuilder = append(Color.WHITE)
    public open fun red(): AnsiMessageBuilder = append(Color.RED)
    public open fun emeraldGreen(): AnsiMessageBuilder = append(Color.EMERALD_GREEN)
    public open fun gold(): AnsiMessageBuilder = append(Color.GOLD)
    public open fun blue(): AnsiMessageBuilder = append(Color.BLUE)
    public open fun purple(): AnsiMessageBuilder = append(Color.PURPLE)
    public open fun green(): AnsiMessageBuilder = append(Color.GREEN)
    public open fun gray(): AnsiMessageBuilder = append(Color.GRAY)
    public open fun lightRed(): AnsiMessageBuilder = append(Color.LIGHT_RED)
    public open fun lightGreen(): AnsiMessageBuilder = append(Color.LIGHT_GREEN)
    public open fun lightYellow(): AnsiMessageBuilder = append(Color.LIGHT_YELLOW)
    public open fun lightBlue(): AnsiMessageBuilder = append(Color.LIGHT_BLUE)
    public open fun lightPurple(): AnsiMessageBuilder = append(Color.LIGHT_PURPLE)
    public open fun lightCyan(): AnsiMessageBuilder = append(Color.LIGHT_CYAN)

    internal object Color {
        const val RESET = "\u001b[0m"
        const val WHITE = "\u001b[30m"
        const val RED = "\u001b[31m"
        const val EMERALD_GREEN = "\u001b[32m"
        const val GOLD = "\u001b[33m"
        const val BLUE = "\u001b[34m"
        const val PURPLE = "\u001b[35m"
        const val GREEN = "\u001b[36m"
        const val GRAY = "\u001b[90m"
        const val LIGHT_RED = "\u001b[91m"
        const val LIGHT_GREEN = "\u001b[92m"
        const val LIGHT_YELLOW = "\u001b[93m"
        const val LIGHT_BLUE = "\u001b[94m"
        const val LIGHT_PURPLE = "\u001b[95m"
        const val LIGHT_CYAN = "\u001b[96m"
    }

    internal class NoAnsiMessageBuilder(builder: StringBuilder) : AnsiMessageBuilder(builder) {
        override fun reset(): AnsiMessageBuilder = this
        override fun white(): AnsiMessageBuilder = this
        override fun red(): AnsiMessageBuilder = this
        override fun emeraldGreen(): AnsiMessageBuilder = this
        override fun gold(): AnsiMessageBuilder = this
        override fun blue(): AnsiMessageBuilder = this
        override fun purple(): AnsiMessageBuilder = this
        override fun green(): AnsiMessageBuilder = this
        override fun gray(): AnsiMessageBuilder = this
        override fun lightRed(): AnsiMessageBuilder = this
        override fun lightGreen(): AnsiMessageBuilder = this
        override fun lightYellow(): AnsiMessageBuilder = this
        override fun lightBlue(): AnsiMessageBuilder = this
        override fun lightPurple(): AnsiMessageBuilder = this
        override fun lightCyan(): AnsiMessageBuilder = this
        override fun ansi(code: String): AnsiMessageBuilder = this
    }

    public companion object {
        public fun ansiMessageBuilder(
            builder: StringBuilder,
            noAnsi: Boolean = false
        ): AnsiMessageBuilder = if (noAnsi) {
            NoAnsiMessageBuilder(builder)
        } else AnsiMessageBuilder(builder)

        public fun ansiMessageBuilder(
            size: Int = 16,
            noAnsi: Boolean = false
        ): AnsiMessageBuilder = ansiMessageBuilder(StringBuilder(size), noAnsi)

        public inline fun buildAnsiMessage(
            builder: StringBuilder,
            noAnsi: Boolean = false,
            action: AnsiMessageBuilder.() -> Unit
        ): String = ansiMessageBuilder(builder, noAnsi).apply(action).toString()

        public inline fun buildAnsiMessage(
            size: Int = 16,
            noAnsi: Boolean = false,
            action: AnsiMessageBuilder.() -> Unit
        ): String = ansiMessageBuilder(size, noAnsi).apply(action).toString()

        @ConsoleExperimentalApi
        public fun isAnsiSupport(sender: CommandSender): Boolean =
            if (sender is ConsoleCommandSender) {
                MiraiConsoleImplementationBridge.isAnsiSupport
            } else false

        public suspend fun CommandSender.sendAnsiMessage(
            size: Int = 16,
            builder: AnsiMessageBuilder.() -> Unit
        ) {
            sendMessage(buildAnsiMessage(size, noAnsi = !isAnsiSupport(this), builder))
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    override fun append(c: Char): AnsiMessageBuilder {
        builder.append(c); return this
    }

    override fun append(csq: CharSequence?): AnsiMessageBuilder {
        builder.append(csq); return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): AnsiMessageBuilder {
        builder.append(csq, start, end); return this
    }

    public fun append(any: Any?): AnsiMessageBuilder {
        builder.append(any); return this
    }

    public fun append(value: String): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: String, start: Int, end: Int): AnsiMessageBuilder {
        builder.append(value, start, end); return this
    }

    public fun append(value: Boolean): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: Float): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: Double): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: Int): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: Long): AnsiMessageBuilder {
        builder.append(value); return this
    }

    public fun append(value: Short): AnsiMessageBuilder {
        builder.append(value); return this
    }
    /////////////////////////////////////////////////////////////////////////////////
}