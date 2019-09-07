package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * used to replace old logger
 */
object MiraiLogger {
    infix fun log(o: Any?) = info(o)
    infix fun println(o: Any?) = info(o)
    infix fun info(o: Any?) = this.print(o.toString(), LoggerTextFormat.RESET)


    infix fun error(o: Any?) = this.print(o.toString(), LoggerTextFormat.RED)

    infix fun notice(o: Any?) = this.print(o.toString(), LoggerTextFormat.LIGHT_BLUE)

    infix fun success(o: Any?) = this.print(o.toString(), LoggerTextFormat.GREEN)

    infix fun debug(o: Any?) = this.print(o.toString(), LoggerTextFormat.YELLOW)

    infix fun catching(e: Throwable) {
        e.printStackTrace()
        /*
        this.print(e.message)
        this.print(e.localizedMessage)
        this.print(e.cause.toString())*/
    }

    private fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
        kotlin.io.println("$color[Mirai] $s : $value")
    }
}


fun Any.logInfo() = MiraiLogger.info(this)

fun Any.logDebug() = MiraiLogger.debug(this)

fun Any.logError() = MiraiLogger.error(this)

fun Any.logNotice() = MiraiLogger.notice(this)