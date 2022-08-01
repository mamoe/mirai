/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("RedundantSuspendModifier", "unused", "UNUSED_PARAMETER")

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.*
import org.jetbrains.annotations.NotNull
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.*


internal class JvmMethodEventsTest : AbstractEventTest() {

    @Test
    fun testMethodListener() {
        class TestClass : ListenerHost, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {
            private var called = AtomicInteger(0)

            fun getCalled() = called.get()

            @Suppress("unused")
            @EventHandler
            suspend fun TestEvent.`suspend receiver param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @Suppress("unused")
            @EventHandler
            suspend fun TestEvent.`suspend receiver Unit`() {
                called.getAndIncrement()
            }

            @Suppress("unused")
            @EventHandler
            suspend fun `suspend param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @EventHandler
            @Suppress("unused")
            fun TestEvent.`receiver param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @EventHandler
            @Suppress("unused")
            suspend fun TestEvent.`suspend receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @Suppress("unused")
            @EventHandler
            suspend fun TestEvent.`suspend receiver LS`(): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            @Suppress("unused")
            suspend fun `suspend param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            @Suppress("unused")
            private fun TestEvent.`test annotations`(@NotNull event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            @Suppress("unused")
            fun TestEvent.`receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }
        }

        TestClass().run {
            this.globalEventChannel().registerListenerHost(this)

            runBlocking {
                TestEvent().broadcast()
            }

            assertEquals(9, this.getCalled())
            cancel() // reset listeners
        }
    }

    @Test
    fun testExceptionHandle() {
        class MyException : RuntimeException()

        class TestClass : SimpleListenerHost() {
            var exceptionHandled: Boolean = false

            override fun handleException(context: CoroutineContext, exception: Throwable) {
                assert(exception is ExceptionInEventHandlerException)
                assert(exception.event is TestEvent)
                assert(exception.rootCause is MyException)
                exceptionHandled = true
            }

            @Suppress("unused")
            @EventHandler
            private suspend fun TestEvent.test() {
                throw MyException()
            }
        }

        TestClass().run {
            this.globalEventChannel().registerListenerHost(this)

            runBlocking {
                TestEvent().broadcast()
            }
            cancel() // reset listeners
            if (!exceptionHandled) {
                fail("SimpleListenerHost.handleException not invoked")
            }
        }

        TestClass().run {
            globalEventChannel().registerListenerHost(this)

            runBlocking {
                TestEvent().broadcast()
            }
            cancel() // reset listeners
            if (!exceptionHandled) {
                fail("SimpleListenerHost.handleException not invoked")
            }
        }

        TestClass().run {
            val scope = CoroutineScope(EmptyCoroutineContext)
            scope.globalEventChannel().registerListenerHost(this)

            runBlocking {
                TestEvent().broadcast()
            }
            cancel() // reset listeners
            scope.cancel()
            if (!exceptionHandled) {
                fail("SimpleListenerHost.handleException not invoked")
            }
        }
    }

    @Test
    fun testIntercept() {
        class TestClass : ListenerHost, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {
            private var called = AtomicInteger(0)

            fun getCalled() = called.get()

            @Suppress("unused")
            @EventHandler(EventPriority.HIGHEST)
            private suspend fun TestEvent.`suspend receiver param Unit`(event: TestEvent) {
                intercept()
                called.getAndIncrement()
            }

            @EventHandler(EventPriority.MONITOR)
            @Suppress("unused")
            private fun TestEvent.`receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }
        }

        TestClass().run {
            this.globalEventChannel().registerListenerHost(this)

            runBlocking {
                TestEvent().broadcast()
            }
            cancel() // reset listeners

            assertEquals(1, this.getCalled())
        }
    }

    @Test
    fun testCancellation() {
        class TestingListener : SimpleListenerHost() {
            var handled: Boolean = false

            @EventHandler
            fun handle(event: TestEvent) {
                handled = true
            }
        }
        runBlocking {

            TestingListener().runTesting(this.globalEventChannel()) {
                TestEvent().broadcast()
                assertTrue { handled }
            }

            // registered listeners cancelled when parent scope cancelled
            CoroutineScope(EmptyCoroutineContext).let { scope ->
                TestingListener().runTesting(scope.globalEventChannel()) { listener ->
                    scope.cancel()
                    TestEvent().broadcast()
                    assertFalse { handled }
                    assertTrue { listener.isActive }
                }
            }

            // registered listeners cancelled when ListenerHost cancelled
            CoroutineScope(EmptyCoroutineContext).let { scope ->
                val listener = TestingListener()
                listener.runTesting(scope.globalEventChannel()) { }
                assertFalse { listener.isActive }
                assertTrue { scope.isActive }
                TestEvent().broadcast()
                assertFalse { listener.handled }
                scope.cancel()
            }

        }
    }

    private inline fun <T> T.runTesting(
        channel: EventChannel<*>,
        block: T.(T) -> Unit
    ) where T : SimpleListenerHost {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        channel.registerListenerHost(this)
        block(this, this)
        cancel()
    }
}
