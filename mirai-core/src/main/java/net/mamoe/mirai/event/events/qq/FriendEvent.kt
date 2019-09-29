package net.mamoe.mirai.event.events.qq

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.bot.BotEvent

/**
 * @author Him188moe
 */
abstract class FriendEvent(bot: Bot, val sender: QQ) : BotEvent(bot)