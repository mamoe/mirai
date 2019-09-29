package net.mamoe.mirai.event.events.group

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.bot.BotEvent

/**
 * @author Him188moe
 */
abstract class GroupEvent(bot: Bot, val group: Group) : BotEvent(bot)