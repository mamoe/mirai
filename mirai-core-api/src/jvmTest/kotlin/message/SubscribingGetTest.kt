/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message

import kotlinx.coroutines.*
import net.mamoe.mirai.event.TestEvent
import net.mamoe.mirai.event.syncFromEvent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class SubscribingGetTest {

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