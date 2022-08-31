/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.frontendbase

import java.io.OutputStreamWriter
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RepipedMessageForwardTest {
    private val pendingMsg = mutableListOf<String>()
    private val ouptut = RepipedMessageForward(pendingMsg::add).pipedOutputStream

    @Test
    fun testPrintStream() {
        val ps = PrintStream(ouptut)
        ps.println("ABC")
        ps.append("D").append("E").append("F").println("G")
        ps.println("LLOO")

        assertEquals(3, pendingMsg.size)
        assertEquals("ABC", pendingMsg.removeAt(0))
        assertEquals("DEFG", pendingMsg.removeAt(0))
        assertEquals("LLOO", pendingMsg.removeAt(0))
    }

    @Test
    fun testCRLF() {
        OutputStreamWriter(ouptut).use { writer ->

            writer.append("LINE").append("AAA").append("OOOO").append("\r\n")
            writer.append("Line1125744\r\n")
            writer.append("AFFXZ\r\n")

        }


        assertEquals(3, pendingMsg.size)
        assertEquals("LINEAAAOOOO", pendingMsg.removeAt(0))
        assertEquals("Line1125744", pendingMsg.removeAt(0))
        assertEquals("AFFXZ", pendingMsg.removeAt(0))
    }

    @Test
    fun testLF() {

        OutputStreamWriter(ouptut).use { writer ->

            writer.append("LINE").append("\n")
            writer.append("Line5\n")
            writer.append("AFFXZ\n")
            writer.append("NO\rCR REMOVED\n")

        }


        assertEquals(4, pendingMsg.size)
        assertEquals("LINE", pendingMsg.removeAt(0))
        assertEquals("Line5", pendingMsg.removeAt(0))
        assertEquals("AFFXZ", pendingMsg.removeAt(0))
        assertEquals("NO\rCR REMOVED", pendingMsg.removeAt(0))
    }

    @Test
    fun testCRLFMixing() {

        OutputStreamWriter(ouptut).use { writer ->
            writer.append("LF\n")
            writer.append("CRLF\r\n")
            writer.append("LFLF\n\n")
        }

        assertEquals(4, pendingMsg.size)
        assertEquals("LF", pendingMsg.removeAt(0))
        assertEquals("CRLF", pendingMsg.removeAt(0))
        assertEquals("LFLF", pendingMsg.removeAt(0))
        assertEquals("", pendingMsg.removeAt(0))
    }

    @Test
    fun testEmptyLines_LF() {
        OutputStreamWriter(ouptut).use { writer ->
            repeat(7) {
                writer.append("\n")
            }
        }
        assertEquals(7, pendingMsg.size)
        repeat(7) {
            assertEquals("", pendingMsg.removeAt(0))
        }
    }

    @Test
    fun testEmptyLines_CRLF() {
        OutputStreamWriter(ouptut).use { writer ->
            repeat(7) {
                writer.append("\r\n")
            }
        }
        assertEquals(7, pendingMsg.size)
        repeat(7) {
            assertEquals("", pendingMsg.removeAt(0))
        }
    }
}
