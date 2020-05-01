/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.utils.StepUtil
import net.mamoe.mirai.utils.internal.runBlocking
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertTrue

class TestEvent : Event, AbstractEvent() {
    var triggered = false
}

class EventTests {
    @Test
    fun testSubscribeInplace() {
        runBlocking {
            val subscriber = subscribeAlways<TestEvent> {
                triggered = true
            }

            assertTrue(TestEvent().broadcast().triggered)
            subscriber.complete()
        }
    }

    @Test
    fun testSubscribeGlobalScope() {
        runBlocking {
            GlobalScope.subscribeAlways<TestEvent> {
                triggered = true
            }

            assertTrue(TestEvent().broadcast().triggered)
        }
    }

    @Test
    fun `test concurrent listening`() {
        var listeners = 0
        val counter = AtomicInteger(0)
        for (p in Listener.EventPriority.values()) {
            repeat(2333) {
                listeners++
                GlobalScope.subscribeAlways<ParentEvent> {
                    counter.getAndIncrement()
                }
            }
        }
        kotlinx.coroutines.runBlocking {
            ParentEvent().broadcast()
            delay(5000L) // ?
        }
        val called = counter.get()
        println("Registered $listeners listeners and $called called")
        if (listeners != called) {
            throw IllegalStateException("Registered $listeners listeners but only $called called")
        }
    }

    open class ParentEvent : Event, AbstractEvent() {
        var triggered = false
    }

    open class ChildEvent : ParentEvent()

    open class ChildChildEvent : ChildEvent()

    @Test
    fun `broadcast Child to Parent`() {
        runBlocking {
            val job: CompletableJob
            job = subscribeAlways<ParentEvent> {
                triggered = true
            }

            assertTrue(ChildEvent().broadcast().triggered)
            job.complete()
        }
    }

    @Test
    fun `broadcast ChildChild to Parent`() {
        runBlocking {
            val job: CompletableJob
            job = subscribeAlways<ParentEvent> {
                triggered = true
            }
            assertTrue(ChildChildEvent().broadcast().triggered)
            job.complete()
        }
    }

    open class PriorityTestEvent : AbstractEvent() {}

    fun singleThreaded(step: StepUtil, invoke: suspend CoroutineScope.() -> Unit) {
        // runBlocking 会完全堵死, 没法退出
        val scope = CoroutineScope(Executor { it.run() }.asCoroutineDispatcher())
        val job = scope.launch {
            invoke(scope)
        }
        kotlinx.coroutines.runBlocking {
            job.join()
        }
        step.throws()
    }

    @Test
    fun `test handler remvoe`() {
        val step = StepUtil()
        singleThreaded(step) {
            subscribe<Event> {
                step.step(0)
                ListeningStatus.STOPPED
            }
            ParentEvent().broadcast()
            ParentEvent().broadcast()
        }
    }

    /*
    @Test
    fun `test boom`() {
        val step = StepUtil()
        singleThreaded(step) {
            step.step(0)
            step.step(0)
        }
    }
    */

    @Test
    fun `test intercept with always`() {
        val step = StepUtil()
        singleThreaded(step) {
            subscribeAlways<ParentEvent> {
                step.step(0)
                intercept()
            }
            subscribe<Event> {
                step.step(-1, "Boom")
                ListeningStatus.LISTENING
            }
            ParentEvent().broadcast()
        }
    }

    @Test
    fun `test intercept`() {
        val step = StepUtil()
        singleThreaded(step) {
            subscribe<Event> {
                step.step(0)
                ListeningStatus.INTERCEPTED
            }
            subscribe<Event> {
                step.step(-1, "Boom")
                ListeningStatus.LISTENING
            }
            ParentEvent().broadcast()
        }
    }

    @Test
    fun `test listener complete`() {
        val step = StepUtil()
        singleThreaded(step) {
            val listener = subscribeAlways<ParentEvent> {
                step.step(0, "boom!")
            }
            ParentEvent().broadcast()
            listener.complete()
            ParentEvent().broadcast()
        }
    }

    @Test
    fun `test event priority`() {
        val step = StepUtil()
        singleThreaded(step) {
            subscribe<PriorityTestEvent> {
                step.step(1)
                ListeningStatus.LISTENING
            }
            subscribe<PriorityTestEvent>(priority = Listener.EventPriority.HIGH) {
                step.step(0)
                ListeningStatus.LISTENING
            }
            subscribe<PriorityTestEvent>(priority = Listener.EventPriority.LOW) {
                step.step(3)
                ListeningStatus.LISTENING
            }
            subscribe<PriorityTestEvent> {
                step.step(2)
                ListeningStatus.LISTENING
            }
            PriorityTestEvent().broadcast()
        }
    }
}