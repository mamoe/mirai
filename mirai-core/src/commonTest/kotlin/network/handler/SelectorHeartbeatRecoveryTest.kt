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
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTestWithSelector
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.protocol.packet.login.StatSvc
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.network.NoServerAvailableException
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.*

/**
 * Test whether the selector can recover the connection after first successful login.
 */
internal class SelectorHeartbeatRecoveryTest : AbstractCommonNHTestWithSelector() {
//    @BeforeTest
//    fun beforeTest(info: TestInfo) {
//        println("=".repeat(30) + "BEGIN: ${info.displayName}" + "=".repeat(30))
//    }
//
//    @AfterTest
//    fun afterTest(info: TestInfo) {
//        println("=".repeat(31) + "END: ${info.displayName}" + "=".repeat(31))
//    }

    /**
     * @see NetworkHandler.State.CONNECTING
     */
    var throwExceptionOnLogin: (() -> Unit)? = null

    init {
        overrideComponents[SsoProcessor] = object : TestSsoProcessor(bot) {
            private val delegate = overrideComponents[SsoProcessor]
            override suspend fun login(handler: NetworkHandler) {
                delegate.login(handler)
                throwExceptionOnLogin?.invoke()
            }
        }.apply {
            setFirstLoginResult(FirstLoginResult.PASSED)
        }
    }

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
    fun `can recover on LoginFailedException with killBot=false`() = runBlockingUnit {
        throwExceptionOnLogin = {
            if (selector.createdInstanceCount != 3) {
                throw NoServerAvailableException(null)
            }
        }

        bot.login()
        eventDispatcher.joinBroadcast()
        assertState(NetworkHandler.State.OK)
        assertEquals(3, selector.createdInstanceCount)
    }

    @Test
    fun `can recover on LoginFailedException with killBot=true`() = runBlockingUnit {
        throwExceptionOnLogin = {
            throw WrongPasswordException("Congratulations! Your bot has been blocked!")
        }

        assertFailsWith<WrongPasswordException> { bot.login() }
        eventDispatcher.joinBroadcast()
        assertState(NetworkHandler.State.CLOSED)
        assertEquals(1, selector.createdInstanceCount)
    }

    @Test
    fun `can recover on MsfOffline but fail and close bot on next login`() = runBlockingUnit {
        bot.login()
        firstLoginResult = FirstLoginResult.PASSED
        eventDispatcher.joinBroadcast()
        // Now first login succeed, and all events have been processed.
        assertState(NetworkHandler.State.OK)

        // Assume bot is blocked.
        throwExceptionOnLogin = {
            throw WrongPasswordException("Congratulations! Your bot has been blocked!")
        }
        // When blocked, server sends this event.
        eventDispatcher.broadcast(BotOfflineEvent.MsfOffline(bot, StatSvc.ReqMSFOffline.MsfOfflineToken(bot.uin, 1, 1)))
        eventDispatcher.joinBroadcast() // Sync for processing the async recovery launched by [BotOfflineEventMonitor]

        // Now we should expect
        assertState(NetworkHandler.State.CLOSED)
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