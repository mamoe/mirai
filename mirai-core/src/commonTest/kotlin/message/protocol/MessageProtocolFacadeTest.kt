/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol

import net.mamoe.mirai.internal.test.AbstractTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MessageProtocolFacadeTest : AbstractTest() {


    @Test
    fun `can load`() {
        assertEquals(
            """
                QuoteReplyProtocol
                CustomMessageProtocol
                FileMessageProtocol
                FlashImageProtocol
                FaceProtocol
                ImageProtocol
                MarketFaceProtocol
                MusicShareProtocol
                PokeMessageProtocol
                IgnoredMessagesProtocol
                PttMessageProtocol
                RichMessageProtocol
                TextProtocol
                VipFaceProtocol
                ForwardMessageProtocol
                LongMessageProtocol
                UnsupportedMessageProtocol
                GeneralMessageSenderProtocol
            """.trimIndent(),
            MessageProtocolFacadeImpl().loaded.joinToString("\n") { it::class.simpleName.toString() }
        )
    }
}