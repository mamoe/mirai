package net.mamoe.mirai.contact

import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.PlainText

/**
 * A contact is a [QQ] or a [Group] for one particular [Robot] instance only.
 *
 * @author Him188moe
 */
abstract class Contact(val number: Int) {

    /**
     * Async
     */
    abstract fun sendMessage(message: Message)

    fun sendMessage(message: String) {
        this.sendMessage(PlainText(message))
    }

    /**
     * Async
     */
    abstract fun sendXMLMessage(message: String)
}
