package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

actual typealias PlatformLogger = Console

/**
 * JVM 控制台日志实现
 */
open class Console @JvmOverloads internal constructor(
    override val identity: String? = "Mirai"
) : MiraiLoggerPlatformBase() {
    override fun verbose0(any: Any?) = println(any, LoggerTextFormat.RESET)
    override fun verbose0(message: String?, e: Throwable?) {
        if (message != null) verbose(message.toString())
        e?.printStackTrace()
    }

    override fun info0(any: Any?) = println(any, LoggerTextFormat.LIGHT_GREEN)
    override fun info0(message: String?, e: Throwable?) {
        if (message != null) info(message.toString())
        e?.printStackTrace()
    }

    override fun warning0(any: Any?) = println(any, LoggerTextFormat.LIGHT_RED)
    override fun warning0(message: String?, e: Throwable?) {
        if (message != null) warning(message.toString())
        e?.printStackTrace()
    }

    override fun error0(any: Any?) = println(any, LoggerTextFormat.RED)
    override fun error0(message: String?, e: Throwable?) {
        if (message != null) error(message.toString())
        e?.printStackTrace()
    }

    override fun debug0(any: Any?) = println(any, LoggerTextFormat.LIGHT_CYAN)
    override fun debug0(message: String?, e: Throwable?) {
        if (message != null) debug(message.toString())
        e?.printStackTrace()
    }

    private fun println(value: Any?, color: LoggerTextFormat) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.SIMPLIFIED_CHINESE).format(Date())

        if (identity == null) {
            println("$color$time : $value")
        } else {
            println("$color$identity $time : $value")
        }
    }
}

/**
 * @author NaturalHG
 */
internal enum class LoggerTextFormat(private val format: String) {
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