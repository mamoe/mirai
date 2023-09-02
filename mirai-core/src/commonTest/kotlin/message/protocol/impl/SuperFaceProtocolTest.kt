/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.message.data.Face
import net.mamoe.mirai.message.data.SuperFace
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class SuperFaceProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(SuperFaceProtocol(), TextProtocol())

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
                "AA 03 20 08 25 12 1A 0A 01 31 12 02 31 36 18 05 20 01 28 01 32 00 3A 07 2F E6 B5 81 E6 B3 AA 48 01 18 01".hexToBytes()
                    .loadAs(net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem.serializer())
            )
            elem(
                "AA 03 23 08 25 12 1D 0A 01 31 12 02 31 33 18 72 20 01 28 02 32 01 35 3A 07 2F E7 AF AE E7 90 83 42 00 48 01 18 02".hexToBytes()
                    .loadAs(net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem.serializer())
            )
            message(
                SuperFace.from(Face(Face.LAN_QIU))
            )
        }.doDecoderChecks()
    }

    @TestFactory
    fun `test serialization`(): DynamicTestsResult {
        val data = SuperFace.from(Face(Face.LAN_QIU))
        val serialName = SuperFace.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}