@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.io.core.readBytes
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.ContactList
import net.mamoe.mirai.utils.LoginConfiguration
import net.mamoe.mirai.utils.toUHexString

/**
 * The mirror of functions in inner classes of [Bot]
 *
 * @author Him188moe
 */

//Contacts
fun Bot.getQQ(number: Long): QQ = this.contacts.getQQ(number)

fun Bot.getQQ(number: UInt): QQ = getQQ(number.toLong())

fun Bot.getGroupByNumber(number: Long): Group = this.contacts.getGroupByNumber(number)
fun Bot.getGroupByNumber(number: UInt): Group = getGroupByNumber(number.toLong())

fun Bot.getGroupById(number: Long): Group = this.contacts.getGroupById(number)

val Bot.groups: ContactList<Group> get() = this.contacts.groups

val Bot.qqs: ContactList<QQ> get() = this.contacts.qqs


//NetworkHandler
suspend fun Bot.sendPacket(packet: ClientPacket) = this.network.sendPacket(packet)

suspend fun Bot.login(configuration: LoginConfiguration.() -> Unit): LoginResult = this.network.login(LoginConfiguration().apply(configuration))

suspend fun Bot.login(): LoginResult = this.network.login(LoginConfiguration.Default)

//BotAccount
val Bot.qqNumber: Long get() = this.account.qqNumber


//logging
fun Bot.log(o: Any?) = info(o)

fun Bot.println(o: Any?) = info(o)
fun Bot.info(o: Any?) = this.logger.logInfo(o)

fun Bot.error(o: Any?) = this.logger.logError(o)

fun Bot.purple(o: Any?) = this.logger.logPurple(o)

fun Bot.cyan(o: Any?) = this.logger.logCyan(o)
fun Bot.green(o: Any?) = this.logger.logGreen(o)

fun Bot.debug(o: Any?) = this.logger.logDebug(o)

fun Bot.printPacketDebugging(packet: ServerPacket) {
    debug("Packet=$packet")
    debug("Packet size=" + packet.input.readBytes().size)
    debug("Packet data=" + packet.input.readBytes().toUHexString())
}