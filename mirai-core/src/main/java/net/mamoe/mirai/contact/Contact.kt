package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.defaults.MessageChain
import net.mamoe.mirai.message.defaults.PlainText

/**
 * A contact is a [QQ] or a [Group] for one particular [Bot] instance only.
 *
 * @param bot Owner [Bot]
 * @author Him188moe
 */
abstract class Contact internal constructor(val bot: Bot, val number: Long) {
    /**
     * Async
     */
    abstract fun sendMessage(message: MessageChain)

    fun sendMessage(message: Message) {
        if (message is MessageChain) {
            return sendMessage(message)
        }
        return sendMessage(message.toChain())
    }

    fun sendMessage(message: String) {
        this.sendMessage(PlainText(message))
    }

    /**
     * Async
     */
    abstract fun sendXMLMessage(message: String)
}
