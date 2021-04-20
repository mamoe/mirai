/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // MiraiCodeParser

package net.mamoe.mirai.internal.message.code

import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.code.internal.MiraiCodeParser
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestMiraiCode : AbstractTest() {
    @Test
    fun testDynamicMiraiCodeParser() {
        fun runTest(args: Int, code: String, parse: (args: Array<String>) -> Unit) {
            val response = MiraiCodeParser.DynamicParser(args) { args0 -> parse(args0); AtAll }.parse(null, code)
            assertNotNull(response, "Parser not invoked")
        }
        runTest(3, "test,\\,test,\\,\\,test") { (arg1, arg2, arg3) ->
            assertEquals("test", arg1)
            assertEquals(",test", arg2)
            assertEquals(",,test", arg3)
        }
        runTest(2, ",") {}
    }

    @Test
    fun `test serialization`() {
        assertEquals("[mirai:file:id,1,name,2]", FileMessage("id", 1, "name", 2).serializeToMiraiCode())
    }

    @Test
    fun `test deserialization`() {
        assertEquals(AtAll.toMessageChain(), "[mirai:atall]".deserializeMiraiCode())
        assertEquals(PlainText("[Hello").toMessageChain(), "\\[Hello".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("1")
            +AtAll
            +PlainText("2345")
            +AtAll
        }, "1[mirai:atall]2345[mirai:atall]".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("1")
            +AtAll
            +PlainText("2345[mirai:atall")
        }, "1[mirai:atall]2345[mirai:atall".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "\\[mirai:atall]".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "[mirai:atall\\]".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +PlainText("[mirai:atall]")
        }, "[mirai\\:atall]".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +SimpleServiceMessage(1, "[HiHi!!!\\]")
            +PlainText(" XE")
        }, "[mirai:service:1,\\[HiHi!!!\\\\\\]] XE".deserializeMiraiCode())
        assertEquals(buildMessageChain {
            +Dice(1)
        }, "[mirai:dice:1]".deserializeMiraiCode())
        assertEquals(FileMessage("id", 1, "name", 2), "[mirai:file:id,1,name,2]".deserializeMiraiCode().single())

        val musicShare = MusicShare(
            kind = MusicKind.NeteaseCloudMusic,
            title = "ファッション",
            summary = "rinahamu/Yunomi",
            jumpUrl = "http://music.163.com/song/1338728297/?userid=324076307",
            pictureUrl = "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
            musicUrl = "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307",
            brief = "",
        )
        assertEquals(musicShare.toMessageChain(), musicShare.serializeToMiraiCode().deserializeMiraiCode())
    }
}