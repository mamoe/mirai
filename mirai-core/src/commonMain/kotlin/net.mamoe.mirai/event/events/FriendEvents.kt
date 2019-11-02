@file:Suppress("unused")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Profile
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Message
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.utils.OnlineStatus

/**
 * 好友事件
 */
sealed class FriendEvent(bot: Bot, val sender: QQ) : BotEvent(bot)

/**
 * 接受好友消息事件
 *
 * @author Him188moe
 */
class FriendMessageEvent(bot: Bot, sender: QQ, val message: MessageChain) : FriendEvent(bot, sender) {
    suspend inline fun reply(message: Message) = sender.sendMessage(message)

    suspend inline fun reply(message: String) = sender.sendMessage(message)

    suspend inline fun reply(message: MessageChain) = sender.sendMessage(message)//shortcut
}

/**
 * 好友发起会话事件. 即好友在消息输入框内输入任意内容.
 */
class FriendConversationInitializedEvent(bot: Bot, sender: QQ) : FriendEvent(bot, sender)

/**
 * 好友在线状态改变事件
 */
class FriendOnlineStatusChangedEvent(bot: Bot, sender: QQ, val newStatus: OnlineStatus) : FriendEvent(bot, sender)

/**
 * 好友个人资料更新
 */
class FriendProfileUpdatedEvent(bot: Bot, qq: QQ, val profile: Profile) : FriendEvent(bot, qq)