@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.BotNetworkConfiguration

/*
 * The mirror of functions in inner classes of [Bot]
 */

//Contacts
suspend fun Bot.getQQ(number: Long): QQ = this.contacts.getQQ(number.toUInt())

suspend fun Bot.getQQ(number: UInt): QQ = this.contacts.getQQ(number)

suspend fun Bot.getGroup(id: GroupId): Group = this.contacts.getGroup(id)
suspend fun Bot.getGroup(internalId: GroupInternalId): Group = this.contacts.getGroup(internalId)

val Bot.groups: ContactList<Group> get() = this.contacts.groups
val Bot.qqs: ContactList<QQ> get() = this.contacts.qqs


//NetworkHandler
suspend fun Bot.sendPacket(packet: OutgoingPacket) = this.network.sendPacket(packet)

suspend fun Bot.login(configuration: BotNetworkConfiguration.() -> Unit): LoginResult = this.network.login(BotNetworkConfiguration().apply(configuration))

suspend fun Bot.login(): LoginResult = this.network.login(BotNetworkConfiguration.Default)

//BotAccount
val Bot.qqAccount: UInt get() = this.account.id
