/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.code

import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestMiraiCode {
    @Test
    fun testCodes() {
        assertEquals(AtAll.asMessageChain(), "[mirai:atall]".parseMiraiCode())
        assertEquals(PlainText("[Hello").asMessageChain(), "\\[Hello".parseMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("1")
            +AtAll
            +PlainText("2345")
            +AtAll
        }, "1[mirai:atall]2345[mirai:atall]".parseMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("1")
            +AtAll
            +PlainText("2345[mirai:atall")
        }, "1[mirai:atall]2345[mirai:atall".parseMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "\\[mirai:atall]".parseMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "[mirai:atall\\]".parseMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "[mirai\\:atall]".parseMiraiCode())
        assertEquals(buildMessageChain {
            +SimpleServiceMessage(1, "[HiHi!!!\\]")
            +PlainText(" XE")
        }, "[mirai:service:1,\\[HiHi!!!\\\\\\]] XE".parseMiraiCode())
    }
}