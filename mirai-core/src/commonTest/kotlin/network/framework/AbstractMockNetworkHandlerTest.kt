/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.internal.network.framework

import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.components.*
import net.mamoe.mirai.internal.network.framework.components.TestImagePatcher
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.state.LoggingStateObserver
import net.mamoe.mirai.internal.network.handler.state.SafeStateObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.utils.ImagePatcher
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.MiraiLogger
import network.framework.components.TestEventDispatcherImpl
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.assertEquals


/**
 * Test with mock [NetworkHandler], and without selector.
 */
internal abstract class AbstractMockNetworkHandlerTest : AbstractNetworkHandlerTest() {
    protected open fun createNetworkHandlerContext() = TestNetworkHandlerContext(bot, logger, components)
    protected open fun createNetworkHandler() = TestNetworkHandler(bot, createNetworkHandlerContext())

    protected open fun createAccount() = BotAccount(Random.nextLong().absoluteValue.mod(1000L), "pwd")

    protected val bot: QQAndroidBot by lazy {
        MockBot(createAccount()) {
            nhProvider = { createNetworkHandler() }
            additionalComponentsProvider = { this@AbstractMockNetworkHandlerTest.components }
        }
    }
    protected val logger = MiraiLogger.Factory.create(Bot::class, "test")

    private val eventDispatcherJob = SupervisorJob()

    @AfterTest
    private fun cancelJob() {
        eventDispatcherJob.cancel()
    }

    protected val components = ConcurrentComponentStorage().apply {
        set(SsoProcessor, TestSsoProcessor(bot))
        set(
            EventDispatcher,
            // Note that in real we use 'bot.coroutineContext', but here we override with a new, independent job
            // to allow BotOfflineEvent.Active to be broadcast and joinBroadcast works even if bot coroutineScope is closed.
            TestEventDispatcherImpl(
                bot.coroutineContext + eventDispatcherJob,
                bot.logger.subLogger("TestEventDispatcherImpl")
            )
        )
        set(
            StateObserver,
            SafeStateObserver(
                LoggingStateObserver(MiraiLogger.Factory.create(LoggingStateObserver::class, "States")),
                MiraiLogger.Factory.create(SafeStateObserver::class, "StateObserver errors")
            )
        )
        set(ImagePatcher, TestImagePatcher())
        set(PacketLoggingStrategy, PacketLoggingStrategyImpl(bot))
        set(AccountSecretsManager, MemoryAccountSecretsManager())
        set(HttpClientProvider, HttpClientProviderImpl())
    }

    fun NetworkHandler.assertState(state: NetworkHandler.State) {
        assertEquals(this.state, state)
    }
}