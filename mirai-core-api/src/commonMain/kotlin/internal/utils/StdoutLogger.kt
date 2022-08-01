/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.*


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
 * 2020-05-21 19:51:09 V/Bot 123456789: Send: OidbSvc.0x88d_7
 * ```
 *
 * 日期时间格式为 `yyyy-MM-dd HH:mm:ss`,
 *
 * 严重程度为 V, I, W, E. 分别对应 verbose, info, warning, error
 *
 * @param isColored 是否添加 ANSI 颜色
 *
 * @see MiraiLogger.create
 * @see SingleFileLogger 使用单一文件记录日志
 * @see DirectoryLogger 在一个目录中按日期存放文件记录日志, 自动清理过期日志
 */
internal open class StdoutLogger constructor(
    override val identity: String? = "Mirai",
    /**
     * 日志输出. 不会自动添加换行
     */
    open val output: (String) -> Unit,
    val isColored: Boolean = true
) : MiraiLoggerPlatformBase() {
    constructor(identity: String?) : this(identity, ::println)
    constructor(identity: String?, output: (String) -> Unit) : this(identity, output, true)

    /**
     * 输出一条日志. [message] 末尾可能不带换行符.
     */
    protected open fun printLog(message: String?, priority: SimpleLogger.LogPriority) {
        if (isColored) output("${priority.color}$currentTimeFormatted ${priority.simpleName}/$identity: $message${Color.RESET}")
        else output("$currentTimeFormatted ${priority.simpleName}/$identity: $message")
    }

    /**
     * 获取指定 [SimpleLogger.LogPriority] 的颜色
     */
    protected open val SimpleLogger.LogPriority.color: Color
        get() = when (this) {
            SimpleLogger.LogPriority.VERBOSE -> Color.RESET
            SimpleLogger.LogPriority.INFO -> Color.LIGHT_GREEN
            SimpleLogger.LogPriority.WARNING -> Color.LIGHT_RED
            SimpleLogger.LogPriority.ERROR -> Color.RED
            SimpleLogger.LogPriority.DEBUG -> Color.LIGHT_CYAN
        }

    public override fun verbose0(message: String?): Unit = printLog(message, SimpleLogger.LogPriority.VERBOSE)

    public override fun verbose0(message: String?, e: Throwable?) {
        if (e != null) verbose((message ?: e.toString()) + "\n${e.stackTraceToString()}")
        else verbose(message.toString())
    }

    public override fun info0(message: String?): Unit = printLog(message, SimpleLogger.LogPriority.INFO)
    public override fun info0(message: String?, e: Throwable?) {
        if (e != null) info((message ?: e.toString()) + "\n${e.stackTraceToString()}")
        else info(message.toString())
    }

    public override fun warning0(message: String?): Unit = printLog(message, SimpleLogger.LogPriority.WARNING)
    public override fun warning0(message: String?, e: Throwable?) {
        if (e != null) warning((message ?: e.toString()) + "\n${e.stackTraceToString()}")
        else warning(message.toString())
    }

    public override fun error0(message: String?): Unit = printLog(message, SimpleLogger.LogPriority.ERROR)
    public override fun error0(message: String?, e: Throwable?) {
        if (e != null) error((message ?: e.toString()) + "\n${e.stackTraceToString()}")
        else error(message.toString())
    }

    public override fun debug0(message: String?): Unit = printLog(message, SimpleLogger.LogPriority.DEBUG)
    public override fun debug0(message: String?, e: Throwable?) {
        if (e != null) debug((message ?: e.toString()) + "\n${e.stackTraceToString()}")
        else debug(message.toString())
    }


    private val currentTimeFormatted get() = currentTimeFormatted(null)

    @MiraiExperimentalApi("This is subject to change.")
    protected enum class Color(private val format: String) {
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