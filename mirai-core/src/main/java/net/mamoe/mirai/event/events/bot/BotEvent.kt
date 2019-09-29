package net.mamoe.mirai.event.events.bot

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.MiraiEvent

/**
 * @author Him188moe
 */
abstract class BotEvent(val bot: Bot) : MiraiEvent()

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)