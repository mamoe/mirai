/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.handler

import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.internal.network.components.FirstLoginResult
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.framework.AbstractRealNetworkHandlerTest
import net.mamoe.mirai.internal.network.framework.TestCommonNetworkHandler
import net.mamoe.mirai.internal.network.framework.components.TestSsoProcessor
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class SelectorLoginRecoveryTest :
    AbstractRealNetworkHandlerTest<SelectorNetworkHandler<TestCommonNetworkHandler>>() {

    // does not use selector
    override val factory: NetworkHandlerFactory<SelectorNetworkHandler<TestCommonNetworkHandler>> =
        NetworkHandlerFactory<SelectorNetworkHandler<TestCommonNetworkHandler>> { context, address ->
            SelectorNetworkHandler(TestSelector {
                object : TestCommonNetworkHandler(bot, context, address) {
                }
            })
        }

    /**
     * 登录时遇到未知错误, [WtLogin] 会抛 [IllegalStateException] (即抛不可挽救的异常),
     * selector 应该停止登录, 不要重试, 重新抛出捕获的异常.
     */
    @Test
    fun `rethrow exception caught during Bot_login`() = runTest {
        val exceptionMessage = "Login failed!"
        setComponent(SsoProcessor, object : TestSsoProcessor(bot) {
            override suspend fun login(handler: NetworkHandler) {
                throw IllegalStateException(exceptionMessage)
            }
        })

        assertFailsWith<IllegalStateException> {
            bot.login()
        }.let { e ->
            assertEquals(exceptionMessage, e.message)
        }
    }

    /**
     * 登录时遇到未知错误, [WtLogin] 会抛 [IllegalStateException] (即抛不可挽救的异常),
     * selector 应该 close Bot, 不要 logout, 要重新抛出捕获的异常.
     */
    @Test
    fun `do not call logout when closing bot due to failed to login`() = runTest {
        val exceptionMessage = "Login failed!"
        setComponent(SsoProcessor, object : TestSsoProcessor(bot) {
            override suspend fun login(handler: NetworkHandler) {
                throw IllegalStateException(exceptionMessage)
            }

            override suspend fun logout(handler: NetworkHandler) {
                if (firstLoginSucceed) {
                    throw AssertionError("Congratulations! You called logout!")
                }
            }
        })

        assertEquals(null, bot.components[SsoProcessor].firstLoginResult)
        bot.components[SsoProcessor].setFirstLoginResult(null)
        assertFailsWith<IllegalStateException> {
            bot.login()
        }.let { e ->
            assertEquals(exceptionMessage, e.message)
        }
        assertEquals(FirstLoginResult.OTHER_FAILURE, bot.components[SsoProcessor].firstLoginResult)
        bot.close()
    }
}