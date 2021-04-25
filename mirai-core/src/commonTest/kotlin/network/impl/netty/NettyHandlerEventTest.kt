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
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.INITIALIZED
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.OK
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.seconds


internal class NettyHandlerEventTest : AbstractNettyNHTest() {
    @Test
    fun `BotOnlineEvent after successful logon`() = runBlockingUnit {
        assertEventBroadcasts<BotOnlineEvent> {
            assertEquals(INITIALIZED, network.state)
            bot.login() // launches a job which broadcasts the event
            delay(3.seconds)
            assertEquals(OK, network.state)
        }
    }

    @Test
    fun `BotReloginEvent after successful reconnection`() = runBlockingUnit {
        assertEventBroadcasts<BotReloginEvent> {
            assertEquals(INITIALIZED, network.state)
            bot.login()
            bot.firstLoginSucceed = true
            network.setStateConnecting()
            network.resumeConnection()
            delay(3.seconds) // `login` launches a job which broadcasts the event
            assertEquals(OK, network.state)
        }
    }

    @Test
    fun `BotOnlineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(INITIALIZED, network.state)
        bot.login()
        bot.firstLoginSucceed = true
        assertEquals(OK, network.state)
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOnlineEvent>(1) {
            network.setStateConnecting()
            network.resumeConnection()
            delay(3.seconds)
            assertEquals(OK, network.state)
        }
    }

    @Test
    fun `BotOfflineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(INITIALIZED, network.state)
        bot.login()
        bot.firstLoginSucceed = true
        assertEquals(OK, network.state)
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOfflineEvent>(1) {
            network.setStateClosed()
            delay(3.seconds)
            assertEquals(State.CLOSED, network.state)
        }
    }


    private fun noEventOn(setState: () -> Unit) = runBlockingUnit {
        assertState(INITIALIZED)
        bot.login()
        bot.firstLoginSucceed = true
        assertState(OK)
        network.setStateConnecting()
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<Event>(0) {
            setState()
            delay(3.seconds)
        }
    }

    @Test
    fun `no event from CONNECTING TO CLOSED`() = noEventOn { network.setStateConnecting() }

    @Test
    fun `no event from CLOSED TO CLOSED`() = noEventOn { network.setStateClosed() }

    @Test
    fun `no event from INITIALIZED TO CLOSED`() = noEventOn { }
}