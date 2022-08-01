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

import net.mamoe.mirai.internal.network.components.FirstLoginResult
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.framework.PlatformConn
import net.mamoe.mirai.internal.network.framework.TestCommonNetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.MaxAttemptsReachedException
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.TestOnly
import kotlin.test.*

internal class KeepAliveNetworkHandlerSelectorRealTest : AbstractCommonNHTest() {

    internal class FakeFailOnCreatingConnection : AbstractCommonNHTest() {
        private class MyException : Exception()

        private lateinit var throwException: () -> Nothing

        override val factory: NetworkHandlerFactory<TestCommonNetworkHandler> =
            NetworkHandlerFactory<TestCommonNetworkHandler> { context, address ->
                object : TestCommonNetworkHandler(bot, context, address) {
                    override suspend fun createConnection(): PlatformConn {
                        throwException()
                    }
                }
            }

        @Test
        fun `should not tolerant any exception except NetworkException thrown by states`() = runBlockingUnit {
            // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.
            throwException = {
                throw MyException()
            }

            val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
            assertFailsWith<Throwable> { selector.awaitResumeInstance() }
        }

        // Since #1963, any error during first login will close the bot. So we assume first login succeed to do our test.
        @BeforeTest
        private fun setFirstLoginPassed() {
            assertEquals(null, bot.components[SsoProcessor].firstLoginResult.value)
            bot.components[SsoProcessor].firstLoginResult.value = FirstLoginResult.PASSED
        }

        @Test
        fun `should tolerant NetworkException thrown by states`() = runBlockingUnit {
            // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.
            throwException = {
                throw object : NetworkException(true) {}
            }

            val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
            assertFailsWith<MaxAttemptsReachedException> { selector.awaitResumeInstance() }.let {
                assertIs<NetworkException>(it.cause)
            }
        }

        @Test
        fun `throws MaxAttemptsReachedException with cause of original`() = runBlockingUnit {
            throwException = {
                throw MyException()
            }
            val selector = TestSelector(3) { factory.create(createContext(), createAddress()) }
            assertFailsWith<MaxAttemptsReachedException> { selector.awaitResumeInstance() }.let {
                assertIs<MyException>(it.cause)
            }
        }
    }

}