/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class LateinitMutablePropertyTestJvm {
    @Test
    fun `initializer called once if requested by multiple threads`() = runTest {
        val value = Symbol("expected")
        var counter = 0

        val verySlowInitializer = CompletableDeferred<Unit>()


        val prop by lateinitMutableProperty {
            counter++
            runBlocking { yield(); verySlowInitializer.await() }
            value
        }


        // requested by 10 threads
        val lock = CompletableFuture<Unit>()
        repeat(10) {
            thread {
                lock.join()
                @Suppress("UNUSED_EXPRESSION")
                prop
            }
        }
        lock.complete(Unit) // resume callers


        verySlowInitializer.complete(Unit)

        assertSame(value, prop)
        assertEquals(1, counter)
    }

}