/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.event.TestEvent
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.test.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class SubscribingGetTest {

    @Test
    fun testSyncFromEvent(): Unit = runBlocking {
        withTimeout(500) {
            suspendCoroutine<Unit> { cont ->
                launch {
                    syncFromEvent(5000) { _: TestEvent ->
                        cont.resume(Unit)
                    }
                }
                launch {
                    TestEvent().broadcast()
                }
            }
        }
    }

    @Test
    fun testSyncFromEventTimeout() {
        runBlockingWithTimeout(500) {
            assertFailsWith<TimeoutCancellationException> {
                syncFromEvent(100) { _: TestEvent -> }
            }
        }
    }
}

internal fun <R> runBlockingWithTimeout(
    millis: Long,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> R
): R = runBlocking(context) {
    withTimeout(millis, block)
}