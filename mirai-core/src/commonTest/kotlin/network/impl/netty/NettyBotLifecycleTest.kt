/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractNettyNHTest
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.supervisorJob
import kotlin.test.*

internal class NettyBotLifecycleTest : AbstractNettyNHTest() {


    // not allowed anymore

    //    @Test
    //    fun `closed on Force offline with BotOfflineEventMonitor`() = runBlockingUnit {
    //        defaultComponents[BotOfflineEventMonitor] = BotOfflineEventMonitorImpl()
    //        bot.login()
    //        assertState(OK)
    //        BotOfflineEvent.Force(bot, "test", "test").broadcast()
    //        assertState(CLOSED)
    //        assertFalse { network.isActive }
    //        assertTrue { bot.isActive }
    //    }

    //    @Test
    //    fun `closed on Active offline with BotOfflineEventMonitor`() = runBlockingUnit {
    //        defaultComponents[BotOfflineEventMonitor] = BotOfflineEventMonitorImpl()
    //        bot.login()
    //        assertState(OK)
    //        BotOfflineEvent.Active(bot, null).broadcast()
    //        assertState(CLOSED)
    //        assertFalse { network.isActive }
    //        assertTrue { bot.isActive }
    //    }

    @Test
    fun `state is CLOSED after Bot close`() = runBlockingUnit {
        bot.network.assertState(INITIALIZED)
        bot.login()
        bot.network.assertState(OK)
        bot.closeAndJoin()
        bot.network.assertState(CLOSED)
        bot.components[EventDispatcher].joinBroadcast()
        bot.network.assertState(CLOSED)
    }


    @Test
    fun `send logout on exit`() = runBlockingUnit {
        assertIs<TestSsoProcessor>(bot.components[SsoProcessor])
        bot.network.assertState(INITIALIZED)
        bot.login()
        bot.network.assertState(OK)
        bot.closeAndJoin() // send logout blocking
        eventDispatcher.joinBroadcast()
        bot.network.assertState(CLOSED)
        assertTrue { nhEvents.any { it is NHEvent.Logout } }
    }

    @Test
    fun `can override context`() = runBlockingUnit {
        bot = MockBot {
            conf {
                parentCoroutineContext = CoroutineName("Overrode")
            }
            networkHandlerProvider { createHandler() }
        }
        assertEquals("Overrode", bot.coroutineContext[CoroutineName]!!.name)
    }

    @Test
    fun `job attached`() = runBlockingUnit {
        val parentJob = SupervisorJob()
        bot = MockBot {
            conf {
                parentCoroutineContext = parentJob
            }
            networkHandlerProvider { createHandler() }
        }
        assertEquals(1, parentJob.children.count())
        assertEquals(bot.supervisorJob, parentJob.children.first())
    }

    @Test
    fun `network scope closed on bot close`() = runBlockingUnit {
        assertTrue { network.isActive }
        bot.closeAndJoin()
        assertFalse { network.isActive }
    }

    @Test
    fun `network closed on SimpleGet Error`() = runBlockingUnit {
        assertTrue { network.isActive }
        bot.login()
        assertTrue { network.isActive }
        network.collectReceived(
            IncomingPacket(
                commandName = StatSvc.SimpleGet.commandName,
                sequenceId = 1,
                data = StatSvc.SimpleGet.Response.Error(1, "test error"),
            )
        )
        assertFalse { network.isActive }
        network.assertState(CLOSED) // we do not use selector in this test so it will be CLOSED. It will recover (reconnect) instead in real.
    }


    @Test
    fun `isOnline returns false if network not initialized`() = runBlockingUnit {
        assertFalse { bot.isOnline }
    }
}