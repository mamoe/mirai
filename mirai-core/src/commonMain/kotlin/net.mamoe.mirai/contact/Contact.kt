@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.PlainText
import net.mamoe.mirai.message.toChain

/**
 * 联系人平台基础. 包含所有平台通用的函数等.
 *
 * @author Him188moe
 */
abstract class PlatformContactBase internal constructor(val bot: Bot, val number: UInt) {

    abstract suspend fun sendMessage(message: MessageChain)

    suspend fun sendMessage(message: Message) {
        if (message is MessageChain) {
            return sendMessage(message)
        }
        return sendMessage(message.toChain())
    }

    suspend fun sendMessage(plain: String) = this.sendMessage(PlainText(plain))


    abstract suspend fun sendXMLMessage(message: String)
}

/**
 * 所有的 [QQ], [Group] 都继承自这个类.
 * 在不同平台可能有不同的实现.
 * 如在 JVM, suspend 调用不便, [Contact] 中有简化调用的 `blocking`() 和 `async`
 */
expect sealed class Contact(bot: Bot, number: UInt) : PlatformContactBase