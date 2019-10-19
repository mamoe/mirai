@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.handler.EventPacketHandler
import net.mamoe.mirai.utils.ContactList

/**
 * 联系人.
 *
 * A contact is a [QQ] or a [Group] for one particular [Bot] instance only.
 *
 * @param bot the Owner [Bot]
 * @param number the id number of this contact
 * @author Him188moe
 */
@Suppress("unused")
actual sealed class Contact actual constructor(bot: Bot, number: UInt) : PlatformContactBase(bot, number) {

    abstract override suspend fun sendMessage(message: MessageChain)

    abstract override suspend fun sendXMLMessage(message: String)


    /**
     * 阻塞发送一个消息. 仅应在 Java 使用
     */
    fun blockingSendMessage(chain: MessageChain) = runBlocking { sendMessage(chain) }

    /**
     * 阻塞发送一个消息. 仅应在 Java 使用
     */
    fun blockingSendMessage(message: Message) = runBlocking { sendMessage(message) }

    /**
     * 阻塞发送一个消息. 仅应在 Java 使用
     */
    fun blockingSendMessage(plain: String) = runBlocking { sendMessage(plain) }

    /**
     * 异步发送一个消息. 仅应在 Java 使用
     */
    fun asyncSendMessage(chain: MessageChain) = bot.network.NetworkScope.launch { sendMessage(chain) }

    /**
     * 异步发送一个消息. 仅应在 Java 使用
     */
    fun asyncSendMessage(message: Message) = bot.network.NetworkScope.launch { sendMessage(message) }

    /**
     * 异步发送一个消息. 仅应在 Java 使用
     */
    fun asyncSendMessage(plain: String) = bot.network.NetworkScope.launch { sendMessage(plain) }
}

/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.number]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.groupId]) 是与服务器通讯时使用的 id.(在 QQ 客户端中不可见)
 *
 * Java 获取 groupNumber: `group.getNumber()`
 * Java 获取所属 bot: `group.getBot()`
 * Java 获取群成员列表: `group.getMembers()`
 * Java 获取 groupId: `group.getGroupId()`
 *
 * Java 调用 [groupNumberToId] : `Group.groupNumberToId(number)`
 * @author Him188moe
 */
actual class Group actual constructor(bot: Bot, number: UInt) : Contact(bot, number) {
    actual val groupId = groupNumberToId(number)
    actual val members: ContactList<QQ>
        //todo members
        get() = throw UnsupportedOperationException("Not yet supported")

    actual override suspend fun sendMessage(message: MessageChain) {
        bot.network[EventPacketHandler].sendGroupMessage(this, message)
    }

    actual override suspend fun sendXMLMessage(message: String) {

    }

    actual companion object
}

/**
 * QQ 账号.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 *
 * Java 获取 qq 号: `qq.getNumber()`
 * Java 获取所属 bot: `qq.getBot()`
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
actual class QQ actual constructor(bot: Bot, number: UInt) : Contact(bot, number) {
    actual override suspend fun sendMessage(message: MessageChain) {
        bot.network[EventPacketHandler].sendFriendMessage(this, message)
    }

    actual override suspend fun sendXMLMessage(message: String) {
        TODO()
    }
}