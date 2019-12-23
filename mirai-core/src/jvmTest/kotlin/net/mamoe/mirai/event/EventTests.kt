package net.mamoe.mirai.event

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
                println("Triggered")
            }

            TestEvent().broadcast().triggered shouldBeEqualTo true
            subscriber.complete()
            println("finished")
        }
    }

    @Test
    fun testSubscribeGlobalScope() {
        runBlocking {
            TestEvent().broadcast().triggered shouldBeEqualTo true
            println("finished")
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EventTests().testSubscribeGlobalScope()
            exitProcess(0)
        }
    }
}