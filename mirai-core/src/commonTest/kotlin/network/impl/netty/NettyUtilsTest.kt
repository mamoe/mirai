/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import io.netty.channel.DefaultChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.test.runBlockingUnit
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration

/**
 * @see awaitKt
 */
internal class NettyUtilsTest : AbstractTest() {
    companion object {
        private val channel = EmbeddedChannel()

        @JvmStatic
        @AfterAll
        fun afterAll() {
            channel.close()
        }
    }

    @Test
    fun canAwait() = runBlockingUnit(timeout = Duration.seconds(5)) {
        val future = DefaultChannelPromise(channel)
        launch(start = CoroutineStart.UNDISPATCHED) { future.awaitKt() }
        launch {
            future.setSuccess()
        }
    }

    @Test
    fun returnsImmediatelyIfCompleted() = runBlockingUnit(timeout = Duration.seconds(5)) {
        val future = DefaultChannelPromise(channel)
        future.setSuccess()
        future.awaitKt()
    }

    @Test
    fun testAwait() {
        class MyError : AssertionError("My") // coroutine debugger will modify the exception if inside coroutine

        runBlockingUnit(timeout = Duration.seconds(5)) {
            val future = DefaultChannelPromise(channel)
            launch(start = CoroutineStart.UNDISPATCHED) {
                assertFailsWith<AssertionError> {
                    future.awaitKt()
                }.let { actual ->
                    assertTrue { actual is MyError }
                }
            }
            launch {
                future.setFailure(MyError())
            }
        }
    }
}
