/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.impl.common

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State.*
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.*

internal class BotLifecycleTest : AbstractCommonNHTest() {


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
            networkHandlerProvider { factory.create(createContext(), createAddress()) }
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
            networkHandlerProvider { factory.create(createContext(), createAddress()) }
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
        network.coroutineContext.job.join()
        network.assertState(CLOSED) // we do not use selector in this test so it will be CLOSED. It will recover (reconnect) instead in real.
        assertFalse { network.isActive }
    }

    @Test // #2266
    fun `no StackOverflowError on Bot close`() = runTest {
        assertTrue { network.isActive }
        bot.login()
        assertTrue { network.isActive }
        overrideComponents[SsoProcessor] = object : TestSsoProcessor(bot) {
            override suspend fun logout(handler: NetworkHandler) {
                bot.close()
            }
        }
        bot.close()
        network.assertState(CLOSED)
        assertFalse { network.isActive }
    }


    @Test
    fun `isOnline returns false if network not initialized`() = runBlockingUnit {
        assertFalse { bot.isOnline }
    }
}