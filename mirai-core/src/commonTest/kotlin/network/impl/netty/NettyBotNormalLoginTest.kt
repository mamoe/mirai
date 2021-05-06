/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.nextEvent
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.firstIsInstanceOrNull
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class NettyBotNormalLoginTest : AbstractNettyNHTest() {
    class CusLoginException(message: String?) : RuntimeException(message)

    @Test
    fun `test login fail`() = runBlockingUnit {
        setSsoProcessor { throw CusLoginException("A") }
        assertFailsWith<CusLoginException>("A") { bot.login() }
    }

    @Test
    fun `test network broken`() = runBlockingUnit {
        setSsoProcessor {
            delay(1000)
            channel.pipeline().fireExceptionCaught(IOException("TestNetworkBroken"))
            delay(100000) // receive bits from "network"
        }
        assertFailsWith<IOException>("TestNetworkBroken") {
            bot.login()
        }
    }

    @Test
    fun `test errors after logon`() = runBlockingUnit {
        bot.login()
        delay(1000)
        assertEventBroadcasts<BotEvent>(-1) {
            launch {
                delay(1000)
                channel.pipeline().fireExceptionCaught(CusLoginException("Net error"))
            }
            assertNotNull(
                nextEvent<BotReloginEvent>(5000) { it.bot === bot }
            )
        }.let { events ->
            assertFailsWith<CusLoginException>("Net error") {
                throw events.firstIsInstanceOrNull<BotOfflineEvent.Dropped>()!!.cause!!
            }
        }
        assertState(NetworkHandler.State.OK)
    }
}
