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

internal abstract class AbstractMutableComponentStorageTest : AbstractTest() {
    protected abstract fun createStorage(): MutableComponentStorage

    @Test
    fun `can put component`() {
        val storage = createStorage().apply {
            set(TestComponent2, TestComponent2(1))
        }
        assertEquals(1, storage.size)
    }

    @Test
    fun `can put multiple components with different key`() {
        val storage = createStorage().apply {
            set(TestComponent2, TestComponent2(1))
            set(TestComponent3, TestComponent3(1))
        }
        assertEquals(2, storage.size)
    }

    @Test
    fun `can get component`() {
        val storage = createStorage().apply {
            set(TestComponent2, TestComponent2(1))
        }
        assertEquals(1, storage.size)
        assertEquals(TestComponent2(1), storage[TestComponent2])
    }

    @Test
    fun `can override component with same key`() {
        val storage = createStorage().apply {
            set(TestComponent2, TestComponent2(1))
            set(TestComponent2, TestComponent2(2))
        }
        assertEquals(1, storage.size)
        assertEquals(TestComponent2(2), storage[TestComponent2])
    }
}