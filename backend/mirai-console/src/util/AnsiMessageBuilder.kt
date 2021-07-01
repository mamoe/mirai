/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package net.mamoe.mirai.console.util

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.util.AnsiMessageBuilder.Companion.asAnsiMessageBuilder
import net.mamoe.mirai.console.util.AnsiMessageBuilder.Companion.dropAnsi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import java.io.Serializable

/**
 * @see buildAnsiMessage
 * @see sendAnsiMessage
 * @see AnsiMessageBuilder
 * @see AnsiMessageBuilder.create
 * @see asAnsiMessageBuilder
 * @since 1.1
 */
public open class AnsiMessageBuilder public constructor(
    public val delegate: StringBuilder,
) : Appendable, Serializable {
    override fun toString(): String = delegate.toString()

    /**
     * 同 [append] 方法, 在 `noAnsi=true` 的时候会忽略此函数的调用
     *
     * 参考资料:
     * - [ANSI转义序列](https://zh.wikipedia.org/wiki/ANSI%E8%BD%AC%E4%B9%89%E5%BA%8F%E5%88%97)
     * - [ANSI转义序列#颜色](https://zh.wikipedia.org/wiki/ANSI%E8%BD%AC%E4%B9%89%E5%BA%8F%E5%88%97#%E9%A2%9C%E8%89%B2)
     *
     * @param code Ansi 操作码
     *
     * @see asAnsiMessageBuilder
     * @see create
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

    public companion object {
        /**
         * 从 [String] 中剔除 ansi 控制符
         */
        @JvmStatic
        public fun String.dropAnsi(): String = this
            .replace(DROP_CSI_PATTERN, "") // 先进行 CSI 剔除后进行 ANSI 剔除
            .replace(DROP_ANSI_PATTERN, "")

        /**
         * 使用 [this] 封装一个 [AnsiMessageBuilder]
         *
         * @param noAnsi 为 `true` 时忽略全部与 ansi 有关的方法的调用
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("from")
        public fun StringBuilder.asAnsiMessageBuilder(noAnsi: Boolean = false): AnsiMessageBuilder =
            if (noAnsi) NoAnsiMessageBuilder(this) else AnsiMessageBuilder(this)

        /**
         * @param capacity [StringBuilder] 的初始化大小
         *
         * @param noAnsi 为 `true` 时忽略全部与 ansi 有关的方法的调用
         * @see AnsiMessageBuilder
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            capacity: Int = 16,
            noAnsi: Boolean = false,
        ): AnsiMessageBuilder = StringBuilder(capacity).asAnsiMessageBuilder(noAnsi)

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
            action: AnsiMessageBuilder.() -> Unit,
        ): AnsiMessageBuilder = this.asAnsiMessageBuilder().apply(action)
    }

    override fun hashCode(): Int = this.delegate.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class.java != this::class.java) return false
        other as AnsiMessageBuilder
        if (other.delegate != this.delegate) return false
        return true
    }

    /////////////////////////////////////////////////////////////////////////////////
    override fun append(c: Char): AnsiMessageBuilder = apply { delegate.append(c) }
    override fun append(csq: CharSequence?): AnsiMessageBuilder = apply { delegate.append(csq) }
    override fun append(csq: CharSequence?, start: Int, end: Int): AnsiMessageBuilder =
        apply { delegate.append(csq, start, end) }

    public fun append(any: Any?): AnsiMessageBuilder = apply { delegate.append(any) }
    public fun append(value: String): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: String, start: Int, end: Int): AnsiMessageBuilder =
        apply { delegate.append(value, start, end) }

    public fun append(value: Boolean): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Float): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Double): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Int): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Long): AnsiMessageBuilder = apply { delegate.append(value) }
    public fun append(value: Short): AnsiMessageBuilder = apply { delegate.append(value) }
    /////////////////////////////////////////////////////////////////////////////////
}

/**
 * @param capacity [StringBuilder] 初始化大小
 * @see AnsiMessageBuilder.create
 * @since 1.1
 */
@JvmSynthetic
public fun AnsiMessageBuilder(capacity: Int = 16): AnsiMessageBuilder = AnsiMessageBuilder(StringBuilder(capacity))

/**
 * 构建一条 ANSI 信息
 *
 * @see AnsiMessageBuilder
 * @since 1.1
 */
@JvmSynthetic
public inline fun buildAnsiMessage(
    capacity: Int = 16,
    action: AnsiMessageBuilder.() -> Unit,
): String = AnsiMessageBuilder.create(capacity, false).apply(action).toString()

// 不在 top-level 使用者会得到 Internal error: Couldn't inline sendAnsiMessage

/**
 * 向 [CommandSender] 发送一条带有 ANSI 控制符的信息
 *
 * @see AnsiMessageBuilder
 * @since 1.1
 */
@JvmSynthetic
public suspend inline fun CommandSender.sendAnsiMessage(
    capacity: Int = 16,
    builder: AnsiMessageBuilder.() -> Unit,
): MessageReceipt<Contact>? {
    return sendMessage(
        AnsiMessageBuilder.create(capacity, noAnsi = !AnsiMessageBuilder.isAnsiSupported(this))
            .apply(builder)
            .toString()
    )
}

/**
 * 向 [CommandSender] 发送一条带有 ANSI 控制符的信息
 *
 * @see AnsiMessageBuilder.Companion.dropAnsi
 * @since 1.1
 */
@JvmSynthetic
public suspend inline fun CommandSender.sendAnsiMessage(message: String): MessageReceipt<Contact>? {
    return sendMessage(
        if (AnsiMessageBuilder.isAnsiSupported(this)) message
        else message.dropAnsi()
    )
}


// internals


// CSI序列由ESC [、若干个（包括0个）“参数字节”、若干个“中间字节”，以及一个“最终字节”组成。各部分的字符范围如下：
//
// CSI序列在ESC [之后各个组成部分的字符范围[12]:5.4
// 组成部分	字符范围	ASCII
// 参数字节	0x30–0x3F	0–9:;<=>?
// 中间字节	0x20–0x2F	空格、!"#$%&'()*+,-./
// 最终字节	0x40–0x7E	@A–Z[\]^_`a–z{|}~
//
// @see https://zh.wikipedia.org/wiki/ANSI%E8%BD%AC%E4%B9%89%E5%BA%8F%E5%88%97#CSI%E5%BA%8F%E5%88%97
@Suppress("RegExpRedundantEscape")
private val DROP_CSI_PATTERN = """\u001b\[([\u0030-\u003F])*?([\u0020-\u002F])*?[\u0040-\u007E]""".toRegex()

// 序列具有不同的长度。所有序列都以ASCII字符ESC（27 / 十六进制 0x1B）开头，
// 第二个字节则是0x40–0x5F（ASCII @A–Z[\]^_）范围内的字符。[12]:5.3.a
//
// 标准规定，在8位环境中，这两个字节的序列可以合并为0x80-0x9F范围内的单个字节（详情请参阅C1控制字符集）。
// 但是，在现代设备上，这些代码通常用于其他目的，例如UTF-8的一部分或CP-1252字符，因此并不使用这种合并的方式。
//
// 除ESC之外的其他C0代码（通常是BEL，BS，CR，LF，FF，TAB，VT，SO和SI）在输出时也可能会产生与某些控制序列相似或相同的效果。
//
// @see https://zh.wikipedia.org/wiki/ANSI%E8%BD%AC%E4%B9%89%E5%BA%8F%E5%88%97#%E8%BD%AC%E4%B9%89%E5%BA%8F%E5%88%97
//
// 注: 缺少详细资料, 只能认定 ansi 长度固定为二字节 (CSI除外)
private val DROP_ANSI_PATTERN = """\u001b[\u0040–\u005F]""".toRegex()

private object Color {
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

private class NoAnsiMessageBuilder(builder: StringBuilder) : AnsiMessageBuilder(builder) {
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
