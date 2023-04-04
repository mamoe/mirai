/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.source.OfflineMessageSourceImplData
import net.mamoe.mirai.internal.message.toMessageChainOnline
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.dynamicTest
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.internal.utils.runCoroutineInPlace
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.Test

internal class QuoteReplyProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(QuoteReplyProtocol(), TextProtocol())

    @Test
    fun `decode referencing online group message in group`() {
        buildCodingChecks {
            targetGroup()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(1803),
                        senderUin = 1230001,
                        time = 1653147259,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                        ),
                        pbReserve = "18 AB 85 9D 81 82 80 80 80 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
            )
            message(
                QuoteReply(
                    OfflineMessageSourceImplData(
                        ids = intArrayOf(1803),
                        internalIds = intArrayOf(539443883),
                        time = 1653147259,
                        originalMessage = messageChainOf(PlainText("a")),
                        kind = messageSourceKind,
                        fromId = 1230001,
                        targetId = 1,
                        botId = bot.id,
                    )
                ), PlainText("s")
            )
        }.doDecoderChecks()
    }

    @Test
    fun `encode referencing offline group message in group`() {
        buildCodingChecks {
            targetGroup()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(-31257),
                        senderUin = 1230001,
                        time = 1653326514,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                        ),
                        pbReserve = "18 AE FB A2 F7 86 80 80 80 01".hexToBytes(),
                        srcMsg = "0A 2C 08 B1 89 4B 10 DD F1 92 B7 07 18 09 20 0B 28 E7 8B FE FF FF FF FF FF FF 01 30 B2 85 AF 94 06 38 AE FB A2 F7 86 80 80 80 01 E0 01 01 1A 0D 0A 0B 12 05 0A 03 0A 01 61 12 02 4A 00".hexToBytes(),
                        toUin = 1994701021,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "@",
                        attr6Buf = "00 01 00 00 00 01 00 00 12 C4 B1 00 00".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
            )
            message(
                QuoteReply(
                    OfflineMessageSourceImplData(
                        ids = intArrayOf(-31257),
                        internalIds = intArrayOf(1860746670),
                        time = 1653326514,
                        originalMessage = messageChainOf(PlainText("a")),
                        kind = messageSourceKind,
                        fromId = 1230001,
                        targetId = 1994701021,
                        botId = bot.id,
                    )
                ), PlainText("s")
            )
        }.doEncoderChecks()
    }


    private val onlineIncomingGroupMessage = runCoroutineInPlace {
        net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
            msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                fromUin = 1230001,
                toUin = 1230002,
                msgType = 166,
                c2cCmd = 11,
                msgSeq = 31245,
                msgTime = 1653330864,
                msgUid = 72057594652150074,
                wseqInC2cMsghead = 31245,
            ),
            msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                    attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                        codePage = 0,
                        time = 1653330864,
                        random = 614222138,
                        size = 9,
                        effect = 0,
                        charSet = 134,
                        pitchAndFamily = 0,
                        fontName = "Helvetica",
                    ),
                    elems = mutableListOf(
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                str = "a",
                            ),
                        ),
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                        ),
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 61".hexToBytes(),
                            ),
                        ),
                    ),
                ),
            ),
        ).toMessageChainOnline(bot)
    }

    @Test
    fun `encode referencing online incoming group message in group`() {
        buildCodingChecks {
            targetGroup()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(31245),
                        senderUin = 1230001,
                        time = 1653330864,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            ),
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 61".hexToBytes(),
                                ),
                            ),
                        ),
                        pbReserve = "18 BA 92 F1 A4 82 80 80 80 01".hexToBytes(),
                        srcMsg = "0A 24 08 B1 89 4B 10 B2 89 4B 18 A6 01 20 0B 28 8D F4 01 30 B0 A7 AF 94 06 38 BA 92 F1 A4 82 80 80 80 01 E0 01 01 1A 2D 0A 2B 12 05 0A 03 0A 01 61 12 00 12 1C AA 02 19 9A 01 16 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 61 12 02 4A 00".hexToBytes(),
                        toUin = 1230002,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                )
            )

            message(onlineIncomingGroupMessage.quote(), PlainText("s"))
        }.doEncoderChecks()
    }

    // stranger and group temp are almost the same for friend.
    @Test
    fun `decode referencing online incoming private message in friend`() {
        buildCodingChecks {
            targetFriend()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(34279),
                        senderUin = 1230001,
                        time = 1653326514,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                        ),
                        pbReserve = "18 AE FB A2 F7 86 80 80 80 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                        pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 51".hexToBytes(),
                    ),
                ),
            )
            message(
                QuoteReply(
                    OfflineMessageSourceImplData(
                        ids = intArrayOf(34279),
                        internalIds = intArrayOf(1860746670),
                        time = 1653326514,
                        originalMessage = messageChainOf(PlainText("a")),
                        kind = messageSourceKind,
                        fromId = 1230001,
                        targetId = 0, // the referenced message was actually sending from friend 1230001 to bot.
                        botId = bot.id,
                    )
                ), PlainText("s")
            )
        }.doDecoderChecks()
    }

    @Test
    fun `decode referencing online outgoing private message in friend`() {
        buildCodingChecks {
            targetFriend()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(49858),
                        senderUin = 1230002,
                        time = 1653329998,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "b",
                                ),
                            ),
                        ),
                        pbReserve = "18 C3 94 C4 B3 84 80 80 80 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                        pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 5E".hexToBytes(),
                    ),
                ),
            )
            message(
                QuoteReply(
                    OfflineMessageSourceImplData(
                        ids = intArrayOf(49858),
                        internalIds = intArrayOf(1181813315),
                        time = 1653329998,
                        originalMessage = messageChainOf(PlainText("b")),
                        kind = messageSourceKind,
                        fromId = 1230002, // bot id
                        targetId = 0, // the referenced message was actually sending from bot to the friend 1230001.
                        botId = bot.id,
                    )
                ), PlainText("s")
            )
        }.doDecoderChecks()
    }

    @Test
    fun `encode referencing offline private message in friend`() {
        buildCodingChecks {
            targetFriend()

            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(-31257),
                        senderUin = 1230001,
                        time = 1653326514,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                        ),
                        pbReserve = "18 AE FB A2 F7 86 80 80 80 01".hexToBytes(),
                        srcMsg = "0A 2C 08 B1 89 4B 10 DD F1 92 B7 07 18 09 20 0B 28 E7 8B FE FF FF FF FF FF FF 01 30 B2 85 AF 94 06 38 AE FB A2 F7 86 80 80 80 01 E0 01 01 1A 0D 0A 0B 12 05 0A 03 0A 01 61 12 02 4A 00".hexToBytes(),
                        toUin = 1994701021,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                ),
            )
            message(
                QuoteReply(
                    OfflineMessageSourceImplData(
                        ids = intArrayOf(-31257),
                        internalIds = intArrayOf(1860746670),
                        time = 1653326514,
                        originalMessage = messageChainOf(PlainText("a")),
                        kind = messageSourceKind,
                        fromId = 1230001,
                        targetId = 1994701021,
                        botId = bot.id,
                    )
                ), PlainText("s")
            )
        }.doEncoderChecks()
    }

    init {
        bot.addFriend(1230001)
    }

    private val onlineIncomingFriendMessage: MessageChain = runCoroutineInPlace {
        net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.Msg(
            msgHead = net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm.MsgHead(
                fromUin = 1230001,
                toUin = 1230002,
                msgType = 166,
                c2cCmd = 11,
                msgSeq = 31222,
                msgTime = 1653328003,
                msgUid = 72057595832827069,
                wseqInC2cMsghead = 31222,
            ),
            msgBody = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MsgBody(
                richText = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichText(
                    attr = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Attr(
                        codePage = 0,
                        time = 1653328002,
                        random = 1794899133,
                        size = 9,
                        effect = 0,
                        charSet = 134,
                        pitchAndFamily = 0,
                        fontName = "Helvetica",
                    ),
                    elems = mutableListOf(
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                str = "a",
                            ),
                        ),
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                        ),
                        net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 4F".hexToBytes(),
                            ),
                        ),
                    ),
                ),
            ),
        ).toMessageChainOnline(bot)
    }


    @Test
    fun `encode referencing online incoming private message in friend`() {
        buildCodingChecks {
            targetFriend()
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    srcMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.SourceMsg(
                        origSeqs = intArrayOf(31222),
                        senderUin = 1230001,
                        time = 1653328003,
                        flag = 1,
                        elems = mutableListOf(
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                                    str = "a",
                                ),
                            ),
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                            ), // Don't worry about this empty Elem, it's  from official
                            net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                                generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                                    pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 4F".hexToBytes(),
                                ),
                            ),
                        ),
                        pbReserve = "18 BD F9 EF D7 86 80 80 80 01".hexToBytes(),
                        srcMsg = "0A 24 08 B1 89 4B 10 B2 89 4B 18 A6 01 20 0B 28 F6 F3 01 30 83 91 AF 94 06 38 BD F9 EF D7 86 80 80 80 01 E0 01 01 1A 2D 0A 2B 12 05 0A 03 0A 01 61 12 00 12 1C AA 02 19 9A 01 16 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 CA 04 00 D2 05 02 08 4F 12 02 4A 00".hexToBytes(),
                        toUin = 1230002,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "s",
                    ),
                )
            )

            message(onlineIncomingFriendMessage.quote(), PlainText("s"))
        }.doEncoderChecks()
    }

    private fun CodingChecksBuilder.targetGroup() {
        target(bot.addGroup(1, 1))
    }

    private fun CodingChecksBuilder.targetFriend() {
        target(bot.getFriendOrFail(1230001))
    }


    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    @TestFactory
    fun `test serialization for QuoteReply`(): DynamicTestsResult {
        val source = MessageSourceBuilder()
            .sender(123)
            .target(123)
            .messages {
                append("test")
            }
            .build(123, MessageSourceKind.FRIEND)

        val data = QuoteReply(source)

        val serialName = QuoteReply.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageMetadata(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }


    ///////////////////////////////////////////////////////////////////////////
    // MessageSource serialization
    ///////////////////////////////////////////////////////////////////////////


    @Serializable
    data class PolymorphicWrapperMessageSource(
        override val message: @Polymorphic MessageSource
    ) : PolymorphicWrapper

    @Serializable
    data class StaticWrapperMessageSource(
        override val message: MessageSource
    ) : PolymorphicWrapper

    private fun <M : MessageSource> testPolymorphicInMessageSource(
        data: M,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInMessageSource") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperMessageSource.serializer(),
            polyConstructor = ::PolymorphicWrapperMessageSource,
            data = data,
            expectedInstance = expectedInstance,
            expectedSerialName = null,
        )
    })

    private fun <M : MessageSource> testStaticInMessageSource(
        data: M,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testStaticInMessageSource") {
        testPolymorphicIn(
            polySerializer = StaticWrapperMessageSource.serializer(),
            polyConstructor = ::StaticWrapperMessageSource,
            data = data,
            expectedInstance = expectedInstance,
            expectedSerialName = null,
        )
    })

    @TestFactory
    fun `test serialization for OfflineMessageSource`(): DynamicTestsResult {
        val data = MessageSourceBuilder()
            .sender(123)
            .target(123)
            .messages {
                append("test")
            }
            .build(123, MessageSourceKind.FRIEND)

        val serialName = MessageSource.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageSource(data),
            testPolymorphicInMessageMetadata(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
            testStaticInMessageSource(data),
        )
    }

    @TestFactory
    fun `test serialization for OnlineMessageSource`(): DynamicTestsResult {
        val data = onlineIncomingGroupMessage[MessageSource]!!
        val expected = (data as OnlineMessageSource).toOffline()

        val serialName = MessageSource.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageSource(data, expectedInstance = expected),
            testPolymorphicInMessageMetadata(data, serialName, expectedInstance = expected),
            testPolymorphicInSingleMessage(data, serialName, expectedInstance = expected),
            testInsideMessageChain(data, serialName, expectedInstance = expected),
            testContextual(data, serialName, expectedInstance = expected),
            testStaticInMessageSource(data, expectedInstance = expected),
        )
    }
}