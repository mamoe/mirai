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

internal class SizedCacheTest {
    @Test
    fun validIfCacheNotFilled() {
        val cache = SizedCache<String>(20)
        val list = mutableListOf<String>()
        repeat(10) {
            cache.emit("-$it")
            list.add("-$it")
        }
        assertEquals(list, cache.toList())
    }

    @Test
    fun validIfNotUsed() {
        val cache = SizedCache<String>(20)
        assertEquals(emptyList(), cache.toList())
    }

    @Test
    fun validIfFilled() {
        val cache = SizedCache<String>(20)
        val list = mutableListOf<String>()
        repeat(20) {
            cache.emit("-$it")
            list.add("-$it")
        }
        assertEquals(list, cache.toList())
    }

    @Test
    fun chaosTest() {
        repeat(1000) {
            val size = (5..200).random()
            val list = mutableListOf<Int>()
            val cache = SizedCache<Int>(size)
            repeat((100..5000).random().coerceAtLeast(size)) {
                cache.emit(it)
                list.add(it)
            }
            assertEquals(
                list.subList(list.size - size, list.size).toMutableList(),
                cache.toMutableList(),
            )
        }
    }
}