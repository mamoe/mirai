/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("RedundantSuspendModifier", "unused", "UNUSED_PARAMETER")

package net.mamoe.mirai.event

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.internal.runBlocking
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.EmptyCoroutineContext


internal class JvmMethodEventsTest {

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

            @Suppress("unused")
            @EventHandler
            suspend fun `suspend param Void`(event: TestEvent): Void? {
                called.getAndIncrement()
                return null
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
            fun TestEvent.`receiver param LS`(event: TestEvent): ListeningStatus {
                called.getAndIncrement()
                return ListeningStatus.STOPPED
            }
        }

//        TestClass().run {
//            this.registerEvents()
//
//            runBlocking {
//                TestEvent().broadcast()
//            }
//
//            assertEquals(9, this.getCalled())
//        }
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

//        TestClass().run {
//            this.registerEvents()
//
//            runBlocking {
//                TestEvent().broadcast()
//            }
//
//            assertEquals(1, this.getCalled())
//        }
    }
}
