/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TestMessageChainDelegate {
    private val message = messageChainOf(AtAll, PlainText("test"))

    @Test
    fun testGetValue() {
        @Suppress("UNUSED_VARIABLE")
        val atAll: AtAll by message
        val plain: PlainText by message
        assertEquals(plain.content, "test")
    }

    @Test
    fun testOrNull() {
        @Suppress("UNUSED_VARIABLE")
        val atAll: AtAll? by message.orNull()
        val plain: PlainText? by message.orNull()
        assertEquals(plain!!.content, "test")
    }

    @Test
    fun testOrElse() {
        val message = messageChainOf()

        @Suppress("UNUSED_VARIABLE")
        val plain: PlainText? by message.orElse { PlainText("test") }
        assertEquals(plain!!.content, "test")
    }

}