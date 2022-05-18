/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.data.visitor.MessageVisitorUnit
import net.mamoe.mirai.message.data.visitor.acceptChildren
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class LinearMessageChainImplTest {

    private val complexLinearChain = buildMessageChain {
        +PlainText("foo")
        +buildMessageChain {
            +PlainText("bar")
            +emptyMessageChain()
            +buildMessageChain {
                +AtAll
                +PlainText("zoo")
            }
        }
        +buildMessageChain {
            +buildMessageChain {
                +At(2)
            }
            +At(1)
        }
    }

    @Test
    fun buildMessageChainCreatesLinearMessageChainImpl() {
        assertIs<LinearMessageChainImpl>(complexLinearChain)
    }

    @Test
    fun hierarchicalIterator() {
        assertEquals(
            listOf(
                PlainText("foo"),
                PlainText("bar"),
                AtAll,
                PlainText("zoo"),
                At(2),
                At(1),
            ),
            complexLinearChain.iterator().asSequence().toList()
        )
    }

    @Test
    fun sizeTest() {
        assertEquals(0, messageChainOf().size)
        assertEquals(6, complexLinearChain.size)
        assertEquals(6, complexLinearChain.count())
    }

    @Test
    fun acceptChildrenTest() {
        val list = buildList {
            complexLinearChain.acceptChildren(object : MessageVisitorUnit() {
                override fun visitMessage(message: Message, data: Unit) {
                    add(message)
                    super.visitMessage(message, data)
                }
            })
        }
        assertEquals(
            listOf(
                PlainText("foo"),
                PlainText("bar"),
                AtAll,
                PlainText("zoo"),
                At(2),
                At(1),
            ),
            list
        )
    }
}