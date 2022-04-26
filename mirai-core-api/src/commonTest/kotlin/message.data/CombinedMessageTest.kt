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
import net.mamoe.mirai.message.data.visitor.RecursiveMessageVisitor
import net.mamoe.mirai.message.data.visitor.accept
import net.mamoe.mirai.message.data.visitor.acceptChildren
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs


@OptIn(MessageChainConstructor::class)
internal class CombinedMessageTest {
    private fun linearMessageChainOf(vararg values: SingleMessage) =
        LinearMessageChainImpl.create(values.toList(), values.any { it.hasConstrainSingle })

    @Test
    fun singlePlusSingleCreatesCombinedMessage() {
        kotlin.run {
            val chain = PlainText("fo").plus(PlainText("o"))
            assertIs<CombinedMessage>(chain)
            assertEquals(PlainText("fo"), chain.element)
            assertEquals(PlainText("o"), chain.tail)
            assertEquals(false, chain.hasConstrainSingle)
        }
        kotlin.run {
            val chain = PlainText("fo").plus(LightApp("c"))
            assertIs<LinearMessageChainImpl>(chain)
            assertEquals(LightApp("c"), chain.single())
            assertEquals(true, chain.hasConstrainSingle)
        }
    }

    @Test
    fun singlePlusChainCreatesCombinedMessage() {
        val chain = PlainText("fo").plus(linearMessageChainOf(PlainText("o")))
        assertIs<CombinedMessage>(chain)
        assertEquals(PlainText("fo"), chain.element)
        assertEquals(linearMessageChainOf(PlainText("o")), chain.tail)
        assertEquals(false, chain.hasConstrainSingle)
    }

    @Test
    fun chainPlusSingleCreatesCombinedMessage() {
        val chain = linearMessageChainOf(PlainText("o")).plus(PlainText("fo"))
        assertIs<CombinedMessage>(chain)
        assertEquals(linearMessageChainOf(PlainText("o")), chain.element)
        assertEquals(PlainText("fo"), chain.tail)
        assertEquals(false, chain.hasConstrainSingle)
    }

    private fun createTestInstance(): CombinedMessage {
        return CombinedMessage(
            buildMessageChain {
                +PlainText("bar")
                +emptyMessageChain()
                +buildMessageChain {
                    +AtAll
                    +PlainText("zoo")
                }
            },
            buildMessageChain {
                +buildMessageChain {
                    +At(2)
                }
                +At(1)
            },
            false
        )
    }

    private fun createComplexCombined() = CombinedMessage(
        PlainText("foo"),
        createTestInstance(),
        false
    )

    private val complexCombined = createComplexCombined()
    ///////////////////////////////////////////////////////////////////////////
    // properties
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun sizeTest() {
        assertEquals(0, CombinedMessage(messageChainOf(), messageChainOf(), false).size)
        assertEquals(
            0,
            CombinedMessage(messageChainOf(), CombinedMessage(messageChainOf(), messageChainOf(), false), false).size
        )

        createTestInstance().run {
            assertEquals(5, size)
            assertFalse { slowList.isInitialized() }
        }
    }

    @Test
    fun slowListTest() {
        assertEquals(messageChainOf(), CombinedMessage(messageChainOf(), messageChainOf(), false).slowList.value)
        assertEquals(
            listOf(
                PlainText("foo"),
                PlainText("bar"),
                AtAll,
                PlainText("zoo"),
                At(2),
                At(1),
            ),
            complexCombined.slowList.value.toList()
        )
    }

    @Test
    fun consistencyTest() {
        val first = createTestInstance()
        val second = createTestInstance()
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertEquals(first.toString(), second.toString())
        assertEquals(first.contentToString(), second.contentToString())
        assertEquals(first.size, second.size)
    }

    ///////////////////////////////////////////////////////////////////////////
    // functions
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun subList() {
        assertEquals(
            createComplexCombined().slowList.value.subList(2, 4),
            complexCombined.subList(2, 4)
        )
        assertFalse {
            complexCombined.slowList.isInitialized()
        }
        complexCombined.slowList.value // initialize
        assertEquals(
            createComplexCombined().slowList.value.subList(2, 4),
            complexCombined.subList(2, 4)
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // visiting
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun acceptChildrenTest() {
        val list = buildList {
            complexCombined.acceptChildren(object : MessageVisitorUnit() {
                override fun visitMessage(message: Message, data: Unit) {
                    add(message)
                    super.visitMessage(message, data)
                }
            })
        }
        assertEquals(
            listOf(
                PlainText("foo"),
                createTestInstance()
            ),
            list
        )
    }

    @OptIn(MessageChainConstructor::class)
    @Test
    fun acceptChildrenRecursiveTest() {
        val list = buildList {
            complexCombined.accept(object : RecursiveMessageVisitor<Unit>() {
                override fun visitMessage(message: Message, data: Unit) {
                    println("visitMessage: (${message::class.simpleName}) $message")
                    super.visitMessage(message, data)
                }

                override fun visitSingleMessage(message: SingleMessage, data: Unit) {
                    add(message)
                    super.visitSingleMessage(message, data)
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
            complexCombined.iterator().asSequence().toList()
        )
        assertFalse { complexCombined.slowList.isInitialized() }
    }
}