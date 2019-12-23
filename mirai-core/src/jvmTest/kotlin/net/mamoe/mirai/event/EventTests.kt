package net.mamoe.mirai.event

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.test.shouldBeEqualTo
import kotlin.system.exitProcess
import kotlin.test.Test


class TestEvent : Subscribable {
    var triggered = false
}

class EventTests {
    @Test
    fun testSubscribeInplace() {
        runBlocking {
            val subscriber = subscribeAlways<TestEvent> {
                triggered = true
            }

            TestEvent().broadcast().triggered shouldBeEqualTo true
            subscriber.complete()
        }
    }

    @Test
    fun testSubscribeGlobalScope() {
        runBlocking {
            GlobalScope.subscribeAlways<TestEvent> {
                triggered = true
            }

            TestEvent().broadcast().triggered shouldBeEqualTo true
        }
    }


    open class ParentEvent : Subscribable {
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
            ChildEvent().broadcast().triggered shouldBeEqualTo true
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
            ChildChildEvent().broadcast().triggered shouldBeEqualTo true
            job.complete()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EventTests().`broadcast ChildChild to Parent`()
            exitProcess(0)
        }
    }
}