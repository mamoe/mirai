package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event


abstract class BotEvent : Event {
    private lateinit var _bot: Bot
    open val bot: Bot get() = _bot

    constructor(bot: Bot) : super() {
        this._bot = bot
    }

    constructor() : super()
}

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)