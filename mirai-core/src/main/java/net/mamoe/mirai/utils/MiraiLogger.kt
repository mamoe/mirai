package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import java.text.SimpleDateFormat
import java.util.*

/**
 * used to replace old logger
 *
 * @author Him188moe
 * @author NaturalHG
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

infix fun Bot.log(o: Any?) = info(o)
infix fun Bot.println(o: Any?) = info(o)
infix fun Bot.info(o: Any?) = print(this, o.toString(), LoggerTextFormat.RESET)

infix fun Bot.error(o: Any?) = print(this, o.toString(), LoggerTextFormat.RED)

infix fun Bot.notice(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_BLUE)

infix fun Bot.purple(o: Any?) = print(this, o.toString(), LoggerTextFormat.PURPLE)

infix fun Bot.cyanL(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_CYAN)

infix fun Bot.success(o: Any?) = print(this, o.toString(), LoggerTextFormat.GREEN)

infix fun Bot.debug(o: Any?) = print(this, o.toString(), LoggerTextFormat.YELLOW)


@Synchronized
private fun print(bot: Bot, value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
    val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
    kotlin.io.println("$color[Mirai] $s #R${bot.id}: $value")
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