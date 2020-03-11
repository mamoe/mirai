package net.mamoe.mirai.utils

import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.asMessageChain
import kotlin.test.*

class MessageMatcherTest {
    @Test
    fun testBaseMatch() {
        MessageMatcher(
            StringMessageMatcherElement("把"),
            BlankMessageMatcherElement,
            PersonMessageMatcherElement("target"),
            BlankMessageMatcherElement,
            StringMessageMatcherElement("禁言"),
            StringMessageMatcherElement("喽").withRange(0..1)
        ).run {
            PlainText("把 233禁言").asMessageChain().match(this).run {
                assertNotNull(this)
                assertEquals("233", (this["target"] as PersonMessageMatcherElement.PersonReference).display)
                println("can it run?")
            }
            PlainText("把 你  禁言喽").asMessageChain().match(this).run {
                assertNotNull(this)
                // 对方说你，指的是机器人（我）
                assertEquals("我", (this["target"] as PersonMessageMatcherElement.PersonReference).display)
            }
            PlainText("把 我  禁言喽喽喽").asMessageChain().match(this).run { assertNull(this) }
            PlainText("把 我禁 言").asMessageChain().match(this).run { assertNull(this) }
        }
        MessageMatcher(
            StringMessageMatcherElement("管理员都"),
            CharMessageMatcherElement('有', '是'),
            CharMessageMatcherElement('谁'),
            CharMessageMatcherElement('啊').withRange(0..1),
            CharMessageMatcherElement('?', '？').withRange(0..1)
        ).run {
            for (i in setOf(
                "管理员都有谁啊？",
                "管理员都是谁啊",
                "管理员都是谁",
                "管理员都有谁?"
            )) assertTrue(PlainText(i).asMessageChain().matchTo(this))
        }
    }

    @Test
    fun testMessageMatcherBuilder() {
        val matcher = MessageMatcher {
            add("我是")
            add('弟'.toElement().withRange(1..2))
            add("，你呢")
            add('？'.toElement().withRange(0..1))
            add(PersonMessageMatcherElement().named("target"))
        }
        PlainText("我是弟弟，你呢？233").asMessageChain().match(matcher).run {
            assertNotNull(this)
            (this["target"] as PersonMessageMatcherElement.PersonReference).run {
                assertEquals(getId(null), 233)
                assertEquals(display, "233")
            }
        }
        PlainText("我是弟，你呢2333").asMessageChain().match(matcher).run {
            assertNotNull(this)
            (this["target"] as PersonMessageMatcherElement.PersonReference).run {
                assertEquals(getId(null), 2333)
                assertEquals(display, "2333")
            }
        }
    }

    @Test
    fun testBothSideMatch() {
        val matcher = MessageMatcher {
            add("你好")
            add('！'.toElement().withRange(0..1))
        }
        assertTrue(PlainText("你好！我是xxx").asMessageChain().matchFromBegin(matcher))
        assertTrue(PlainText("我是xxx！你好").asMessageChain().matchFromEnd(matcher))
    }
}