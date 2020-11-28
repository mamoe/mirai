/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package net.mamoe.mirai.console.util

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.util.AnsiMessageBuilder.Companion.dropAnsi

public open class AnsiMessageBuilder public constructor(
    public val delegate: StringBuilder
) : Appendable {
    override fun toString(): String = delegate.toString()

    /**
     * 在添加 ansi code 的时候建议使用此方法.
     *
     * 在 `noAnsi=true` 的时候会忽略此函数的调用
     *
     * @see from
     * @see builder
     */
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
        private val DROP_ANSI_PATTERN = """\u001b\[([0-9]+)(;[0-9]+)*m""".toRegex()

        /**
         * 从 [String] 中剔除 ansi 控制符
         */
        @JvmStatic
        public fun String.dropAnsi(): String = DROP_ANSI_PATTERN.replace(this, "")

        @JvmStatic
        @JvmOverloads
        public fun from(
            builder: StringBuilder,
            noAnsi: Boolean = false
        ): AnsiMessageBuilder = if (noAnsi) {
            NoAnsiMessageBuilder(builder)
        } else AnsiMessageBuilder(builder)

        /**
         * @param capacity [StringBuilder] 的初始化大小
         */
        @JvmStatic
        @JvmOverloads
        public fun builder(
            capacity: Int = 16,
            noAnsi: Boolean = false
        ): AnsiMessageBuilder = from(StringBuilder(capacity), noAnsi)

        /**
         * 判断 [sender] 是否支持带 ansi 控制符的正确显示
         */
        @ConsoleExperimentalApi
        @JvmStatic
        public fun isAnsiSupported(sender: CommandSender): Boolean =
            if (sender is ConsoleCommandSender) {
                MiraiConsoleImplementationBridge.isAnsiSupported
            } else false

        /**
         * 往 [StringBuilder] 追加 ansi 控制符
         */
        public inline fun StringBuilder.appendAnsi(
            noAnsi: Boolean = false,
            action: AnsiMessageBuilder.() -> Unit
        ): AnsiMessageBuilder = from(this, noAnsi).apply(action)

    }

    /////////////////////////////////////////////////////////////////////////////////
    override fun append(c: Char): AnsiMessageBuilder = apply { delegate.append(c) }
    override fun append(csq: CharSequence?): AnsiMessageBuilder = apply { delegate.append(csq) }
    override fun append(csq: CharSequence?, start: Int, end: Int): AnsiMessageBuilder = apply { delegate.append(csq, start, end) }
    public fun append(any: Any?): AnsiMessageBuilder = apply { delegate.append(any) }
    public fun append(value: String): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: String, start: Int, end: Int): AnsiMessageBuilder = apply { delegate.append(value, start, end) }
    public fun append(value: Boolean): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Float): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Double): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Int): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Long): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Short): AnsiMessageBuilder = apply { delegate.append(value) }
    /////////////////////////////////////////////////////////////////////////////////
}

/**
 * 构建一条 ansi 信息
 *
 * @see [AnsiMessageBuilder]
 */
public inline fun buildAnsiMessage(
    capacity: Int = 16,
    action: AnsiMessageBuilder.() -> Unit
): String = AnsiMessageBuilder.builder(capacity, false).apply(action).toString()

// 不在 top-level 使用者会得到 Internal error: Couldn't inline sendAnsiMessage

/**
 * 向 [CommandSender] 发送一条带有 ansi 控制符的信息
 *
 * @see [AnsiMessageBuilder]
 */
public suspend inline fun CommandSender.sendAnsiMessage(
    capacity: Int = 16,
    builder: AnsiMessageBuilder.() -> Unit
) {
    sendMessage(
        AnsiMessageBuilder.builder(capacity, noAnsi = !AnsiMessageBuilder.isAnsiSupported(this))
            .apply(builder)
            .toString()
    )
}

/**
 * 向 [CommandSender] 发送一条带有 ansi 控制符的信息
 *
 * @see [AnsiMessageBuilder.Companion.dropAnsi]
 */
public suspend inline fun CommandSender.sendAnsiMessage(message: String) {
    sendMessage(
        if (AnsiMessageBuilder.isAnsiSupported(this))
            message
        else
            message.dropAnsi()
    )
}

public fun AnsiMessageBuilder(capacity: Int = 16): AnsiMessageBuilder = AnsiMessageBuilder(StringBuilder(capacity))
