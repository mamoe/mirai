/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Utils")
@file:JvmMultifileClass
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * JVM 控制台日志实现
 *
 *
 * 单条日志格式 (正则) 为:
 * ```regex
 * ^([\w-]*\s[\w:]*)\s(\w)\/(.*?):\s(.+)$
 * ```
 * 其中 group 分别为: 日期与时间, 严重程度, [identity], 消息内容.
 *
 * 示例:
 * ```log
 * 2020-05-21 19:51:09 V/Bot 1994701021: Send: OidbSvc.0x88d_7
 * ```
 *
 * 日期时间格式为 `yyyy-MM-dd HH:mm:ss`,
 *
 * 严重程度为 V, I, W, E. 分别对应 verbose, info, warning, error
 *
 * @param isColored 是否添加 ANSI 颜色
 *
 * @see DefaultLogger
 * @see SingleFileLogger 使用单一文件记录日志
 * @see DirectoryLogger 在一个目录中按日期存放文件记录日志, 自动清理过期日志
 */
actual open class PlatformLogger @JvmOverloads constructor(
    override val identity: String? = "Mirai",
    /**
     * 日志输出. 不会自动添加换行
     */
    open val output: (String) -> Unit,
    val isColored: Boolean = true
) : MiraiLoggerPlatformBase() {
    actual constructor(identity: String?) : this(identity, ::println)

    private fun out(message: String?, priority: String, color: Color) {
        if (isColored) output("$color$currentTimeFormatted $priority/$identity: $message")
        else output("$currentTimeFormatted $priority/$identity: $message")
    }

    override fun verbose0(message: String?) = out(message, "V", Color.RESET)

    override fun verbose0(message: String?, e: Throwable?) {
        if (e != null) verbose((message ?: e.toString()) + "\n${e.stackTraceString}")
        else verbose(message.toString())
    }

    override fun info0(message: String?) = out(message, "I", Color.LIGHT_GREEN)
    override fun info0(message: String?, e: Throwable?) {
        if (e != null) info((message ?: e.toString()) + "\n${e.stackTraceString}")
        else info(message.toString())
    }

    override fun warning0(message: String?) = out(message, "W", Color.LIGHT_RED)
    override fun warning0(message: String?, e: Throwable?) {
        if (e != null) warning((message ?: e.toString()) + "\n${e.stackTraceString}")
        else warning(message.toString())
    }

    override fun error0(message: String?) = out(message, "E", Color.RED)
    override fun error0(message: String?, e: Throwable?) {
        if (e != null) error((message ?: e.toString()) + "\n${e.stackTraceString}")
        else error(message.toString())
    }

    override fun debug0(message: String?) = out(message, "D", Color.LIGHT_CYAN)
    override fun debug0(message: String?, e: Throwable?) {
        if (e != null) debug((message ?: e.toString()) + "\n${e.stackTraceString}")
        else debug(message.toString())
    }

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE)
    private val currentTimeFormatted get() = timeFormat.format(Date())

    /**
     * @author NaturalHG
     */
    @Suppress("unused")
    private enum class Color(private val format: String) {
        RESET("\u001b[0m"),

        WHITE("\u001b[30m"),
        RED("\u001b[31m"),
        EMERALD_GREEN("\u001b[32m"),
        GOLD("\u001b[33m"),
        BLUE("\u001b[34m"),
        PURPLE("\u001b[35m"),
        GREEN("\u001b[36m"),

        GRAY("\u001b[90m"),
        LIGHT_RED("\u001b[91m"),
        LIGHT_GREEN("\u001b[92m"),
        LIGHT_YELLOW("\u001b[93m"),
        LIGHT_BLUE("\u001b[94m"),
        LIGHT_PURPLE("\u001b[95m"),
        LIGHT_CYAN("\u001b[96m")
        ;

        override fun toString(): String = format
    }
}

@get:JvmSynthetic
internal val Throwable.stackTraceString
    get() = ByteArrayOutputStream().run {
        printStackTrace(PrintStream(this))
        String(this.toByteArray())
    }