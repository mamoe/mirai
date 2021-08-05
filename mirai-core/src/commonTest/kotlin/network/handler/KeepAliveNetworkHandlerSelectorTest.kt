/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(TestOnly::class)

package net.mamoe.mirai.internal.network.handler

import net.mamoe.mirai.internal.network.framework.AbstractMockNetworkHandlerTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.selector.AbstractKeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.TestOnly
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*
import kotlin.time.Duration

internal val selectorLogger = MiraiLogger.Factory.create(TestSelector::class, "selector")

internal class TestSelector<H : NetworkHandler> :
    AbstractKeepAliveNetworkHandlerSelector<H> {

    val createInstance0: () -> H

    constructor(createInstance0: () -> H) : super(logger = selectorLogger) {
        this.createInstance0 = createInstance0
    }

    constructor(maxAttempts: Int, createInstance0: () -> H) : super(maxAttempts, selectorLogger) {
        this.createInstance0 = createInstance0
    }

    val createInstanceCount: AtomicInteger = AtomicInteger(0)

    override fun createInstance(): H {
        createInstanceCount.incrementAndGet()
        return this.createInstance0()
    }
}

internal class KeepAliveNetworkHandlerSelectorTest : AbstractMockNetworkHandlerTest() {
    @Test
    fun `can initialize instance`() {
        val selector = TestSelector {
            createNetworkHandler().apply {
                setState(State.OK)
            }
        }
        runBlockingUnit(timeout = Duration.seconds(1)) { selector.awaitResumeInstance() }
        assertNotNull(selector.getCurrentInstanceOrNull())
    }

    @Test
    fun `no redundant initialization`() {
        val selector = TestSelector<NetworkHandler> {
            fail("initialize called")
        }
        val handler = createNetworkHandler()
        selector.setCurrent(handler)
        assertSame(handler, selector.getCurrentInstanceOrNull())
    }

    @Test
    fun `initialize another when closed`() {
        val selector = TestSelector {
            createNetworkHandler().apply { setState(State.OK) }
        }
        val handler = createNetworkHandler()
        selector.setCurrent(handler)
        assertSame(handler, selector.getCurrentInstanceOrNull())
        handler.setState(State.CLOSED)
        runBlockingUnit(timeout = Duration.seconds(3)) { selector.awaitResumeInstance() }
        assertEquals(1, selector.createInstanceCount.get())
    }

    @Test
    fun `limited attempts`() = runBlockingUnit {
        val selector = TestSelector(3) {
            createNetworkHandler().apply { setState(State.CLOSED) }
        }
        assertFailsWith<IllegalStateException> {
            selector.awaitResumeInstance()
        }
        assertEquals(3, selector.createInstanceCount.get())
    }
}