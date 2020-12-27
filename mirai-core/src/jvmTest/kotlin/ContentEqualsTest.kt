/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TestConstrainSingleMessage : ConstrainSingle, Any() {
    companion object Key : AbstractMessageKey<TestConstrainSingleMessage>({ it.safeCast() })

    override fun toString(): String = "<TestConstrainSingleMessage#${super.hashCode()}>"
    override fun contentToString(): String = ""

    override val key: MessageKey<TestConstrainSingleMessage>
        get() = Key
}

internal class ContentEqualsTest {

    @Test
    fun testContentEquals() {
        val mySource = TestConstrainSingleMessage()
        val image = Image("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.mirai")

        assertTrue {
            buildMessageChain {
                +"test"
            }.contentEquals(buildMessageChain {
                +"te"
                +mySource
                +"st"
            })
        }

        assertFalse {
            buildMessageChain {
                +"tests"
            }.contentEquals(buildMessageChain {
                +"te"
                +"st"
            })
        }

        assertEquals("test", buildMessageChain {
            +mySource
            +"test"
            +mySource
        }.content)

        assertTrue {
            buildMessageChain {
                +"test"
            }.contentEquals(buildMessageChain {
                +"te"
                +"st"
                +mySource
            })
        }

        assertTrue {
            buildMessageChain {
                +"test"
                +image
            }.contentEquals(buildMessageChain {
                +"te"
                +mySource
                +"st"
                +image
            })
        }


        assertEquals("test", buildMessageChain {
            +mySource
            +"test"
            +mySource
        }.content)

        assertTrue {
            buildMessageChain {
                +"test"
                +image
            }.contentEquals(buildMessageChain {
                +"te"
                +"st"
                +image
                +mySource
            })
        }

        assertFalse {
            buildMessageChain {
                +image
                +"test"
                +mySource
            }.contentEquals("test")
        }

        assertFalse {
            buildMessageChain {
                +"test"
                +image
            }.contentEquals("test")
        }

        assertFalse {
            buildMessageChain {
                +image
                +"test"
            }.contentEquals(buildMessageChain {
                +"te"
                +"st"
                +image
                +mySource
            })
        }
    }
}