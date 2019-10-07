package net.mamoe.mirai

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import net.mamoe.mirai.utils.ContactList

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