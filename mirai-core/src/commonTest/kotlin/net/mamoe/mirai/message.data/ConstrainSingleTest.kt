/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


@OptIn(MiraiExperimentalAPI::class)
internal class TestConstrainSingleMessage : ConstrainSingle<TestConstrainSingleMessage>, Any() {
    companion object Key : Message.Key<TestConstrainSingleMessage> {
        override val typeName: String
            get() = "TestMessage"
    }

    override fun toString(): String = "<TestConstrainSingleMessage#${super.hashCode()}>"

    override fun contentToString(): String {
        TODO("Not yet implemented")
    }

    override val key: Message.Key<TestConstrainSingleMessage>
        get() = Key
    override val length: Int
        get() = TODO("Not yet implemented")

    override fun get(index: Int): Char {
        TODO("Not yet implemented")
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        TODO("Not yet implemented")
    }

    override fun compareTo(other: String): Int {
        TODO("Not yet implemented")
    }
}

internal class ConstrainSingleTest {

    @OptIn(MiraiExperimentalAPI::class)
    @Test
    fun testConstrainSingleInPlus() {
        val new = TestConstrainSingleMessage()
        val combined = TestConstrainSingleMessage() + new

        assertEquals(combined.left, EmptyMessageChain)
        assertSame(combined.tail, new)
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
}