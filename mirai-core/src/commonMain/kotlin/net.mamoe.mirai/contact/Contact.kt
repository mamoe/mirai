@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.PlainText
import net.mamoe.mirai.message.toChain
import net.mamoe.mirai.network.protocol.tim.handler.EventPacketHandler

class ContactList<C : Contact> : MutableMap<UInt, C> by mutableMapOf()

/**
 * 联系人. 虽然叫做联系人, 但它直营
 * 现支持的联系人只有 [QQ号][QQ] 和 [群][Group].
 *
 * @param bot 这个联系人所属 [Bot]
 * @param id 可以是 QQ 号码或者群号码 [GroupId].
 *
 * @author Him188moe
 */
sealed class Contact(val bot: Bot, val id: UInt) {

    abstract suspend fun sendMessage(message: MessageChain)

    suspend fun sendMessage(message: Message) = sendMessage(message.toChain())
    suspend fun sendMessage(plain: String) = sendMessage(PlainText(plain))

    abstract suspend fun sendXMLMessage(message: String)
}


/**
 * 一般的用户可见的 ID.
 * 在 TIM/QQ 客户端中所看到的的号码均是这个 ID
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 */
inline class GroupId(val value: UInt)

fun UInt.groupId(): GroupId = GroupId(this)

/**
 * 一些群 API 使用的 ID. 在使用时会特别注明
 *
 * @see GroupInternalId.toId 由 [GroupInternalId] 转换为 [GroupId]
 */
inline class GroupInternalId(val value: UInt)

/**
 * 群.
 *
 * Group ID 与 Group Number 并不是同一个值.
 * - Group Number([Group.id]) 是通常使用的群号码.(在 QQ 客户端中可见)
 * - Group ID([Group.internalId]) 是与调用 API 时使用的 id.(在 QQ 客户端中不可见)
 * @author Him188moe
 */
class Group internal constructor(bot: Bot, id: UInt) : Contact(bot, id) {
    val internalId = GroupId(id).toInternalId()
    val members: ContactList<QQ>
        //todo members
        get() = throw UnsupportedOperationException("Not yet supported")

    override suspend fun sendMessage(message: MessageChain) {
        bot.network[EventPacketHandler].sendGroupMessage(this, message)
    }

    override suspend fun sendXMLMessage(message: String) {

    }

    companion object
}

/**
 * QQ 账号.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
class QQ internal constructor(bot: Bot, number: UInt) : Contact(bot, number) {
    override suspend fun sendMessage(message: MessageChain) {
        bot.network[EventPacketHandler].sendFriendMessage(this, message)
    }

    override suspend fun sendXMLMessage(message: String) {
        TODO()
    }
}