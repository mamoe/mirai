/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.utils.JdkStreamSupport.toStream
import org.junit.jupiter.api.Test
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.*

internal class KotlinFlowToJdkStreamTest {
    private fun <T> Stream<T>.collectList(): List<T> = use { collect(Collectors.toList()) }

    @Test
    internal fun testFlowFinally() = runTest {
        var finallyCalled = false

        flow<Any?> {
            try {
                while (true) {
                    emit("")
                }
            } finally {
                finallyCalled = true
            }
        }.toStream().use { it.findFirst() }

        assertTrue { finallyCalled }
    }

    @Test
    internal fun testNormally() = runTest {
        flow<Any?> {
            emit("1")
            emit("5")
            emit("2")
            emit("3")
        }.toStream().collectList().let {
            assertEquals(listOf("1", "5", "2", "3"), it)
        }
    }

    @Test
    internal fun testSuspendInFlow() = runTest {
        flow<Any?> {
            emit("1")
            yield() // Suspended
            emit("2")
        }.toStream(context = Dispatchers.IO).collectList().let {
            assertEquals(listOf("1", "2"), it)
        }
    }

    @Test
    internal fun testCounter() = runTest {
        var counter = 0
        flow<Any?> {
            while (true) {
                counter++
                emit(counter)
            }
        }.toStream().use { stream ->
            stream.limit(5).forEach { }
        }
        assertEquals(5, counter)
    }

    @Test
    internal fun testChannelFlow() = runTest {
        channelFlow<Any?> {
            send(514)
            launch { send(94481) }
            launch { send(94481) }
            launch { send(94481) }
            launch { send(94481) }
        }.toStream().collectList().let {
            assertEquals(listOf(514, 94481, 94481, 94481, 94481), it)
        }
    }

    @Test
    internal fun testExceptionCaught() = runTest {
        val msg = UUID.randomUUID().toString()
        flow<Any> { error(msg) }.toStream().use { s ->
            assertFails(msg) { s.findFirst() }.printStackTrace(System.out)
        }
    }

    @Test
    internal fun testErrorInLaunchedContext() = runTest {
        lateinit var myError: Throwable
        val msg = UUID.randomUUID().toString()

        flow<Any> {
            myError = Throwable(msg)
            throw myError
        }.toStream(
            context = Dispatchers.IO,
        ).use { stream ->
            assertFailsWith<RuntimeException>(msg) { stream.findFirst() }.let { err ->
                assertSame(myError, err.cause)
                assertTrue {
                    err.stackTrace.any { it.className == "net.mamoe.mirai.utils.JdkStreamSupport\$FlowSpliterator" && it.methodName == "tryAdvance" }
                }

                err.printStackTrace(System.out)
            }
        }
    }

    @Test
    internal fun errorWillNotCancelJob() = runTest {
        val scope = CoroutineScope(EmptyCoroutineContext)
        val errmsg = UUID.randomUUID().toString()

        flow<Any> { error(errmsg) }.toStream(
            context = scope.coroutineContext
        ).use { assertFails(errmsg) { it.findFirst() } }

        val job = scope.coroutineContext.job
        assertTrue { job.isActive }
        assertFalse { job.isCancelled }
        assertFalse { job.isCompleted }
    }
}