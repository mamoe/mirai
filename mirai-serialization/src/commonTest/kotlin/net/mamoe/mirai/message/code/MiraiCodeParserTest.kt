/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.message.code

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.VipFace.Companion.AiXin
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MiraiCodeParserTest {

    @Test
    fun testSplit() {
        val str = "sadvass][ [mirai:at:1,test]]vdsavs [mirai:atall]"
        assertEquals(str, str.parseMiraiCode().toString())
    }

    @Test
    fun at() {
        val str = "[mirai:at:1,test]"
        assertEquals(At._lowLevelConstructAtInstance(1, "test"), str.parseMiraiCode()[0])

        fun testPlain(str: String) {
            assertEquals(str, (str.parseMiraiCode()[0] as PlainText).content)
        }
        testPlain("[mirai:at:bad,test]")
        testPlain("[mirai:at:bad]")
        testPlain("[mirai:at:]")
        testPlain("[mirai:at]")
    }

    @Test
    fun atAll() {
        val str = "[mirai:atall]"
        assertEquals(AtAll, str.parseMiraiCode()[0])
    }

    @Test
    fun poke() {
        assertEquals(PokeMessage.Poke, PokeMessage.Poke.toString().parseMiraiCode()[0])
    }

    @Test
    fun vipFace() {
        val instance = VipFace(AiXin, 1)
        assertEquals(instance, instance.toString().parseMiraiCode()[0])
    }

    @Test
    fun image() {
        val instance = Image("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai")
        assertEquals(instance, instance.toString().parseMiraiCode()[0])
    }

    @Test
    fun flash() {
        val instance = Image("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai").flash()
        assertEquals(instance, instance.toString().parseMiraiCode()[0])
    }
}