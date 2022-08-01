/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
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
        var counter = 0

        val prop by lateinitMutableProperty {
            counter++
            value
        }
        assertSame(value, prop)
        assertSame(value, prop)
        assertEquals(1, counter)
    }

    @Test
    fun setValuePrevailsOnCompetitionWithInitializer() = runTest {
        val verySlowInitializer = CompletableDeferred<Unit>()
        val override = Symbol("override")
        val initializer = Symbol("initializer")

        var prop by lateinitMutableProperty {
            runBlocking { yield(); verySlowInitializer.await() }
            initializer
        }

        launch { println(prop) }
        prop = override
        verySlowInitializer.complete(Unit)

        assertSame(override, prop)
    }
}