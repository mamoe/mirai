package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.Packet

/**
 * 被挤下线
 */
data class ForceOfflineEvent(
    override val bot: Bot,
    val title: String,
    val tips: String
) : BotEvent(), Packet