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
import net.mamoe.mirai.message.data.VipFace
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class VipFaceProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(VipFaceProtocol(), TextProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun `test decode`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    commonElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CommonElem(
                        serviceType = 23,
                        pbElem = "08 09 10 01 1A 06 E6 A6 B4 E8 8E B2".hexToBytes(),
                        businessType = 9,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[榴莲]x1",
                        pbReserve = "0A 34 5B E6 A6 B4 E8 8E B2 5D E8 AF B7 E4 BD BF E7 94 A8 E6 9C 80 E6 96 B0 E7 89 88 E6 89 8B E6 9C BA 51 51 E4 BD 93 E9 AA 8C E6 96 B0 E5 8A 9F E8 83 BD E3 80 82".hexToBytes(),
                    ),
                )
            )
            message(VipFace(VipFace.LiuLian, 1))
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    @TestFactory
    fun `test serialization for VipFace`(): DynamicTestsResult {
        val data = VipFace(
            VipFace.LiuLian, 1
        )

        val serialName = VipFace.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }

}