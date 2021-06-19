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

import io.netty.channel.Channel
import net.mamoe.mirai.internal.network.impl.netty.AbstractNettyNHTest
import net.mamoe.mirai.internal.network.impl.netty.TestNettyNH
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.utils.TestOnly
import org.junit.jupiter.api.assertThrows
import java.net.SocketAddress
import kotlin.test.Test

internal class KeepAliveNetworkHandlerSelectorRealTest : AbstractNettyNHTest() {

    internal class FakeFailOnCreatingConnection : AbstractNettyNHTest() {
        private class MyException : Exception()

        override val factory: NetworkHandlerFactory<TestNettyNH> = object : NetworkHandlerFactory<TestNettyNH> {
            override fun create(context: NetworkHandlerContext, address: SocketAddress): TestNettyNH {
                return object : TestNettyNH(bot, context, address) {
                    override suspend fun createConnection(decodePipeline: PacketDecodePipeline): Channel =
                        throw MyException()
                }
            }
        }

        @Test
        fun `should not tolerant any exception thrown by states`() = runBlockingUnit {
            // selector should not tolerant any exception during state initialization, or in the Jobs launched in states.

            val selector = TestSelector(3) { createHandler() }
            assertThrows<Throwable> { selector.awaitResumeInstance() }
        }

        @Test
        fun `should unwrap exception`() = runBlockingUnit {
            val selector = TestSelector(3) { createHandler() }
            assertThrows<MyException> { selector.awaitResumeInstance() }
        }
    }

}