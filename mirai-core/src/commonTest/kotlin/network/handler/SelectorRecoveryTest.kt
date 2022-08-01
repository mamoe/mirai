/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.HeartbeatFailureHandler
import net.mamoe.mirai.internal.network.components.HeartbeatScheduler
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTestWithSelector
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

/**
 * Test whether the selector can recover the connection after first successful login.
 */
internal class SelectorRecoveryTest : AbstractCommonNHTestWithSelector() {
//    @BeforeTest
//    fun beforeTest(info: TestInfo) {
//        println("=".repeat(30) + "BEGIN: ${info.displayName}" + "=".repeat(30))
//    }
//
//    @AfterTest
//    fun afterTest(info: TestInfo) {
//        println("=".repeat(31) + "END: ${info.displayName}" + "=".repeat(31))
//    }

    @Test
    fun `stop on manual close`() = runBlockingUnit {
        network.resumeConnection()
        network.close(IllegalStateException("Closed by test"))
        assertFails { network.resumeConnection() }
    }

    /**
     * Emulates system hibernation and network failure.
     */
    @Test
    fun `can recover on heartbeat failure with NettyChannelException`() = runBlockingUnit {
        // We allow NetworkException to cause a reconnect.
        testRecoverWhenHeartbeatFailWith { NetworkException("test IO ex", true) }

        bot.components[EventDispatcher].joinBroadcast() // Wait our async connector to complete.

        // BotOfflineMonitor immediately launches a recovery which is UNDISPATCHED, so connection is immediately recovered.
        assertTrue { bot.network.state != NetworkHandler.State.CLOSED }
    }

    @Test
    fun `cannot recover on other failures`() = runBlockingUnit {
        // ISE is considered as an internal error (bug).
        testRecoverWhenHeartbeatFailWith { IllegalStateException() }

        assertState(NetworkHandler.State.CLOSED)
    }

    private suspend fun testRecoverWhenHeartbeatFailWith(exception: () -> Exception) {
        val heartbeatScheduler = object : HeartbeatScheduler {
            lateinit var onHeartFailure: HeartbeatFailureHandler
            override fun launchJobsIn(
                network: NetworkHandlerSupport,
                scope: CoroutineScope,
                onHeartFailure: HeartbeatFailureHandler,
            ): List<Job> {
                this.onHeartFailure = onHeartFailure
                return listOf(Job())
            }
        }
        overrideComponents[HeartbeatScheduler] = heartbeatScheduler

        bot.login()
        // Now first login succeed.
        bot.network.context[EventDispatcher].joinBroadcast()
        assertState(NetworkHandler.State.OK)

        println("1")
        heartbeatScheduler.onHeartFailure("Test", exception())
        println("2")
    }
}