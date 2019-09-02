package net.mamoe.mirai.contact

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.At

/**
 * @author Him188moe
 */
class QQ(number: Long) : Contact(number) {

    override fun sendMessage(message: Message) {

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
