/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.internal.message.ReceiveMessageTransformer.cleanupRubbishMessageElements
import net.mamoe.mirai.internal.message.data.LongMessageInternal
import net.mamoe.mirai.internal.message.data.OnlineAudioImpl
import net.mamoe.mirai.internal.message.protocol.impl.PokeMessageProtocol.Companion.UNSUPPORTED_POKE_MESSAGE_PLAIN
import net.mamoe.mirai.internal.message.protocol.impl.RichMessageProtocol.Companion.UNSUPPORTED_MERGED_MESSAGE_PLAIN
import net.mamoe.mirai.internal.message.source.OfflineMessageSourceImplData
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.data.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CleanupRubbishMessageElementsTest : AbstractTest() {
    //region
    private val replySource = OfflineMessageSourceImplData(
        kind = MessageSourceKind.GROUP,
        ids = intArrayOf(1),
        botId = 1,
        time = 1,
        fromId = 87,
        targetId = 7454,
        originalMessage = messageChainOf(),
        internalIds = intArrayOf(8711)
    )

    private val source = OfflineMessageSourceImplData(
        kind = MessageSourceKind.GROUP,
        ids = intArrayOf(1),
        botId = 1,
        time = 1,
        fromId = 2,
        targetId = 3,
        originalMessage = messageChainOf(),
        internalIds = intArrayOf(9)
    )
    //endregion

    private fun assertCleanup(excepted: MessageChain, source: MessageChain) {
        assertEquals(
            excepted,
            source.cleanupRubbishMessageElements()
        )
        assertEquals(
            noMessageSource(excepted),
            noMessageSource(source).cleanupRubbishMessageElements()
        )
    }

    @Test
    fun testQuoteAtSpace() {
        // Windows PC QQ
        assertCleanup(
            messageChainOf(source, QuoteReply(replySource), PlainText("Hello!")),
            messageChainOf(source, At(123), PlainText(" "), QuoteReply(replySource), PlainText("Hello!")),
        )

        // QQ Android
        assertCleanup(
            messageChainOf(source, QuoteReply(replySource), PlainText("Hello!")),
            messageChainOf(source, QuoteReply(replySource), At(1234567890), PlainText(" Hello!")),
        )
    }

    @Test
    fun testTIMAudio() {
        val audio = OnlineAudioImpl("0", byteArrayOf(), 0, AudioCodec.SILK, "", 0, null)
        assertCleanup(
            messageChainOf(source, audio),
            messageChainOf(source, audio, UNSUPPORTED_VOICE_MESSAGE_PLAIN),
        )
    }

    @Test
    fun testPokeMessageCleanup() {
        val poke = PokeMessage("", 1, 1)
        assertCleanup(
            messageChainOf(source, poke),
            messageChainOf(source, poke, UNSUPPORTED_POKE_MESSAGE_PLAIN),
        )
    }

    @Test
    fun testVipFaceCleanup() {
        val vf = VipFace(VipFace.Kind(1, "Test!"), 50)
        assertCleanup(
            messageChainOf(source, vf),
            messageChainOf(source, vf, PlainText("----CCCCCTest!")),
        )
    }

    @Test
    fun testLongMessageInternalCleanup() {
        val li = LongMessageInternal("", "")
        assertCleanup(
            messageChainOf(source, li),
            messageChainOf(source, li, UNSUPPORTED_MERGED_MESSAGE_PLAIN),
        )
    }

    @Test
    fun testCompressContinuousPlainText() {
        assertCleanup(
            messageChainOf(PlainText("1234567890")),
            "12 3   45 6  789 0".split(" ").map(::PlainText).toMessageChain(),
        )
        assertCleanup(
            msg(source, At(123456), "Hello! How are you?"),
            msg(source, At(123456), "Hello", "!", " ", "How", " ", "are ", "you?"),
        )
    }

    @Test
    fun testEmptyPlainTextRemoved() {
        assertCleanup(
            messageChainOf(),
            "                     ".split(" ").map(::PlainText).toMessageChain(),
        )
        assertCleanup(
            msg(AtAll),
            msg("", AtAll, "", "", ""),
        )
    }

    @Test
    fun testBlankPlainTextLiving() {
        assertCleanup(
            msg("    "),
            msg("", " ", "  ", " "),
        )
    }

    //region

    private fun msg(vararg msgs: Any?): MessageChain {
        return msgs.map { elm ->
            when (elm) {
                is Message -> elm
                is String -> PlainText(elm)
                else -> PlainText(elm.toString())
            }
        }.toMessageChain()
    }

    private fun noMessageSource(c: MessageChain): MessageChain {
        return c.filterNot { it is MessageSource }.toMessageChain()
    }

    //endregion
}