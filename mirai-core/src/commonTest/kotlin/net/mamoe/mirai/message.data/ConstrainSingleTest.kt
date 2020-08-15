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
import kotlin.test.assertSame
import kotlin.test.assertTrue


internal class TestConstrainSingleMessage : ConstrainSingle<TestConstrainSingleMessage>, Any() {
    companion object Key : Message.Key<TestConstrainSingleMessage> {
        override val typeName: String
            get() = "TestMessage"
    }

    override fun toString(): String = "<TestConstrainSingleMessage#${super.hashCode()}>"

    override val key: Message.Key<TestConstrainSingleMessage>
        get() = Key
}


internal class ConstrainSingleTest {


    @Test
    fun testCombine() {
        val result = PlainText("te") + PlainText("st")
        assertTrue(result is CombinedMessage)
        assertEquals("te", result.left.contentToString())
        assertEquals("st", result.tail.contentToString())
        assertEquals(2, result.size)
        assertEquals("test", result.contentToString())
    }

    @Test
    fun testSinglePlusChain() {
        val result = PlainText("te") + buildMessageChain {
            add(TestConstrainSingleMessage())
            add("st")
        }
        assertEquals(3, result.size)
        assertEquals(result.contentToString(), "test")
    }

    @Test
    fun testSinglePlusChainConstrain() {
        val chain = buildMessageChain {
            add(TestConstrainSingleMessage())
            add("st")
        }
        val result = TestConstrainSingleMessage() + chain
        assertSame(chain, result)
        assertEquals(2, result.size)
        assertEquals(result.contentToString(), "st")
        assertTrue { result.first() is TestConstrainSingleMessage }
    }

    @Test
    fun testSinglePlusSingle() {
        val new = TestConstrainSingleMessage()
        val combined = (TestConstrainSingleMessage() + new)

        assertTrue(combined is SingleMessageChainImpl)
        assertSame(new, combined.delegate)
    }

    @Test
    fun testChainPlusSingle() {
        val new = TestConstrainSingleMessage()

        val result = buildMessageChain {
            add(" ")
            add(Face(Face.hao))
            add(TestConstrainSingleMessage())
            add(
                PlainText("ss")
                        + " "
            )
        } + buildMessageChain {
            add(PlainText("p "))
            add(new)
            add(PlainText("test"))
        }

        assertEquals(7, result.size)
        assertEquals(" [OK]ss p test", result.contentToString())
        result as MessageChainImplByCollection
        assertSame(new, result.delegate.toTypedArray()[2])
    }

    @Test // net.mamoe.mirai/message/data/MessageChain.kt:441
    fun testConstrainSingleInSequence() {
        val last = TestConstrainSingleMessage()
        val sequence: Sequence<SingleMessage> = sequenceOf(
            TestConstrainSingleMessage(),
            TestConstrainSingleMessage(),
            last
        )

        val result = sequence.constrainSingleMessages()
        assertEquals(result.count(), 1)
        assertSame(result.single(), last)
    }

    @Test // net.mamoe.mirai/message/data/MessageChain.kt:441
    fun testConstrainSingleOrderInSequence() {
        val last = TestConstrainSingleMessage()
        val sequence: Sequence<SingleMessage> = sequenceOf(
            TestConstrainSingleMessage(), // last should replace here
            PlainText("test"),
            TestConstrainSingleMessage(),
            last
        )

        val result = sequence.constrainSingleMessages()
        assertEquals(result.count(), 2)
        assertSame(result.first(), last)
    }


    @Test
    fun testConversions() {
        val lastSingle = TestConstrainSingleMessage()
        val list: List<SingleMessage> = listOf(
            PlainText("test"),
            TestConstrainSingleMessage(),
            TestConstrainSingleMessage(),
            PlainText("foo"),
            TestConstrainSingleMessage(),
            lastSingle
        )

        // Collection<SingleMessage>.asMessageChain()
        assertEquals("test${lastSingle}foo", list.asMessageChain().toString())

        // Collection<Message>.asMessageChain()
        @Suppress("USELESS_CAST")
        assertEquals(
            "test${lastSingle}foo",
            list.map { it as Message }.asMessageChain().toString()
        )

        // Iterable<SingleMessage>.asMessageChain()
        assertEquals("test${lastSingle}foo", list.asIterable().asMessageChain().toString())

        // Iterable<Message>.asMessageChain()
        @Suppress("USELESS_CAST")
        assertEquals(
            "test${lastSingle}foo",
            list.map { it as Message }.asIterable().asMessageChain().toString()
        )

        // Sequence<SingleMessage>.asMessageChain()
        assertEquals("test${lastSingle}foo", list.asSequence().asMessageChain().toString())

        // Sequence<Message>.asMessageChain()
        @Suppress("USELESS_CAST")
        assertEquals(
            "test${lastSingle}foo",
            list.map { it as Message }.asSequence().asMessageChain().toString()
        )
    }
}