/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.event

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.AtomicInteger
import net.mamoe.mirai.utils.childScope
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TestEvent : AbstractEvent() {
    var triggered = false
}

internal class EventTests : AbstractEventTest() {
    var scope = CoroutineScope(EmptyCoroutineContext)

    @AfterTest
    fun finallyReset() {
        resetEventListeners()
    }

    @Test
    fun testSubscribeInplace() {
        resetEventListeners()
        runBlocking(scope.coroutineContext) {
            val subscriber = globalEventChannel().subscribeAlways<TestEvent> {
                triggered = true
            }

            assertTrue(TestEvent().broadcast().triggered)
            assertTrue { subscriber.complete() }
        }
    }

    @Test
    fun testSubscribeGlobalScope() {
        resetEventListeners()
        runBlocking {
            val listener = globalEventChannel().subscribeAlways<TestEvent> {
                triggered = true
            }

            assertTrue(TestEvent().broadcast().triggered)
            listener.complete()
        }
    }

    @Test
    fun `test concurrent listening`() {
        resetEventListeners()
        var listeners = 0
        val counter = AtomicInteger(0)
        val channel = scope.globalEventChannel()
        for (p in EventPriority.values()) {
            repeat(2333) {
                listeners++
                channel.subscribeAlways<ParentEvent> {
                    counter.getAndIncrement()
                }
            }
        }
        runBlocking {
            ParentEvent().broadcast()
        }
        val called = counter.value
        println("Registered $listeners listeners and $called called")
        if (listeners != called) {
            throw IllegalStateException("Registered $listeners listeners but only $called called")
        }
    }

    @Test
    fun `test concurrent listening 3`() {
        resetEventListeners()
        runBlocking {
            val called = AtomicInteger(0)
            val registered = AtomicInteger(0)
            coroutineScope {
                println("Step 0")
                for (priority in EventPriority.values()) {
                    launch {
                        repeat(5000) {
                            registered.getAndIncrement()
                            scope.globalEventChannel().subscribeAlways<ParentEvent>(
                                priority = priority
                            ) {
                                called.getAndIncrement()
                            }
                        }
                        println("Registered $priority")
                    }
                }
                println("Step 1")
            }
            println("Step 2")
            ParentEvent().broadcast()
            println("Step 3")
            check(called.value == registered.value)
            println("Done")
            println("Called ${called.value}, registered ${registered.value}")
        }
    }

    @Test
    fun `test concurrent listening 2`() = runTest {
        resetEventListeners()
        val registered = AtomicInteger(0)
        val called = AtomicInteger(0)

        val supervisor = CoroutineScope(SupervisorJob())

        coroutineScope {
            repeat(50) {
                launch {
                    repeat(444) {
                        registered.getAndIncrement()

                        supervisor.globalEventChannel().subscribeAlways<ParentEvent> {
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

        val calledCount = called.value
        val shouldCalled = registered.value * postCount
        supervisor.cancel()

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
            job = globalEventChannel().subscribeAlways<ParentEvent> {
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
            job = globalEventChannel().subscribeAlways<ParentEvent> {
                triggered = true
            }
            assertTrue(ChildChildEvent().broadcast().triggered)
            job.complete()
        }
    }

    open class PriorityTestEvent : AbstractEvent()

    private fun singleThreaded(step: StepUtil, invoke: suspend EventChannel<Event>.() -> Unit) {
        runTest(borrowSingleThreadDispatcher()) {
            val scope = this.childScope()
            invoke(scope.globalEventChannel())
            scope.cancel()
        }
        step.throws()
    }

    @Test
    fun `test handler remove`() {
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
    private fun resetEventListeners() {
        scope.cancel()
        runBlocking { scope.coroutineContext[Job]?.join() }
        scope = CoroutineScope(EmptyCoroutineContext)
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
        resetEventListeners()
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
            subscribe<PriorityTestEvent>(priority = EventPriority.HIGH) {
                step.step(0)
                ListeningStatus.LISTENING
            }
            subscribe<PriorityTestEvent>(priority = EventPriority.LOW) {
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

    @Test
    fun `test next event and intercept`() {
        resetEventListeners()
        GlobalEventChannel.subscribeOnce<TestEvent> {
            GlobalEventChannel.nextEvent<TestEvent>(priority = EventPriority.HIGH, intercept = true)
        }
        GlobalEventChannel.subscribeAlways<TestEvent>(priority = EventPriority.LOW) {
            this.triggered = true
        }
        val tmp = TestEvent()
        val tmp2 = TestEvent()
        runBlocking {
            launch {
                tmp.broadcast()
            }
            launch {
                tmp2.broadcast()
            }
        }
        assertTrue { (tmp.triggered || tmp2.triggered) && (tmp.triggered != tmp2.triggered) }
        resetEventListeners()
    }
}