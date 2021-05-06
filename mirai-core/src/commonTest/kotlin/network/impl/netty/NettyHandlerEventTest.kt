/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.INITIALIZED
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.OK
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.seconds


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
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
            bot.components[SsoProcessor].firstLoginSucceed = true
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
        bot.components[SsoProcessor].firstLoginSucceed = true
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
        bot.components[SsoProcessor].firstLoginSucceed = true
        assertEquals(OK, network.state)
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOfflineEvent>(1) {
            network.setStateClosed()
            delay(3.seconds)
            assertEquals(State.CLOSED, network.state)
        }
    }


    @Test
    fun `from OK TO CONNECTING`() = runBlockingUnit {
        defaultComponents[SsoProcessor] = object : SsoProcessor by defaultComponents[SsoProcessor] {
            override suspend fun login(handler: NetworkHandler) = awaitCancellation() // never ends
        }
        assertState(INITIALIZED)
        network.setStateOK(channel)
        delay(2.seconds) // ignore events
        assertEventBroadcasts<Event>(1) {
            network.setStateConnecting()
            delay(2.seconds)
        }.let { event ->
            assertEquals(BotOfflineEvent.Dropped::class, event[0]::class)
        }
    }

    @Test
    fun `from CONNECTING TO OK the first time`() = runBlockingUnit {
        val ok = CompletableDeferred<Unit>()
        defaultComponents[SsoProcessor] = object : SsoProcessor by defaultComponents[SsoProcessor] {
            override suspend fun login(handler: NetworkHandler) = ok.join()
        }
        assertState(INITIALIZED)
        network.setStateConnecting()
        assertEventBroadcasts<Event>(1) {
            ok.complete(Unit)
            network.resumeConnection()
            delay(2000)
        }.let { event ->
            assertEquals(BotOnlineEvent::class, event[0]::class)
        }
    }

    @Test
    fun `from CONNECTING TO OK the second time`() = runBlockingUnit {
        val ok = AtomicReference(CompletableDeferred<Unit>())
        defaultComponents[SsoProcessor] = object : SsoProcessor by defaultComponents[SsoProcessor] {
            override suspend fun login(handler: NetworkHandler) = ok.get().join()
        }

        assertState(INITIALIZED)

        network.setStateConnecting()
        ok.get().complete(Unit)
        network.resumeConnection()
        assertState(OK)

        ok.set(CompletableDeferred())
        network.setStateConnecting()
        delay(2000)
        println("Starting receiving events")
        assertEventBroadcasts<Event>(2) {
            ok.get().complete(Unit)
            network.resumeConnection()
            delay(2000)
        }.let { event ->
            assertEquals(BotOnlineEvent::class, event[0]::class)
            assertEquals(BotReloginEvent::class, event[1]::class)
        }
    }


    @Test
    fun testPreconditions() = runBlockingUnit {
        assertEventBroadcasts<Event>(1) { BotOfflineEvent.Active(bot, null).broadcast() }
    }

    @Test
    fun `BotOffline from OK TO CLOSED`() = runBlockingUnit {
        bot.login()
        assertState(OK)
        delay(3.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<Event>(1) {
            network.close(null)
            delay(3.seconds)
        }.let { event ->
            assertEquals(BotOfflineEvent.Active::class, event[0]::class)
        }
    }

    @Test
    fun `BotOffline from CONNECTING TO CLOSED`() = runBlockingUnit {
        network.setStateConnecting()
        delay(2.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<Event>(1) {
            network.setStateClosed()
            network.resumeConnection()
            delay(2.seconds)
        }.let { event ->
            assertEquals(BotOfflineEvent.Active::class, event[0]::class)
        }
    }

    @Test
    fun `no event from INITIALIZED TO OK`() = runBlockingUnit {
        assertState(INITIALIZED)
        bot.login()
        bot.components[SsoProcessor].firstLoginSucceed = true
        assertState(OK)
        network.setStateConnecting()
        delay(2.seconds) // `login` launches a job which broadcasts the event
        assertEventBroadcasts<Event>(0) {
            network.resumeConnection()
            delay(2.seconds)
        }
    }
}