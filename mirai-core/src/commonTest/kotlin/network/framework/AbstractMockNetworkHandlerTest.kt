/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.internal.network.framework

import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.state.LoggingStateObserver
import net.mamoe.mirai.internal.network.handler.state.SafeStateObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.subLogger
import net.mamoe.mirai.utils.MiraiLogger
import network.framework.components.TestEventDispatcherImpl
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal abstract class AbstractMockNetworkHandlerTest : AbstractTest() {
    protected open fun createNetworkHandlerContext() = TestNetworkHandlerContext(bot, logger, components)
    protected open fun createNetworkHandler() = TestNetworkHandler(bot, createNetworkHandlerContext())

    protected val bot: QQAndroidBot = MockBot {
        nhProvider = { createNetworkHandler() }
        additionalComponentsProvider = { this@AbstractMockNetworkHandlerTest.components }
    }
    protected val logger = MiraiLogger.create("test")
    protected val components = ConcurrentComponentStorage().apply {
        set(SsoProcessor, TestSsoProcessor(bot))
        set(
            EventDispatcher,
            TestEventDispatcherImpl(bot.coroutineContext, bot.logger.subLogger("TestEventDispatcherImpl"))
        )
        set(
            StateObserver,
            SafeStateObserver(
                LoggingStateObserver(MiraiLogger.create("States")),
                MiraiLogger.create("StateObserver errors")
            )
        )
    }

    fun NetworkHandler.assertState(state: NetworkHandler.State) {
        assertEquals(state, state)
    }
}