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
import net.mamoe.mirai.internal.message.flags.AllowSendFileMessage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class FileMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(FileMessageProtocol(), TextProtocol())

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
                    elemFlags2 = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ElemFlags2(
                        compatibleId = 1,
                        msgRptCnt = 1,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    transElemInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.TransElem(
                        elemType = 24,
                        elemValue = "01 00 7A 08 06 12 0C 73 65 73 73 69 6F 6E 2E 6C 6F 63 6B 1A 05 38 42 79 74 65 3A 61 12 5F 08 66 12 25 2F 38 34 33 35 32 37 64 38 2D 64 39 31 35 2D 31 31 65 63 2D 62 32 34 30 2D 35 34 35 32 30 30 37 62 64 61 61 34 18 08 22 0C 73 65 73 73 69 6F 6E 2E 6C 6F 63 6B 28 00 3A 00 42 20 39 30 33 65 39 36 34 35 36 38 38 62 63 62 32 35 35 64 30 36 64 31 64 61 31 35 33 66 64 36 32 64".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                        glamourLevel = 3,
                        pbReserve = "08 0A 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 98 03 00 A0 03 20 B0 03 00 C0 03 00 D0 03 00 E8 03 00 8A 04 02 10 0B 90 04 80 01 B8 04 02 C0 04 01 CA 04 00 F8 04 00 88 05 00".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        nick = "Nick",
                        level = 1,
                        flags = 8,
                        groupMask = 1,
                    ),
                )
            )
            message(FileMessage("/843527d8-d915-11ec-b240-5452007bdaa4", 102, "session.lock", 8))
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    @TestFactory
    fun `test serialization`(): DynamicTestsResult {
        val data = FileMessage("id", 1, "name", 2)
        val serialName = FileMessage.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }

    @Test
    fun `test manual send forbidden`() {
        val data = FileMessage("id", 1, "name", 2)
        assertFailsWith<IllegalStateException> {
            FileMessageProtocol.verifyFileMessage(data.toMessageChain())
        }
    }

    @Test
    fun `test auto send allowed`() {
        val data = FileMessage("id", 1, "name", 2)
        FileMessageProtocol.verifyFileMessage(AllowSendFileMessage + data)
    }

    @Test
    fun `test auto send allowed2`() {
        val data = FileMessage("id", 1, "name", 2)
        FileMessageProtocol.verifyFileMessage(data + AllowSendFileMessage)
    }
}