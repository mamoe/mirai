/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse

internal sealed class MapTest(
    private val map: MutableMap<Int, Int>
) {
    class ConcurrentMapTest : MapTest(ConcurrentHashMap())

    @Test
    fun `initial state test`() {
        assertEquals(0, map.size)
        assertEquals(null, map[1])
        assertFalse(map.iterator().hasNext())
        assertFails { map.iterator().next() }
    }

    @Test
    fun `get set size`() {
        assertEquals(0, map.size)
        assertEquals(null, map[1])
        map[1] = 2
        assertEquals(2, map[1])
        assertEquals(1, map.size)
        map[2] = 2
        assertEquals(2, map[2])
        assertEquals(2, map.size)
    }
}