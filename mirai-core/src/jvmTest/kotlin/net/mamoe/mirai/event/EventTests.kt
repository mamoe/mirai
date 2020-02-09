/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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