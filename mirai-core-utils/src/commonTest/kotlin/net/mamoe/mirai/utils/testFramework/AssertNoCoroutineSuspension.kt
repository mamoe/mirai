/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.testFramework

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

suspend inline fun <R> assertNoCoroutineSuspension(
    crossinline block: suspend () -> R,
): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withContext(Dispatchers.Default.limitedParallelism(1)) {
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            yield()
            fail("Expected no coroutine suspension")
        }
        val ret = block()
        job.cancel()
        ret
    }
}

suspend inline fun <R> assertCoroutineSuspends(
    crossinline block: suspend () -> R,
): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return withContext(Dispatchers.Default.limitedParallelism(1)) {
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            yield() // goto block
        }
        val ret = block()
        kotlin.test.assertTrue("Expected coroutine suspension") { job.isCompleted }
        job.cancel()
        ret
    }
}

class AssertCoroutineSuspensionTest {
    @Test
    fun `assertNoCoroutineSuspension no suspension`() = runTest {
        assertNoCoroutineSuspension {}
    }

    @Test
    fun `assertNoCoroutineSuspension suspend cancellable`() = runTest {
        assertFails {
            assertNoCoroutineSuspension {
                suspendCancellableCoroutine<Unit> { }
            }
        }.run {
            assertEquals("Expected no coroutine suspension", message)
        }
    }

    @Test
    fun `assertCoroutineSuspends suspend`() = runTest {
        assertCoroutineSuspends {
            suspendCancellableCoroutine {
                // resume after suspendCancellableCoroutine returns to create a suspension
                launch(start = CoroutineStart.UNDISPATCHED) {
                    yield()
                    it.resume(Unit)
                }
            }
        }
    }

    @Test
    fun `assertCoroutineSuspends no suspension`() = runTest {
        assertFails {
            assertCoroutineSuspends {}
        }.run {
            assertEquals("Expected coroutine suspension", message)
        }
    }

}