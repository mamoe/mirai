/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.HeartbeatFailureHandler
import net.mamoe.mirai.internal.network.components.HeartbeatScheduler
import net.mamoe.mirai.internal.network.framework.AbstractNettyNHTestWithSelector
import net.mamoe.mirai.internal.network.impl.netty.HeartbeatFailedException
import net.mamoe.mirai.internal.network.impl.netty.NettyChannelException
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertFails

internal class SelectorRecoveryTest : AbstractNettyNHTestWithSelector() {
    @Test
    fun `stop on manual close`() = runBlockingUnit {
        network.resumeConnection()
        network.close(IllegalStateException("Closed by test"))
        assertFails { network.resumeConnection() }
    }

    /**
     * Emulates system hibernation and network failure.
     * @see HeartbeatFailedException
     */
    @Test
    fun `can recover on heartbeat failure with IOException`() = runBlockingUnit {
        // We allow IOException to cause a reconnect.
        testRecoverWhenHeartbeatFailWith { IOException("test IO ex") }

        // BotOfflineMonitor immediately launches a recovery which is UNDISPATCHED, so connection is immediately recovered.
        assertState(NetworkHandler.State.CONNECTING, NetworkHandler.State.OK)
    }

    /**
     * Emulates system hibernation and network failure.
     * @see HeartbeatFailedException
     */
    @Test
    fun `can recover on heartbeat failure with NettyChannelException`() = runBlockingUnit {
        // We allow IOException to cause a reconnect.
        testRecoverWhenHeartbeatFailWith { NettyChannelException("test IO ex") }

        // BotOfflineMonitor immediately launches a recovery which is UNDISPATCHED, so connection is immediately recovered.
        assertState(NetworkHandler.State.CONNECTING, NetworkHandler.State.OK)
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
        bot.network.context[EventDispatcher].joinBroadcast()
        assertState(NetworkHandler.State.OK)

        heartbeatScheduler.onHeartFailure("Test", exception())
    }
}