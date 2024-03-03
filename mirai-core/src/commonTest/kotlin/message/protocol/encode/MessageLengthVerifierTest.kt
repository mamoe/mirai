/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.encode

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.message.protocol.encode.MessageLengthVerifierImpl.Companion.numberOfDigitsInDecimal
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.visitor.accept
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @see MessageLengthVerifier
 */
internal class MessageLengthVerifierTest : AbstractTest(), GroupExtensions {
    private val bot = MockBot { }
    private val group = bot.addGroup(123L, 1111L).apply {
        addMember(1111L, permission = MemberPermission.OWNER)
    }

    private companion object {
        private val fiveThousandChars = PlainText("a".repeat(5000))
        private val anImage =
            // [mirai:image:{9D97AF44-0007-5F86-6567-C0BD3F6A5C5C}.gif, width=211, height=243, size=108292, type=GIF, isEmoji=true]
            Image("{9D97AF44-0007-5F86-6567-C0BD3F6A5C5C}.gif") { // guess what it is?
                width = 211
                height = 243
                size = 108292
                type = ImageType.GIF
                isEmoji = true
            }
    }

    @Test
    fun numberOfDigitsInDecimal() {
        assertEquals(1, 0L.numberOfDigitsInDecimal)
        assertEquals(2, 10L.numberOfDigitsInDecimal)
        assertEquals(2, 11L.numberOfDigitsInDecimal)
        assertEquals(4, 1000L.numberOfDigitsInDecimal)
        assertEquals(4, 1001L.numberOfDigitsInDecimal)
        assertEquals(4, 1999L.numberOfDigitsInDecimal)
    }

    @Test
    fun `initial values`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        assertEquals(0, verifier.uiChars)
        assertEquals(0, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
        assertTrue(verifier.isLengthValid())
    }

    @Test
    fun `count PlainTexts`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(fiveThousandChars)
        chain.accept(verifier)
        assertEquals(5000, verifier.uiChars)
        assertEquals(0, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
    }

    @Test
    fun `count Images`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(anImage, anImage)
        chain.accept(verifier)
        assertEquals(0, verifier.uiChars)
        assertEquals(2, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
    }

    @Test
    fun `count Images and PlainTexts`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(fiveThousandChars, anImage)
        chain.accept(verifier)
        assertEquals(5000, verifier.uiChars)
        assertEquals(1, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
    }

    @Test
    fun failfast() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = true)

        val chain = messageChainOf(fiveThousandChars, anImage, fiveThousandChars, fiveThousandChars, anImage)
        chain.accept(verifier)
        assertEquals(fiveThousandChars.content.length * 2L, verifier.uiChars)
        assertEquals(1, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
        assertFalse(verifier.isLengthValid())
    }

    @Test
    fun `disable failfast`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(fiveThousandChars, anImage, fiveThousandChars, fiveThousandChars, anImage)
        chain.accept(verifier)
        assertEquals(fiveThousandChars.content.length * 3L, verifier.uiChars)
        assertEquals(2, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
        assertFalse(verifier.isLengthValid())
    }

    @Test
    fun `limits are inclusive`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = true)

        val chain = messageChainOf(fiveThousandChars, anImage)
        chain.accept(verifier)
        assertEquals(5000, verifier.uiChars)
        assertEquals(1, verifier.uiImages)
        assertEquals(0, verifier.uiForwardNodes)
        assertTrue(verifier.isLengthValid())
    }


    @Test
    fun `count recursively ForwardMessage nodes`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(buildForwardMessage(group) {
            1111 says fiveThousandChars
            1111 says anImage
            1111 says fiveThousandChars
            1111 says fiveThousandChars
            1111 says anImage
        })

        chain.accept(verifier)
        assertEquals(fiveThousandChars.content.length * 3L, verifier.uiChars)
        assertEquals(2, verifier.uiImages)
        assertEquals(5, verifier.uiForwardNodes)
        assertFalse(verifier.isLengthValid())
    }

    @Test
    fun `count deeply recursively ForwardMessage nodes`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = false)

        val chain = messageChainOf(buildForwardMessage(group) {
            1111 says fiveThousandChars
            1111 says anImage
            1111 says fiveThousandChars
            1111 says fiveThousandChars
            1111 says anImage

            1111 says buildForwardMessage(group) {
                1111 says fiveThousandChars
                1111 says anImage
                1111 says fiveThousandChars
                1111 says fiveThousandChars
                1111 says anImage
            }
        })

        chain.accept(verifier)
        assertEquals(fiveThousandChars.content.length * 3L * 2, verifier.uiChars)
        assertEquals(2 * 2, verifier.uiImages)
        assertEquals(6 + 5, verifier.uiForwardNodes)
        assertFalse(verifier.isLengthValid())
    }

    @Test
    fun `count deeply recursively ForwardMessage nodes failfast`() {
        val limits = MessageLengthLimits(
            uiChars = 5000,
            uiImages = 50,
            uiForwardNodes = 200,
        )
        val verifier = MessageLengthVerifier(null, limits, failfast = true)

        val chain = messageChainOf(buildForwardMessage(group) {
            1111 says fiveThousandChars
            1111 says anImage
            1111 says fiveThousandChars
            1111 says fiveThousandChars
            1111 says anImage

            1111 says buildForwardMessage(group) {
                1111 says fiveThousandChars
                1111 says anImage
                1111 says fiveThousandChars
                1111 says fiveThousandChars
                1111 says anImage
            }
        })

        chain.accept(verifier)
        assertEquals(fiveThousandChars.content.length * 1L, verifier.uiChars)
        assertEquals(1, verifier.uiImages)
        assertEquals(2, verifier.uiForwardNodes)
        assertFalse(verifier.isLengthValid())
    }
}