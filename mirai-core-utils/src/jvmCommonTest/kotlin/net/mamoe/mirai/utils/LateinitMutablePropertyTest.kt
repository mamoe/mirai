/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class LateinitMutablePropertyTest {
    @Test
    fun canInitialize() {
        val value = Symbol("expected")
        val prop by lateinitMutableProperty { value }
        assertSame(value, prop)
    }

    @Test
    fun canOverride() {
        val value = Symbol("expected")
        val overrode = Symbol("override")

        var prop by lateinitMutableProperty { value }
        prop = overrode
        assertSame(overrode, prop)
    }

    @Test
    fun initializerCalledOnce() {
        val value = Symbol("expected")
        val counter = AtomicInteger(0)

        val prop by lateinitMutableProperty {
            counter.incrementAndGet()
            value
        }
        assertSame(value, prop)
        assertSame(value, prop)
        assertEquals(1, counter.get())
    }

    @Test
    fun initializerCalledOnceConcurrent() = runBlocking {
        val value = Symbol("expected")
        val counter = AtomicInteger(0)

        val verySlowInitializer = CompletableFuture<Unit>()


        val prop by lateinitMutableProperty {
            counter.incrementAndGet()
            verySlowInitializer.join() // do not use coroutine: coroutines run in same thread so `synchronized` doesnt work.
            value
        }


        val lock = CompletableDeferred<Unit>()
        repeat(10) {
            launch {
                lock.join()
                @Suppress("UNUSED_EXPRESSION")
                prop
            }
        }
        lock.complete(Unit) // resume callers


        verySlowInitializer.complete(Unit)

        assertSame(value, prop)
        assertEquals(1, counter.get())
    }

    @Test
    fun setValuePrevailsOnCompetitionWithInitializer() {
        val verySlowInitializer = CompletableFuture<Unit>()
        val override = Symbol("override")
        val initializer = Symbol("initializer")

        var prop by lateinitMutableProperty {
            verySlowInitializer.join()
            initializer
        }

        thread { println(prop) }
        prop = override
        verySlowInitializer.complete(Unit)

        assertSame(override, prop)
    }
}