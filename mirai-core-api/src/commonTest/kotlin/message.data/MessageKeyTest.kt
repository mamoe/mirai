/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.safeCast
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private open class TestStandaloneConstrainSingleMessage : ConstrainSingle, MessageContent {
    companion object Key : AbstractMessageKey<TestStandaloneConstrainSingleMessage>({ it.safeCast() })

    override fun toString(): String = "<TestStandaloneConstrainSingleMessage#${super.hashCode()}>"
    override fun contentToString(): String = ""

    override val key: MessageKey<TestStandaloneConstrainSingleMessage> get() = Key
}

private class TestPolymorphicConstrainSingleMessage : ConstrainSingle, TestStandaloneConstrainSingleMessage(),
    MessageContent {
    companion object Key :
        AbstractPolymorphicMessageKey<TestStandaloneConstrainSingleMessage, TestPolymorphicConstrainSingleMessage>(
            TestStandaloneConstrainSingleMessage, { it.safeCast() }
        )

    override fun toString(): String = "<TestPolymorphicConstrainSingleMessage#${super.hashCode()}>"
    override fun contentToString(): String = ""

    override val key: MessageKey<TestPolymorphicConstrainSingleMessage> get() = Key
}

private class TestPolymorphicConstrainSingleMessageOverridingMessageContent : ConstrainSingle, MessageContent,
    TestStandaloneConstrainSingleMessage() {
    companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, TestPolymorphicConstrainSingleMessageOverridingMessageContent>(
            MessageContent, { it.safeCast() }
        )

    override fun toString(): String =
        "<TestPolymorphicConstrainSingleMessageOverridingMessageContent#${super.hashCode()}>"

    override fun contentToString(): String = ""

    override val key: MessageKey<TestPolymorphicConstrainSingleMessageOverridingMessageContent> get() = Key
}


internal class MessageKeyTest {
    @Test
    fun `test polymorphism get`() {
        val constrainSingle: TestStandaloneConstrainSingleMessage
        val chain = buildMessageChain {
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestStandaloneConstrainSingleMessage().also { constrainSingle = it }
        }

        assertEquals(constrainSingle, chain[MessageContent])
        assertEquals(constrainSingle, chain[TestStandaloneConstrainSingleMessage])
    }

    @Test
    fun `test polymorphism override base`() {
        val constrainSingle: TestPolymorphicConstrainSingleMessage
        val chain = buildMessageChain {
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestPolymorphicConstrainSingleMessage().also { constrainSingle = it }
        }

        assertEquals(constrainSingle, chain[MessageContent])
        assertEquals(constrainSingle, chain[TestPolymorphicConstrainSingleMessage])
    }

    @Test
    fun `test polymorphism override message content`() {
        val constrainSingle: TestPolymorphicConstrainSingleMessageOverridingMessageContent
        val chain = buildMessageChain {
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestStandaloneConstrainSingleMessage()
            +PlainText("test")
            +TestPolymorphicConstrainSingleMessageOverridingMessageContent().also { constrainSingle = it }
        }

        assertEquals(constrainSingle, chain[MessageContent])
        assertEquals<Any?>(constrainSingle, chain[TestStandaloneConstrainSingleMessage])
        assertEquals(1, chain.size)
    }
}