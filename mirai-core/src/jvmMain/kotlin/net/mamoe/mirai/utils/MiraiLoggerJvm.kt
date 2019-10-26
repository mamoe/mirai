package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

actual typealias PlatformLogger = Console

/**
 * JVM 控制台日志实现
 */
open class Console @JvmOverloads internal constructor(
        override var identity: String? = null
) : MiraiLoggerPlatformBase() {
    override fun logGreen0(any: Any?) = println(any.toString(), LoggerTextFormat.GREEN)
    override fun logPurple0(any: Any?) = println(any.toString(), LoggerTextFormat.LIGHT_PURPLE)
    override fun logBlue0(any: Any?) = println(any.toString(), LoggerTextFormat.BLUE)
    override fun logCyan0(any: Any?) = println(any.toString(), LoggerTextFormat.LIGHT_CYAN)
    override fun logError0(any: Any?) = println(any.toString(), LoggerTextFormat.RED)
    override fun log0(e: Throwable) = e.printStackTrace()
    override fun log0(any: Any?) = println(any.toString())//kotlin println
    override fun logDebug0(any: Any?) {
        if (DEBUGGING) {
            println(any.toString(), LoggerTextFormat.YELLOW)
        }
    }

    private fun println(value: String?, color: LoggerTextFormat) {
        val time = SimpleDateFormat("HH:mm:ss").format(Date())

        if (identity == null) {
            println("$color$time : $value")
        } else {
            println("$color$identity $time : $value")
        }
    }
}

private val DEBUGGING: Boolean by lazy {
    //todo 添加环境变量检测
    //avoid inspections
    true
}