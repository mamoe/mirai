/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import net.mamoe.mirai.internal.network.framework.AbstractNettyNHTest
import net.mamoe.mirai.internal.network.framework.setSsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.KeepAliveNetworkHandlerSelector
import net.mamoe.mirai.internal.network.handler.selector.SelectorNetworkHandler
import net.mamoe.mirai.internal.network.handler.selectorLogger
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.network.CustomLoginFailedException
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

internal class NettyBotNormalLoginTest : AbstractNettyNHTest() {
    val selector = KeepAliveNetworkHandlerSelector(selectorLogger) {
        super.factory.create(createContext(), createAddress())
    }

    override fun createHandler(): NetworkHandler {
        return SelectorNetworkHandler(selector)
    }

    class CusLoginException(message: String?) : CustomLoginFailedException(true, message)

    @Test
    fun `test login fail`() = runBlockingUnit {
        setSsoProcessor { throw CusLoginException("A") }
        assertFailsWith<CusLoginException>("A") { bot.login() }
        assertFalse(bot.isActive)
    }

    @Test
    fun `test network broken`() = runBlockingUnit {
        var retryCounter = 0
        setSsoProcessor {
            eventDispatcher.joinBroadcast()
            if (retryCounter++ >= 15) {
                return@setSsoProcessor
            }
            channel.pipeline().fireExceptionCaught(IOException("TestNetworkBroken"))
            awaitCancellation() // receive exception from "network"
        }
        bot.login()
    }
}
