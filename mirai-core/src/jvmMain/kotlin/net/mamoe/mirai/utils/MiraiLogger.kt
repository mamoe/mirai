package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Him188moe
 */
interface MiraiLogger {
    companion object : MiraiLogger by defaultLogger()

    var identity: String

    fun info(any: Any?) = log(any)
    fun log(any: Any?)

    fun error(any: Any?)

    fun debug(any: Any?)

    fun cyan(any: Any?)

    fun purple(any: Any?)

    fun green(any: Any?)

    fun blue(any: Any?)
}

/**
 * 由 mirai-console 或 mirai-web 等模块实现
 */
var defaultLogger: () -> MiraiLogger = { Console() }

val DEBUGGING: Boolean by lazy {
    //avoid inspections
    true
}

open class Console(
        override var identity: String = "[Unknown]"
) : MiraiLogger {
    override fun green(any: Any?) = print(any.toString(), LoggerTextFormat.GREEN)
    override fun purple(any: Any?) = print(any.toString(), LoggerTextFormat.LIGHT_PURPLE)
    override fun blue(any: Any?) = print(any.toString(), LoggerTextFormat.BLUE)
    override fun cyan(any: Any?) = print(any.toString(), LoggerTextFormat.LIGHT_CYAN)
    override fun error(any: Any?) = print(any.toString(), LoggerTextFormat.RED)
    override fun log(any: Any?) = print(any.toString(), LoggerTextFormat.LIGHT_GRAY)
    override fun debug(any: Any?) {
        if (DEBUGGING) {
            print(any.toString(), LoggerTextFormat.YELLOW)
        }
    }

    fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.YELLOW) {
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())

        println("$color$identity $s : $value")
    }
}