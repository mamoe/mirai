package net.mamoe.mirai.qqandroid.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.events.BotEvent

/**
 * 被挤下线
 */
class ForceOfflineEvent(
    override val bot: Bot,
    val title: String,
    val tips: String
) : BotEvent(), Packet