package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

actual typealias PlatformLogger = Console

open class Console @JvmOverloads constructor(
        override var identity: String? = null
) : MiraiLogger {
    override fun logGreen(any: Any?) = println(any.toString(), LoggerTextFormat.GREEN)
    override fun logPurple(any: Any?) = println(any.toString(), LoggerTextFormat.LIGHT_PURPLE)
    override fun logBlue(any: Any?) = println(any.toString(), LoggerTextFormat.BLUE)
    override fun logCyan(any: Any?) = println(any.toString(), LoggerTextFormat.LIGHT_CYAN)
    override fun logError(any: Any?) = println(any.toString(), LoggerTextFormat.RED)
    override fun log(e: Throwable) = e.printStackTrace()
    override fun log(any: Any?) = println(any.toString())//kotlin println
    override fun logDebug(any: Any?) {
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
    //avoid inspections
    true
}