/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/*

internal class AtomicResizeCacheListTest {
    @Test
    fun testDuplication() {
        val list = AtomicResizeCacheList<Int>(1000)
        assertTrue { list.ensureNoDuplication(1) }
        assertFalse { list.ensureNoDuplication(1) }
        assertTrue { list.ensureNoDuplication(1) }
    }

    @Test
    fun testRetention() {
        val list = AtomicResizeCacheList<Int>(1000)
        assertTrue { list.ensureNoDuplication(1) }
        runBlocking {
            delay(1010)
            // because no concurrency guaranteed on same elements
            assertFalse { list.ensureNoDuplication(1) }
            assertTrue { list.ensureNoDuplication(2) }
            // outdated elements are now cleaned
            assertTrue { list.ensureNoDuplication(1) }
        }
    }
}*/