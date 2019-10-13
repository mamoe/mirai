package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.At
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.network.protocol.tim.handler.EventPacketHandler

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
class QQ(bot: Bot, number: Long) : Contact(bot, number) {
    override suspend fun sendMessage(message: MessageChain) {
        bot.network[EventPacketHandler].sendFriendMessage(this, message)
    }

    override suspend fun sendXMLMessage(message: String) {

    }
}

/**
 * At(@) this account.
 *
 * @return an instance of [Message].
 */
fun QQ.at(): At {
    return At(this)
}