/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.util

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.coroutines.resume
import kotlin.test.assertEquals
import kotlin.time.seconds

internal class TestCoroutineUtils {

    @Test
    fun `test launchTimedTask 0 time`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob())

        val result = withTimeoutOrNull(6000) {
            suspendCancellableCoroutine<Unit> { cont ->
                scope.launchTimedTask(5.seconds.toLongMilliseconds()) {
                    cont.resume(Unit)
                }
            }
        }

        assertEquals(null, result)
        scope.cancel()
    }

    @Test
    fun `test launchTimedTask finishes 1 time`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob())

        withTimeout(4000) {
            suspendCancellableCoroutine<Unit> { cont ->
                val task = scope.launchTimedTask(3.seconds.toLongMilliseconds()) {
                    cont.resume(Unit)
                }
                task.setChanged()
            }
        }

        scope.cancel()
    }

    @Test
    fun `test launchTimedTask finishes multiple times`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob())

        withTimeout(10000) {
            suspendCancellableCoroutine<Unit> { cont ->
                val task = scope.launchTimedTask(3.seconds.toLongMilliseconds()) {
                    cont.resume(Unit)
                }
                task.setChanged()
                launch {
                    delay(4000)
                    task.setChanged()
                }
            }
        }

        scope.cancel()
    }

    @Test
    fun `test launchTimedTask interval less than delay`() = runBlocking {
        val scope = CoroutineScope(SupervisorJob())

        withTimeout(5000) {
            suspendCancellableCoroutine<Unit> { cont ->
                val task = scope.launchTimedTask(1.seconds.toLongMilliseconds()) {
                    cont.resume(Unit)
                }
                task.setChanged()
            }
        }

        scope.cancel()
    }

}