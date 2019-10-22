@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.io.core.readBytes
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.BotNetworkConfiguration
import net.mamoe.mirai.utils.ContactList
import net.mamoe.mirai.utils.io.toUHexString

/**
 * The mirror of functions in inner classes of [Bot]
 */

//Contacts
fun Bot.getQQ(number: Long): QQ = this.contacts.getQQ(number.toUInt())

fun Bot.getQQ(number: UInt): QQ = this.contacts.getQQ(number)

fun Bot.getGroupByNumber(number: Long): Group = this.contacts.getGroupByNumber(number.toUInt())
fun Bot.getGroupByNumber(number: UInt): Group = this.contacts.getGroupByNumber(number)

fun Bot.getGroupById(number: UInt): Group = this.contacts.getGroupById(number)

val Bot.groups: ContactList<Group> get() = this.contacts.groups

val Bot.qqs: ContactList<QQ> get() = this.contacts.qqs


//NetworkHandler
suspend fun Bot.sendPacket(packet: OutgoingPacket) = this.network.sendPacket(packet)

suspend fun Bot.login(configuration: BotNetworkConfiguration.() -> Unit): LoginResult = this.network.login(BotNetworkConfiguration().apply(configuration))

suspend fun Bot.login(): LoginResult = this.network.login(BotNetworkConfiguration.Default)

//BotAccount
val Bot.qqAccount: UInt get() = this.account.account


//logging
fun Bot.log(o: Any?) = logInfo(o)

fun Bot.println(o: Any?) = logInfo(o)
fun Bot.logInfo(o: Any?) = this.logger.logInfo(o)

fun Bot.logError(o: Any?) = this.logger.logError(o)

fun Bot.logPurple(o: Any?) = this.logger.logPurple(o)

fun Bot.logCyan(o: Any?) = this.logger.logCyan(o)
fun Bot.logGreen(o: Any?) = this.logger.logGreen(o)

fun Bot.logDebug(o: Any?) = this.logger.logDebug(o)

fun Bot.printPacketDebugging(packet: ServerPacket) {
    logDebug("Packet=$packet")
    logDebug("Packet size=" + packet.input.readBytes().size)
    logDebug("Packet data=" + packet.input.readBytes().toUHexString())
}