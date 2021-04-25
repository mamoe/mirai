/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.internal.contact.replaceMagicCodes
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.utils.verbose

@Deprecated("broad", ReplaceWith("this.broadcast()", "net.mamoe.mirai.event.broadcast"))
internal suspend fun <T : Event> T.broadcastWithBot(bot: Bot): T {
    this.let log@{ event ->
        val logger = bot.logger
        if (event is Packet.NoLog) return@log
        if (event is Packet.NoEventLog) return@log
        if (event is Packet.NoEventLog) {
            logger.verbose { "Recv: $event".replaceMagicCodes() }
        } else {
            logger.verbose { "Event: $event".replaceMagicCodes() }
        }
    }
    return broadcast()
}
