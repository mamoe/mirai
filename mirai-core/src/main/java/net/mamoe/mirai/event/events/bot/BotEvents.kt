package net.mamoe.mirai.event.events.bot

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.MiraiEvent

/**
 * @author Him188moe
 */
class BotLoginEvent(val bot: Bot) : MiraiEvent()

class BotLogoutEvent(val bot: Bot) : MiraiEvent()

class BotMessageReceivedEvent(val bot: Bot, val type: Type, val message: String) : MiraiEvent() {
    enum class Type {
        FRIEND,
        GROUP
    }
}
