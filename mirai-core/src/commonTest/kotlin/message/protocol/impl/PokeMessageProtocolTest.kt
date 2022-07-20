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
import net.mamoe.mirai.message.data.PokeMessage
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class PokeMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(TextProtocol(), PokeMessageProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun `test PokeMessage`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    commonElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CommonElem(
                        serviceType = 2,
                        pbElem = "08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00".hexToBytes(),
                        businessType = 1,
                    ),
                )
            )
            message(PokeMessage("戳一戳", 1, -1))
            useOrdinaryEquality()
        }.doDecoderChecks()
    }


    // Unsupported kinds
//    @Test
//    fun `test PokeMessage`() {
//        buildChecks {
//            elem(
//                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
//                    commonElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CommonElem(
//                        serviceType = 23,
//                        pbElem = "08 0A 10 01 1A 09 E7 95 A5 E7 95 A5 E7 95 A5".hexToBytes(),
//                        businessType = 10,
//                    ),
//                )
//            )
//            message(PokeMessage("略略略", -1, 1))
//            useOrdinaryEquality()
//        }.doDecoderChecks()
//    }

    @Test
    fun encode() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    commonElem = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CommonElem(
                        serviceType = 2,
                        pbElem = "08 01 20 FF FF FF FF FF FF FF FF FF 01 2A 09 E6 88 B3 E4 B8 80 E6 88 B3 32 05 37 2E 32 2E 30".hexToBytes(),
                        businessType = 1,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[戳一戳]请使用最新版手机QQ体验新功能。",
                    ),
                )
            )
            message(PokeMessage.ChuoYiChuo)
        }.doEncoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    @TestFactory
    fun `test serialization for PokeMessage`(): DynamicTestsResult {
        val data = PokeMessage.ChuoYiChuo
        val serialName = PokeMessage.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}