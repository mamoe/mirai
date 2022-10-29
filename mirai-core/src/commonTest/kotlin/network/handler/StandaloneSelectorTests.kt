/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.network.components.FirstLoginResult
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.PlatformConn
import net.mamoe.mirai.internal.network.framework.TestCommonNetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.MaxAttemptsReachedException
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.AtomicInteger
import kotlin.test.*

/**
 * Every function can have its own selector.
 */
internal class StandaloneSelectorTests : AbstractCommonNHTest() {
    private class MyException : Exception() {
        override fun toString(): String {
            return "MyException"
        }
    }

    /**
     * This simulates an error on [NetworkHandler.State.CONNECTING]
     */
    private var throwExceptionOnConnecting: (() -> Nothing)? = null

    // does not use selector
    override val factory: NetworkHandlerFactory<TestCommonNetworkHandler> =
        NetworkHandlerFactory<TestCommonNetworkHandler> { context, address ->
            object : TestCommonNetworkHandler(bot, context, address) {
                override suspend fun createConnection(): PlatformConn {
                    return throwExceptionOnConnecting?.invoke() ?: PlatformConn()
                }
            }
        }

    @Test
    fun `should not tolerant any exception except NetworkException thrown by states`() = runBlockingUnit {
        // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.
        throwExceptionOnConnecting = {
            throw MyException()
        }

        val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
        assertFailsWith<Throwable> { selector.awaitResumeInstance() }
    }

    // Since #1963, any error during first login will close the bot. So we assume first login succeed to do our test.
    @BeforeTest
    private fun setFirstLoginPassed() {
        assertEquals(null, bot.components[SsoProcessor].firstLoginResult)
        bot.components[SsoProcessor].setFirstLoginResult(FirstLoginResult.PASSED)
    }

    @Test
    fun `NetworkException can suggest retrying`() = runBlockingUnit {
        // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.
        throwExceptionOnConnecting = {
            throw object : NetworkException(true) {}
        }

        val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
        assertFailsWith<MaxAttemptsReachedException> { selector.awaitResumeInstance() }.let {
            assertIs<NetworkException>(it.cause)
        }
    }

    @Test
    fun `NetworkException does not cause retrying if recoverable=false`() = runBlockingUnit {
        // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.
        val times = AtomicInteger(0)
        val theException = object : NetworkException(false) {}
        throwExceptionOnConnecting = {
            times.incrementAndGet()
            throw theException
        }

        val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
        assertFailsWith<NetworkException> { selector.awaitResumeInstance() }.let {
            assertEquals(1, times.value)
            assertSame(theException, it)
        }
    }

    @Test
    fun `other exceptions considered as internal error and does not trigger reconnect`() = runBlockingUnit {
        throwExceptionOnConnecting = {
            throw MyException()
        }
        val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
        assertFailsWith<MyException> { selector.awaitResumeInstance() }
    }
}