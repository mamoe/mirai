/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.internal

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.utils.ScheduledJob
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ScheduledJobTest : AbstractTest() {
    @Test
    fun testScheduledJob() {
        runBlocking {
            val scope = CoroutineScope(CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
            })
            val invoked = atomic(0)
            val job = ScheduledJob(scope.coroutineContext, 1000) {
                invoked.incrementAndGet()
            }
            delay(100)
            assertEquals(0, invoked.value)
            job.notice()
            job.notice()
            job.notice()
        }
    }
}