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
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.test.assertFailsWith

internal class NettyBotNormalLoginTest : AbstractNettyNHTest() {
    class CusLoginException(message: String?) : RuntimeException(message)

    @Test
    fun `test login fail`() = runBlockingUnit {
        setSsoProcessor { throw CusLoginException("A") }
        assertFailsWith<CusLoginException>("A") { bot.login() }
    }

    @Test
    fun `test network broken`() = runBlockingUnit {
        setSsoProcessor {
            eventDispatcher.joinBroadcast()
            channel.pipeline().fireExceptionCaught(IOException("TestNetworkBroken"))
            awaitCancellation() // receive exception from "network"
        }
        assertFailsWith<IOException>("TestNetworkBroken") {
            bot.login()
        }
    }
}
