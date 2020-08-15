/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

import kotlinx.coroutines.*
import net.mamoe.mirai.event.internal.GlobalEventListeners
import net.mamoe.mirai.utils.StepUtil
import net.mamoe.mirai.utils.internal.runBlocking
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertTrue

class TestEvent : AbstractEvent() {
    var triggered = false
}

class EventTests {
    @Test
    fun testSubscribeInplace() {
        resetEventListeners()
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
        resetEventListeners()
        runBlocking {
            GlobalScope.subscribeAlways<TestEvent> {
                triggered = true
            }

            assertTrue(TestEvent().broadcast().triggered)
        }
    }

    @Test
    fun `test concurrent listening`() {
        resetEventListeners()
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
        }
        val called = counter.get()
        println("Registered $listeners listeners and $called called")
        if (listeners != called) {
            throw IllegalStateException("Registered $listeners listeners but only $called called")
        }
    }

    @Test
    fun `test concurrent listening 3`() {
        resetEventListeners()
        runBlocking {
            val called = AtomicInteger()
            val registered = AtomicInteger()
            coroutineScope {
                println("Step 0")
                for (priority in Listener.EventPriority.values()) {
                    launch {
                        repeat(5000) {
                            registered.getAndIncrement()
                            GlobalScope.subscribeAlways<ParentEvent>(
                                priority = priority
                            ) {
                                called.getAndIncrement()
                            }
                        }
                        println("Registeterd $priority")
                    }
                }
                println("Step 1")
            }
            println("Step 2")
            ParentEvent().broadcast()
            println("Step 3")
            check(called.get() == registered.get())
            println("Done")
            println("Called ${called.get()}, registered ${registered.get()}")
        }
    }

    @Test
    fun `test concurrent listening 2`() = runBlocking {
        resetEventListeners()
        val registered = AtomicInteger()
        val called = AtomicInteger()

        val supervisor = CoroutineScope(SupervisorJob())

        coroutineScope {
            repeat(50) {
                launch {
                    repeat(444) {
                        registered.getAndIncrement()

                        supervisor.subscribeAlways<ParentEvent> {
                            called.getAndIncrement()
                        }
                    }
                }
            }
        }

        println("All listeners registered")

        val postCount = 3
        coroutineScope {
            repeat(postCount) {
                launch { ParentEvent().broadcast() }
            }
        }

        val calledCount = called.get()
        val shouldCalled = registered.get() * postCount

        println("Should call $shouldCalled times and $called called")
        if (shouldCalled != calledCount) {
            throw IllegalStateException("?")
        }
    }

    open class ParentEvent : Event, AbstractEvent() {
        var triggered = false
    }

    open class ChildEvent : ParentEvent()

    open class ChildChildEvent : ChildEvent()

    @Test
    fun `broadcast Child to Parent`() {
        resetEventListeners()
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
        resetEventListeners()
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
        resetEventListeners()
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
    fun resetEventListeners() {
        for (p in Listener.EventPriority.values()) {
            GlobalEventListeners[p].clear()
        }
    }

    @Test
    fun `test intercept with always`() {
        resetEventListeners()
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
        resetEventListeners()
        val step = StepUtil()
        singleThreaded(step) {
            subscribeAlways<AbstractEvent> {
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
    fun `test listener complete`() {
        resetEventListeners()
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
        resetEventListeners()
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