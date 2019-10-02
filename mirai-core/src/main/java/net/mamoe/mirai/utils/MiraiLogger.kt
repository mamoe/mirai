package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.goto
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

private fun defaultLogger(): MiraiLogger = Console("[TOP Level]")

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

    @Synchronized
    fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.YELLOW) {
        val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())

        println("$color$identity $s : $value")
    }
}

fun Bot.log(o: Any?) = info(o)
fun Bot.println(o: Any?) = info(o)
fun Bot.info(o: Any?) = print(this, o.toString(), LoggerTextFormat.RESET)

fun Bot.error(o: Any?) = print(this, o.toString(), LoggerTextFormat.RED)

fun Bot.notice(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_BLUE)

fun Bot.purple(o: Any?) = print(this, o.toString(), LoggerTextFormat.PURPLE)

fun Bot.cyan(o: Any?) = print(this, o.toString(), LoggerTextFormat.LIGHT_CYAN)
fun Bot.green(o: Any?) = print(this, o.toString(), LoggerTextFormat.GREEN)

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