package net.mamoe.mirai.contact

import net.mamoe.mirai.Robot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.At

/**
 * A QQ instance helps you to receive message from or send message to.
 * Notice that one QQ instance belong to one [Robot], that is, QQ instances from different [Robot] are NOT the same.
 *
 * @author Him188moe
 */
class QQ(robot: Robot, number: Long) : Contact(robot, number) {
    override fun sendMessage(message: Message) {
        robot.network.messageHandler.sendFriendMessage(this, message)
    }

    override fun sendXMLMessage(message: String) {

    }

    /**
     * At(@) this account.
     */
    fun at(): At {
        return At(this)
    }


    /*
    Make that we can use (QQ + QQ2 + QQ3).sendMessage( )

    operator fun plus(qq: QQ): QQCombination {

    }*/
}
