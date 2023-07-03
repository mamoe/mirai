/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.message.data.AnimatedSticker
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class AnimatedStickerProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(AnimatedStickerProtocol(), TextProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun `group AnimatedSticker receive from Android client`() {
        buildCodingChecks {
            elem(
                "AA03200825121A0A01311202313618052001280132003A072FE6B581E6B3AA48011801".hexToBytes()
                    .loadAs(net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem.serializer())
            )
            message(
                AnimatedSticker(id = Face.LIU_LEI)
            )
        }.doDecoderChecks()
    }

    @TestFactory
    fun `test serialization`(): DynamicTestsResult {
        val data = AnimatedSticker(0)
        val serialName = AnimatedSticker.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}