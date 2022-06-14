/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.util

//import kotlinx.coroutines.*
//import kotlin.test.Test
//import java.util.concurrent.atomic.AtomicInteger
//import kotlin.coroutines.resume
//import kotlin.test.assertEquals
//import kotlin.time.milliseconds
//import kotlin.time.seconds

internal class TestCoroutineUtils {

    // TODO TestCoroutineUtils disabled manually: on CI real time measurement is not precise causing tests to fail.

//    @Test
//    fun `test launchTimedTask 0 time`() = runTest {
//        val scope = CoroutineScope(SupervisorJob())
//
//        val result = withTimeoutOrNull(6000) {
//            suspendCancellableCoroutine<Unit> { cont ->
//                scope.launchTimedTask(5.seconds.toLongMilliseconds()) {
//                    cont.resume(Unit)
//                }
//            }
//        }
//
//        assertEquals(null, result)
//        scope.cancel()
//    }
//
//    @Test
//    fun `test launchTimedTask finishes 1 time`() = runTest {
//        val scope = CoroutineScope(SupervisorJob())
//
//        withTimeout(4000) {
//            suspendCancellableCoroutine<Unit> { cont ->
//                val task = scope.launchTimedTask(3.seconds.toLongMilliseconds()) {
//                    cont.resume(Unit)
//                }
//                task.setChanged()
//            }
//        }
//
//        scope.cancel()
//    }
//
//    @Test
//    fun `test launchTimedTask finishes multiple times`() = runTest {
//        val scope = CoroutineScope(SupervisorJob())
//
//        val resumedTimes = AtomicInteger(0)
//        val task = scope.launchTimedTask(3000.milliseconds.toLongMilliseconds()) {
//            resumedTimes.incrementAndGet()
//        }
//        task.setChanged()
//        launch {
//            delay(4000)
//            task.setChanged()
//        }
//
//        delay(6000)
//        assertEquals(1, resumedTimes.get())
//        delay(15000)
//        assertEquals(2, resumedTimes.get())
//
//        scope.cancel()
//    }
//
//    @Test
//    fun `test launchTimedTask interval less than delay`() = runTest {
//        val scope = CoroutineScope(SupervisorJob())
//
//        withTimeout(5000) {
//            suspendCancellableCoroutine<Unit> { cont ->
//                val task = scope.launchTimedTask(1.seconds.toLongMilliseconds()) {
//                    cont.resume(Unit)
//                }
//                task.setChanged()
//            }
//        }
//
//        scope.cancel()
//    }

}