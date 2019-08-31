package net.mamoe.mirai.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * used to replace old logger
 */
object MiraiLogger {
    infix fun info(o: Any?) {
        this.print(o.toString())
    }

    infix fun error(o: Any?) {
        this.print(o.toString(), LoggerTextFormat.RED)
    }

    infix fun log(o: Any?) = info(o)

    infix fun println(o: Any?) = info(o)

    infix fun debug(o: Any?) {
        this.print(o.toString())
    }

    infix fun catching(e: Throwable) {
        e.printStackTrace()
        /*
        this.print(e.message)
        this.print(e.localizedMessage)
        this.print(e.cause.toString())*/
    }

    private fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.BLUE) {
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
        kotlin.io.println("$color[Mirai] $s : $value")
    }
}


fun log(any: Any?) = MiraiLogger.info(any)