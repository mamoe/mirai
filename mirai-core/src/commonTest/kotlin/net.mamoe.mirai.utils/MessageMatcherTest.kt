package net.mamoe.mirai.utils

import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.asMessageChain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageMatcherTest {
    @Test
    fun testMessageMatcher() {
        val matcher = MessageMatcher(
            StringMessageMatcherElement("把"),
            BlankMessageMatcherElement,
            PersonMessageMatcherElement("target"),
            BlankMessageMatcherElement,
            StringMessageMatcherElement("禁言"),
            StringMessageMatcherElement("喽") withRange 0..1
        )
        val resultMap = mutableMapOf<String, Any>()
        assertTrue(PlainText("把 233禁言").asMessageChain().match(matcher, resultMap))
        assertEquals("233", (resultMap["target"] as PersonMessageMatcherElement.PersonReference).display)
        resultMap.clear()
        assertTrue(PlainText("把 你  禁言喽").asMessageChain().match(matcher, resultMap))
        // 对方说你，指的是机器人（我）
        assertEquals("我", (resultMap["target"] as PersonMessageMatcherElement.PersonReference).display)
        resultMap.clear()
        assertFalse(PlainText("把 我  禁言喽喽喽").asMessageChain().match(matcher, resultMap))
        resultMap.clear()
    }

    @Test
    fun testMessageMatcherBuilder() {
        val matcher = MessageMatcher {
            add("我是".toElement())
            add('弟'.toElement() withRange 1..2)
            add("，你呢".toElement())
            add('？'.toElement() withRange 0..1)
            add(PersonMessageMatcherElement() names "target")
        }
        println(matcher.elements)
        val resultMap = mutableMapOf<String, Any>()
        assertTrue(PlainText("我是弟弟，你呢？233").asMessageChain().match(matcher, resultMap))
        (resultMap["target"] as PersonMessageMatcherElement.PersonReference).run {
            assertEquals(getUIN(null), 233)
            assertEquals(display, "233")
        }
        resultMap.clear()
        assertTrue(PlainText("我是弟，你呢2333").asMessageChain().match(matcher, resultMap))
        (resultMap["target"] as PersonMessageMatcherElement.PersonReference).run {
            assertEquals(getUIN(null), 2333)
            assertEquals(display, "2333")
        }
        resultMap.clear()
    }
}