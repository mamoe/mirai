/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import io.netty.channel.DefaultChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.internal.network.net.impl.netty.awaitKt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.test.fail

internal class NettyTestUnit {
    private val channel = EmbeddedChannel()

    @Test
    fun testAwait() = runBlocking {
        withTimeout(10000) {
            val future = DefaultChannelPromise(channel)
            launch {
                delay(2000)
                future.setSuccess()
            }
            future.awaitKt()
        }
        withTimeout(10000) {
            val future = DefaultChannelPromise(channel)
            future.setSuccess()
            future.awaitKt()
        }
        withTimeout(10000) {
            val future = DefaultChannelPromise(channel)
            val e = IllegalArgumentException()
            launch {
                delay(2000)
                future.setFailure(e)
            }
            try {
                future.awaitKt()
                fail("await exception fail")
            } catch (e1: IllegalArgumentException) {
                if (e1 !== e) {
                    throw e1
                }
            }
        }
    }

    @AfterAll
    fun afterAll() {
        channel.close()
    }
}
