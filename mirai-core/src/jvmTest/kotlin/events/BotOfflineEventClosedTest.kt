/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.events

import kotlinx.coroutines.delay
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.AbstractTestWithMiraiImpl
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test

internal class BotOfflineEventClosedTest : AbstractTestWithMiraiImpl() {
    private val bot = MockBot()
    private var closedCheck = false

    @Test
    fun close() = runBlockingUnit {
        val event = bot.eventChannel.subscribeOnce<BotOfflineEvent.Closed> {
            println("closed event checked")
            delay(5000) // 5s, mock BIO
            println(this.bot.nick)
            closedCheck = true
        }
        // bot.login() cant really login
        bot.close()
        event.join()
        assert(closedCheck)
    }
}