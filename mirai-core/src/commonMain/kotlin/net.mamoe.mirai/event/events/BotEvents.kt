package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Cancellable
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.message.ImageId


abstract class BotEvent(val bot: Bot) : Event()

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)

/**
 * 上传好友图片时, 服务器返回好友图片 ID 事件.
 *
 * 在这之后图片将会被上传到服务器.
 */
class FriendImageIdObtainedEvent(bot: Bot, val imageId: ImageId) : BotEvent(bot), Cancellable