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
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.eventDispatcher
import net.mamoe.mirai.internal.network.framework.ssoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.test.assertEventBroadcasts
import net.mamoe.mirai.internal.test.assertEventNotBroadcast
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals


@Ignore
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class NettyHandlerEventTest : AbstractNettyNHTest() {
    @Test
    fun `BotOnlineEvent after successful logon`() = runBlockingUnit {
        assertEventBroadcasts<BotOnlineEvent> {
            assertEquals(INITIALIZED, network.state)
            bot.login() // launches a job which broadcasts the event
            eventDispatcher.joinBroadcast()
            assertEquals(OK, network.state)
        }
    }


    @Test
    fun `BotOnlineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(INITIALIZED, network.state)
        bot.login()
        bot.components[SsoProcessor].firstLoginSucceed = true
        assertEquals(OK, network.state)
        eventDispatcher.joinBroadcast() // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOnlineEvent>(1) {
            network.setStateConnecting()
            network.resumeConnection()
            eventDispatcher.joinBroadcast()
            assertEquals(OK, network.state)
        }
    }

    @Test
    fun `BotOfflineEvent after successful reconnection`() = runBlockingUnit {
        assertEquals(INITIALIZED, network.state)
        bot.login()
        bot.components[SsoProcessor].firstLoginSucceed = true
        assertEquals(OK, network.state)
        eventDispatcher.joinBroadcast() // `login` launches a job which broadcasts the event
        assertEventBroadcasts<BotOfflineEvent>(1) {
            network.setStateClosed()
            eventDispatcher.joinBroadcast()
            assertState(CLOSED)
        }
    }


    @Test
    fun `from CONNECTING TO OK the first time`() = runBlockingUnit {
        val ok = CompletableDeferred<Unit>()
        setSsoProcessor {
            ok.join()
        }
        assertState(INITIALIZED)
        network.setStateConnecting()
        assertEventBroadcasts<Event>(1) {
            ok.complete(Unit)
            network.resumeConnection()
            eventDispatcher.joinBroadcast()
        }.let { event ->
            assertEquals(BotOnlineEvent::class, event[0]::class)
        }
    }

    @Test
    fun `from CONNECTING TO OK the second time`() = runBlockingUnit {
        val ok = AtomicReference(CompletableDeferred<Unit>())
        setSsoProcessor {
            ok.get().join()
        }

        assertState(INITIALIZED)

        network.setStateConnecting()
        ok.get().complete(Unit)
        network.resumeConnection()
        assertState(OK)

        ok.set(CompletableDeferred())
        network.setStateConnecting()
        eventDispatcher.joinBroadcast()
        println("Starting receiving events")
        assertEventBroadcasts<Event>(2) {
            ok.get().complete(Unit)
            network.resumeConnection()
            eventDispatcher.joinBroadcast()
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
        eventDispatcher.joinBroadcast() // `login` launches a job which broadcasts the event
        assertEventBroadcasts<Event>(1) {
            network.close(null)
            assertState(CLOSED)
            eventDispatcher.joinBroadcast()
        }.let { event ->
            assertEquals(BotOfflineEvent.Active::class, event[0]::class)
        }
    }

    @Test
    fun `no BotOffline from CONNECTING TO CLOSED`() = runBlockingUnit {
        network.setStateConnecting()
        eventDispatcher.joinBroadcast() // `login` launches a job which broadcasts the event
        assertEventNotBroadcast {
            network.setStateClosed()
            network.resumeConnection()
            eventDispatcher.joinBroadcast()
        }
    }

    @Test
    fun `BotReloginEvent after successful reconnection`() = runBlockingUnit {
        assertEventBroadcasts<BotReloginEvent> {
            assertState(INITIALIZED)
            bot.login()
            assertState(OK)
            network.ssoProcessor.firstLoginSucceed = true
            network.setStateConnecting()
            network.resumeConnection()
            assertState(OK)
            network.eventDispatcher.joinBroadcast()
            assertState(OK)
        }
    }

//
//    @Test
//    fun `no event from INITIALIZED TO OK`() = runBlockingUnit {
//        assertState(INITIALIZED)
//        bot.login()
//        assertState(OK)
//        network.setStateConnecting()
//        eventDispatcher.joinBroadcast() // `login` launches a job which broadcasts the event
//        assertEventBroadcasts<Event>(0) {
//            network.resumeConnection()
//            eventDispatcher.joinBroadcast()
//        }
//    }
}