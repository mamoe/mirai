/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("RedundantSuspendModifier", "unused")

package net.mamoe.mirai.event

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.internal.runBlocking
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals


internal class JvmMethodEventsTest {

    @Test
    fun testMethodListener() {
        class TestClass : ListenerHost, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {
            private var called = AtomicInteger(0)

            fun getCalled() = called.get()

            @EventHandler
            suspend fun TestEvent.`suspend receiver param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @EventHandler
            suspend fun TestEvent.`suspend receiver Unit`() {
                called.getAndIncrement()
            }

            @EventHandler
            suspend fun `suspend param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @EventHandler
            fun TestEvent.`receiver param Unit`(event: TestEvent) {
                called.getAndIncrement()
            }

            @EventHandler
            suspend fun TestEvent.`suspend receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            suspend fun TestEvent.`suspend receiver LS`(): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            suspend fun `suspend param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }

            @EventHandler
            fun TestEvent.`receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }
        }

        TestClass().run {
            this.registerEvents()

            runBlocking {
                TestEvent().broadcast()
            }

            assertEquals(8, this.getCalled())
        }
    }

    @Test
    fun testIntercept() {
        class TestClass : ListenerHost, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {
            private var called = AtomicInteger(0)

            fun getCalled() = called.get()

            @EventHandler(Listener.EventPriority.HIGHEST)
            private suspend fun TestEvent.`suspend receiver param Unit`(event: TestEvent) {
                intercept()
                called.getAndIncrement()
            }

            @EventHandler(EventPriority.MONITOR)
            private fun TestEvent.`receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }
        }

        TestClass().run {
            this.registerEvents()

            runBlocking {
                TestEvent().broadcast()
            }

            assertEquals(1, this.getCalled())
        }
    }
}
