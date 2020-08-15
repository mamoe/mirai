/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlin.test.Test
import kotlin.test.assertEquals

internal class MessageChainBuilderTest {
    @Test
    fun testConcat() {
        val chain = buildMessageChain {
            +"test"
            +" "
            +PlainText("foo")
            +" "
            +(PlainText("bar") + " goo ")
            buildMessageChain {
                +"1"
                +"2"
                +"3"
            }.joinTo(this)
        }

        assertEquals("test foo bar goo 123", chain.toString())
    }

    @Test
    fun testConstrain() {
        val lastSingle = TestConstrainSingleMessage()

        val chain = buildMessageChain {
            +"test"
            +TestConstrainSingleMessage()
            +TestConstrainSingleMessage()
            +PlainText("foo")
            +TestConstrainSingleMessage()
            +lastSingle
        }

        assertEquals(chain.size, 3)
        assertEquals("test${lastSingle}foo", chain.toString())
    }
}