package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.network.packet.goto
import java.text.SimpleDateFormat
import java.util.*

/**
 * used to replace old logger
 *
 * @author Him188moe
 * @author NaturalHG
 */
object MiraiLogger {
    fun log(o: Any?) = info(o)
    fun println(o: Any?) = info(o)
    fun info(o: Any?) = this.print(o.toString(), LoggerTextFormat.RESET)


    fun error(o: Any?) = this.print(o.toString(), LoggerTextFormat.RED)

    fun notice(o: Any?) = this.print(o.toString(), LoggerTextFormat.LIGHT_BLUE)

    fun success(o: Any?) = this.print(o.toString(), LoggerTextFormat.GREEN)

    fun debug(o: Any?) = this.print(o.toString(), LoggerTextFormat.YELLOW)

    fun catching(e: Throwable) {
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

fun Bot.log(o: Any?) = info(o)
fun Bot.println(o: Any?) = info(o)
fun Bot.info(o: Any?) = print(this, o.toString(), LoggerTextFormat.RESET)

fun Bot.error(o: Any?) = print(this, o.toString(), LoggerTextFormat.RED)

fun Bot.notice(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_BLUE)

fun Bot.purple(o: Any?) = print(this, o.toString(), LoggerTextFormat.PURPLE)

fun Bot.cyanL(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_CYAN)
fun Bot.success(o: Any?) = print(this, o.toString(), LoggerTextFormat.GREEN)

fun Bot.debug(o: Any?) = print(this, o.toString(), LoggerTextFormat.YELLOW)

fun Bot.debugPacket(packet: ServerPacket) {
    debug("Packet=$packet")
    debug("Packet size=" + packet.input.goto(0).readAllBytes().size)
    debug("Packet data=" + packet.input.goto(0).readAllBytes().toUHexString())
}


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