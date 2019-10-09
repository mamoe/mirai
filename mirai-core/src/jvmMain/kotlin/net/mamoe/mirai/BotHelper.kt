@file:Suppress("unused")

package net.mamoe.mirai

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.goto
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import net.mamoe.mirai.utils.ContactList
import net.mamoe.mirai.utils.LoggerTextFormat
import net.mamoe.mirai.utils.toUHexString
import java.text.SimpleDateFormat
import java.util.*

/**
 * The mirror of functions in inner classes of [Bot]
 *
 * @author Him188moe
 */

//Contacts
fun Bot.getQQ(number: Long): QQ = this.contacts.getQQ(number)

fun Bot.getGroupByNumber(number: Long): Group = this.contacts.getGroupByNumber(number)

fun Bot.getGroupById(number: Long): Group = this.contacts.getGroupById(number)

val Bot.groups: ContactList<Group> get() = this.contacts.groups

val Bot.qqs: ContactList<QQ> get() = this.contacts.qqs


//NetworkHandler
suspend fun Bot.sendPacket(packet: ClientPacket) = this.network.socket.sendPacket(packet)

suspend fun Bot.login(): LoginState = this.network.login()

//BotAccount
val Bot.qqNumber: Long get() = this.account.qqNumber


//logging
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

private fun print(bot: Bot, value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
    val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
    println("$color[Mirai] $s #R${bot.id}: $value")
}

private fun print(value: String?, color: LoggerTextFormat = LoggerTextFormat.WHITE) {
    val s = SimpleDateFormat("MM-dd HH:mm:ss").format(Date())
    println("$color[Mirai] $s : $value")
}