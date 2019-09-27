package net.mamoe.mirai

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.packet.ClientPacket

/**
 * The mirror of functions in inner classes of [Bot]
 *
 * @author Him188moe
 */

//Contacts
fun Bot.getQQ(number: Long): QQ = this.contacts.getQQ(number)

fun Bot.getGroupByNumber(number: Long): Group = this.contacts.getGroupByNumber(number)

fun Bot.getGroupById(number: Long): Group = this.contacts.getGroupById(number)


//NetworkHandler
suspend fun Bot.sendPacket(packet: ClientPacket) {
    this.network.socket.sendPacket(packet)
}


//BotAccount

fun Bot.getBotQQ(): Long = this.account.qqNumber