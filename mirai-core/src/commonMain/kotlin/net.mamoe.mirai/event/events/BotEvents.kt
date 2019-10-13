package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event


abstract class BotEvent(val bot: Bot) : Event()

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)