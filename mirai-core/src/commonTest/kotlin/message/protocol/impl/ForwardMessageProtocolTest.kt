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
import net.mamoe.mirai.internal.message.LightMessageRefiner.dropMiraiInternalFlags
import net.mamoe.mirai.internal.message.data.ForwardMessageInternal
import net.mamoe.mirai.internal.message.flags.IgnoreLengthCheck
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.castUp
import net.mamoe.mirai.utils.getRandomString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ForwardMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> =
        arrayOf(
            TextProtocol(),
            ImageProtocol(),
            ForwardMessageProtocol(),
            GeneralMessageSenderProtocol(),
        )

    init {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun precondition() {
        assertEquals(getRandomString(5000, Random(1)), getRandomString(5000, Random(1)))
        assertMessageEquals(
            "test".toPlainText() + getRandomString(5000, Random(1)) +
                    Image("{40A7C56B-45C9-23AE-0CFA-23F095B71035}.jpg").repeat(200),
            "test".toPlainText() + getRandomString(5000, Random(1)) +
                    Image("{40A7C56B-45C9-23AE-0CFA-23F095B71035}.jpg").repeat(200)
        )
    }

    @Test
    fun `can convert ForwardMessage to ForwardMessageInternal`() {
        var message = buildForwardMessage(defaultTarget.cast()) {
            currentTime = 16000000
            1 named "Name1" says "Hello"
        }.toMessageChain()

        message += IgnoreLengthCheck

        runWithFacade {
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)

                assertMessageEquals(
                    ForwardMessageInternal(
                        """<?xml version="1.0" encoding="utf-8"?> <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="[聊天记录]"      m_resid="(size=1)501389E3070B20D87A80A67961F4EA0E" m_fileName="160023"      tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">     <item layout="1" advertiser_id="0" aid="0">         <title size="34" maxLines="2" lineSpace="12">群聊的聊天记录</title>         <title size="26" color="#777777" maxLines="2" lineSpace="12">Name1: Hello</title>         <hr hidden="false" style="0"/>         <summary size="26" color="#777777">查看1条转发消息</summary>     </item>     <source name="聊天记录" icon="" action="" appid="-1"/> </msg>""",
                        "(size=1)501389E3070B20D87A80A67961F4EA0E",
                        null,
                        origin = message[ForwardMessage]
                    ) + IgnoreLengthCheck, context.currentMessageChain
                )
            }
        }
    }

    @Test
    fun `can convert empty ForwardMessage`() {
        val message = buildForwardMessage(defaultTarget.cast()) {}.toMessageChain()

        runWithFacade {
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message.dropMiraiInternalFlags(), receipt.source.originalMessage)

                assertMessageEquals(
                    ForwardMessageInternal(
                        """<?xml version="1.0" encoding="utf-8"?> <msg serviceID="35" templateID="1" action="viewMultiMsg" brief="[聊天记录]"      m_resid="(size=0)D41D8CD98F00B204E9800998ECF8427E" m_fileName="160023"      tSum="3" sourceMsgId="0" url="" flag="3" adverSign="0" multiMsgFlag="0">     <item layout="1" advertiser_id="0" aid="0">         <title size="34" maxLines="2" lineSpace="12">群聊的聊天记录</title>                  <hr hidden="false" style="0"/>         <summary size="26" color="#777777">查看0条转发消息</summary>     </item>     <source name="聊天记录" icon="" action="" appid="-1"/> </msg>""",
                        "(size=0)D41D8CD98F00B204E9800998ECF8427E",
                        null,
                        origin = message[ForwardMessage]
                    ), context.currentMessageChain
                )
            }
        }
    }

    // // TODO: 2022/5/23 test for download ForwardMessage
//    @Test
//    fun `can receive and download ForwardMessage`() {
//        val message = runTest {
//            runWithFacade {
//                net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
//                    msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
//                        fromUin = 1230001,
//                        toUin = 1230002,
//                        msgType = 166,
//                        c2cCmd = 11,
//                        msgSeq = 34437,
//                        msgTime = 1653334690,
//                        msgUid = 72057594524997436,
//                        wseqInC2cMsghead = 34437,
//                    ),
//                    msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
//                        richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
//                            attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
//                                codePage = 0,
//                                time = 1653334690,
//                                random = 487069500,
//                                effect = 0,
//                                charSet = 134,
//                                pitchAndFamily = 2,
//                                fontName = "宋体",
//                            ),
//                            elems = mutableListOf(
//                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
//                                    richMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichMsg(
//                                        template1 = "01 78 9C B5 92 4F 6F D3 30 18 C6 BF 8A 65 0E 39 4C 23 4D FA 2F A0 38 53 B7 29 EB D8 12 89 65 45 FC 11 9A DC C4 C9 2C D9 49 65 3B DD DA DB 24 0E 08 0E 88 03 17 04 42 48 70 40 42 C0 89 DB 3E 4E BB 7E 0C 1C 77 12 E3 80 C4 85 F8 10 47 AF F3 F3 F3 3C EF EB 6F 9D 73 06 A6 44 48 5A 95 C8 72 6E B7 2C 40 CA B4 CA 68 59 20 6B 74 1C 6E 7A 16 90 0A 97 19 66 55 49 90 35 23 D2 02 5B 81 CF 65 01 24 11 53 9A 92 FD 5D 04 DB 5D 08 14 E1 13 86 95 F9 76 20 C0 A9 6A 98 70 4A C9 59 54 33 45 23 59 40 30 16 94 E4 08 3E 59 5D BC 58 7C FA B2 FA F6 63 71 F9 E6 29 04 FC 44 10 49 33 04 C7 C7 E1 91 3C 4C F2 78 F4 28 89 C2 68 7B 63 E0 B9 03 B1 71 FF E1 78 87 1E A4 C5 38 8E 6B 6F 6F BB FF 60 CF 3E 9A 77 0E 1E C7 C3 D1 4E 31 BC 37 8F C2 DD 3C 12 83 44 34 A8 9C 32 12 63 4E 10 EC 3B 2D BD BC 8E 73 C7 E9 BA 1D A7 D7 ED 35 3A 93 9A 23 E8 42 20 AB 5A A4 44 CB DA D7 17 B7 20 A8 05 43 10 82 9C E1 42 3B D2 0E 32 1D 4C 42 8B D2 54 F9 B5 87 D0 94 5B 30 F0 A9 76 0C 18 9E 55 B5 5A 3B 6E CE 2B AA 63 39 A1 6B 22 5E BF 03 5F 51 C5 08 90 74 AE 45 B5 3B 1A 86 CF 0F 69 49 A4 D1 C1 F4 2E 99 E0 54 D7 1C 17 06 43 CA 1D CF E3 15 B9 7A FB EC 66 4C BE 6D 28 7F C2 DC 1E 04 69 C5 2A 81 E0 AD BE 79 FE 11 7E 17 80 FC 2C A3 ED FF 4C 3D 15 E0 94 66 19 D1 19 E6 98 49 A2 53 57 33 46 4C 3A 76 E0 CB 9A 73 2C 66 7F BF 36 58 7E F8 7C F5 EE A5 BB 7C FF 71 75 F9 75 F1 EA F5 F2 E7 F3 E5 C5 77 DF BE FE 33 F0 ED A6 0D 9A 64 9A 09 4A D3 F7 9B B1 41 40 D3 66 0E 7F 4F A4 DE 4D 26 4D 67 36 1D 23 C2 D6 C3 1C FC 02 21 C3 0A 7E".hexToBytes(),
//                                        serviceId = 35,
//                                    ),
//                                ),
//                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
//                                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
//                                        pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 CA 04 00 D2 05 02 08 6A".hexToBytes(),
//                                    ),
//                                ),
//                                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
//                                ),
//                            ),
//                        ),
//                    ),
//                ).toMessageChainOnline(bot, facade = this)
//            }
//        }
//    }

    @TestFactory
    fun `test serialization`(): DynamicTestsResult {
        val data = buildForwardMessage(defaultTarget.castUp()) {
            add(1, "senderName", time = 123, message = PlainText("simple text"))
            add(1, "senderName", time = 123) {
                +PlainText("simple")
                +Face(1)
                +Image("{90CCED1C-2D64-313B-5D66-46625CAB31D7}.jpg")
            }
        }
        val serialName = ForwardMessage.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}