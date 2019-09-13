package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.At
import net.mamoe.mirai.message.defaults.MessageChain

/**
 * QQ 账号.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 *
 * Java 获取 qq 号: `qq.getNumber()`
 * Java 获取所属 bot: `qq.getBot()`
 *
 * A QQ instance helps you to receive message from or sendPacket message to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
class QQ(bot: Bot, number: Long) : Contact(bot, number) {
    override fun sendMessage(message: MessageChain) {
        bot.network.message.sendFriendMessage(this, message)
    }

    override fun sendXMLMessage(message: String) {

    }

    /**
     * At(@) this account.
     *
     * @return an instance of [Message].
     */
    fun at(): At {
        return At(this)
    }


    /*
    Make that we can use (QQ + QQ2 + QQ3).sendMessage( )

    operator fun plus(qq: QQ): QQCombination {

    }*/
}
