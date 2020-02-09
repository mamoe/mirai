/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.events.BotEvent

/**
 * 被挤下线
 */
data class ForceOfflineEvent(
    override val bot: Bot,
    val title: String,
    val tips: String
) : BotEvent(), Packet