package net.mamoe.mirai.utils

import net.mamoe.mirai.Robot
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

    @Synchronized
    private fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
        kotlin.io.println("$color[Mirai] $s : $value")
    }
}

infix fun Robot.log(o: Any?) = info(o)
infix fun Robot.println(o: Any?) = info(o)
infix fun Robot.info(o: Any?) = print(this, o.toString(), LoggerTextFormat.RESET)

infix fun Robot.error(o: Any?) = print(this, o.toString(), LoggerTextFormat.RED)

infix fun Robot.notice(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_BLUE)

infix fun Robot.purple(o: Any?) = print(this, o.toString(), LoggerTextFormat.PURPLE)

infix fun Robot.cyanL(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_CYAN)

infix fun Robot.success(o: Any?) = print(this, o.toString(), LoggerTextFormat.GREEN)

infix fun Robot.debug(o: Any?) = print(this, o.toString(), LoggerTextFormat.YELLOW)


@Synchronized
private fun print(robot: Robot, value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
    val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
    kotlin.io.println("$color[Mirai] $s #R${robot.id}: $value")
}


@Synchronized
private fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
    val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
    kotlin.io.println("$color[Mirai] $s : $value")
}

fun Any.logInfo() = MiraiLogger.info(this)

fun Any.logDebug() = MiraiLogger.debug(this)

fun Any.logError() = MiraiLogger.error(this)

fun Any.logNotice() = MiraiLogger.notice(this)