package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.PlainText
import net.mamoe.mirai.message.toChain

/**
 * 联系人.
 *
 * A contact is a [QQ] or a [Group] for one particular [Bot] instance only.
 *
 * @param bot the Owner [Bot]
 * @param number the id number of this contact
 * @author Him188moe
 */
abstract class Contact internal constructor(val bot: Bot, val number: Long) {

    abstract suspend fun sendMessage(message: MessageChain)

    suspend fun sendMessage(message: Message) {
        if (message is MessageChain) {
            return sendMessage(message)
        }
        return sendMessage(message.toChain())
    }

    suspend fun sendMessage(message: String) {
        this.sendMessage(PlainText(message))
    }

    suspend fun sendMessage(message: List<Message>) {
        this.sendMessage(MessageChain(message))
    }

    abstract suspend fun sendXMLMessage(message: String)
}
