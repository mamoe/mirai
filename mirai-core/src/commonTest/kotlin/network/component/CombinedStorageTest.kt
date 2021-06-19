/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import net.mamoe.mirai.internal.test.AbstractTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class CombinedStorageTest : AbstractTest() {

    @Test
    fun `can get from main`() {
        val storage = ConcurrentComponentStorage().apply {
            set(TestComponent2, TestComponent2(1))
        }.withFallback(ConcurrentComponentStorage())
        assertEquals(TestComponent2(1), storage[TestComponent2])
    }

    @Test
    fun `can get from fallback`() {
        val storage = ConcurrentComponentStorage().withFallback(ConcurrentComponentStorage().apply {
            set(TestComponent2, TestComponent2(1))
        })
        assertEquals(TestComponent2(1), storage[TestComponent2])
    }

    @Test
    fun `size is sum of sizes of two storages`() {
        val storage = ConcurrentComponentStorage().apply {
            set(TestComponent3, TestComponent3(1))
        }.withFallback(ConcurrentComponentStorage().apply {
            set(TestComponent2, TestComponent2(1))
        })
        assertEquals(2, storage.size)
    }

    @Test
    fun `size can be zero`() {
        val storage = ConcurrentComponentStorage().withFallback(ConcurrentComponentStorage())
        assertEquals(0, storage.size)
    }

    @Test
    fun `main prevails than fallback`() {
        val storage = ConcurrentComponentStorage().apply {
            set(TestComponent2, TestComponent2(1))
        }.withFallback(ConcurrentComponentStorage().apply {
            set(TestComponent2, TestComponent2(2))
        })
        assertEquals(TestComponent2(1), storage[TestComponent2])
    }

    @Test
    fun `returns empty if both are null`() {
        val x: ComponentStorage = null.withFallback(null)
        assertEquals(ComponentStorage.EMPTY, x)
    }

    @Test
    fun `returns first if second if null`() {
        val x = ConcurrentComponentStorage().apply { set(TestComponent2, TestComponent2(1)) }
        assertSame(x, x.withFallback(null))
    }

    @Test
    fun `returns second if first if null`() {
        val x = ConcurrentComponentStorage().apply { set(TestComponent2, TestComponent2(1)) }
        assertSame(x, null.withFallback(x))
    }
}