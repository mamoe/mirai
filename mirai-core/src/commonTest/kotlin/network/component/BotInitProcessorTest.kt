/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.component

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.components.BotInitProcessor
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTestWithSelector
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPushForceOffline
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPushForceOffline
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BotInitProcessorTest {
    class WithoutSelector : AbstractCommonNHTest() {
        @Test
        fun `BotInitProcessor halted`() = runTest {
            val p = setComponent(BotInitProcessor, object : BotInitProcessor {
                var ranTimes = 0
                var haltedTimes = 0
                var def = CompletableDeferred<Unit>()
                override suspend fun init() {
                    ranTimes++
                    def.await()
                }

                override fun setLoginHalted() {
                    haltedTimes += 1
                }
            })
            assertTrue { network.isActive }
            network.setStateLoading(conn)
            assertEquals(1, p.ranTimes)
            assertEquals(0, p.haltedTimes)
            assertState(NetworkHandler.State.LOADING)
            network.collectReceived(
                IncomingPacket(
                    MessageSvcPushForceOffline.commandName,
                    RequestPushForceOffline(bot.uin)
                )
            )
            assertEquals(1, p.ranTimes)
            assertEquals(1, p.haltedTimes)
            eventDispatcher.joinBroadcast()
            assertFalse { network.isActive }
            network.assertState(NetworkHandler.State.CLOSED) // we do not use selector in this test so it will be CLOSED. It will recover (reconnect) instead in real.
        }
    }

    class WithSelector : AbstractCommonNHTestWithSelector() {
        @Test
        fun `BotInitProcessor halted`() = runBlockingUnit {
            bot.configuration.autoReconnectOnForceOffline = true
            val p = setComponent(BotInitProcessor, object : BotInitProcessor {
                var ranTimes = 0
                var haltedTimes = 0
                var def = CompletableDeferred<Unit>()
                override suspend fun init() {
                    ranTimes++
                    def.await()
                }

                override fun setLoginHalted() {
                    haltedTimes += 1
                }
            })
            assertTrue { network.isActive }
            network.setStateLoading(conn)
            assertEquals(1, p.ranTimes)
            assertEquals(0, p.haltedTimes)
            assertState(NetworkHandler.State.LOADING)

            network.currentInstance().collectReceived(
                IncomingPacket(
                    MessageSvcPushForceOffline.commandName,
                    RequestPushForceOffline(bot.uin)
                )
            )
            // all jobs launched during `collectReceived` are UNDISPATCHED, `collectReceived` returns on `def.await()` (suspension point)
            // first run is CANCELLED.


            assertEquals(1, p.ranTimes)
            assertEquals(1, p.haltedTimes)

            p.def.complete(Unit) // then BotInitProcessor.init finishes async.
            network.resumeConnection() // join async
            assertEquals(2, p.ranTimes) // we expect selector has run `init`
            assertEquals(1, p.haltedTimes)
        }
    }
}