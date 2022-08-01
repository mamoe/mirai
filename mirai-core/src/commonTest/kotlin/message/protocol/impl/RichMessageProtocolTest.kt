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
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.dynamicTest
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.RichMessage
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RichMessageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(TextProtocol(), RichMessageProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun `decode from Android`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    richMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichMsg(
                        template1 = "01 78 9C 7D 52 41 6F D3 30 14 FE 2B 96 A5 D1 13 71 92 36 59 03 71 2A 95 AA A8 4C 30 8D 82 44 85 50 E5 BA 9E EB 29 76 A2 C4 D9 28 C7 46 42 1C 99 10 12 37 0E 08 21 18 9C 38 00 13 FC 9A 14 10 FF 02 27 5D 27 4E 48 D6 F3 7B F6 F7 FC FC 7D EF 85 BD C7 32 06 C7 2C CB 45 A2 70 CB B1 EC 16 60 8A 26 73 A1 38 6E DD BF 37 BC DA 6D 81 5C 13 35 27 71 A2 18 6E 2D 59 DE 02 BD 28 94 39 07 39 CB 8E 05 65 A3 01 86 0E 04 9A C9 34 26 7A 13 BA 6D 08 08 D5 F5 A3 10 82 59 26 D8 21 86 0F 7F 3E 7B BA 3E 3F 7B 54 AD BE 56 AB 77 55 F9 A1 2A BF 57 E5 CB 6A F5 A6 2A CB 6A F5 A5 2A DF 42 90 27 45 46 D9 ED 9C 8F E6 18 DA 10 14 59 8C E1 42 EB 34 BF 86 D0 D2 92 45 2E A8 E5 F8 6D 8B 26 12 49 94 27 8A F7 C4 1C 7B BE EB 05 E6 D8 BF 42 64 7A BD A0 1A 1F EC D9 A3 7D BA E3 0E C7 37 46 FB 5D DE BF 73 73 C7 ED 9F D0 59 CE 77 DA 03 B3 1A 20 49 D3 E9 96 7D D7 DA B5 3A 3E 04 87 31 E1 4D 69 32 37 57 63 C1 55 13 C9 22 D6 C2 FC 6B 78 71 1D 85 C2 50 06 31 59 26 85 C6 D0 BD C0 6B 61 64 99 8A CD E7 C9 66 8F C2 54 50 5D 64 0C D0 C4 40 36 7C 0C 9D D4 D9 F2 71 7D 4B 31 8D F6 C8 64 3C 0B 92 C9 C1 93 45 EC 3E E8 B3 5B F4 A8 7B 97 63 8C 1C 3B 08 3C C7 F1 3D C7 F5 7C DB D9 B5 5D EB 28 E5 10 9C 34 65 16 8D 45 51 A8 85 8E 59 F4 1F 7D 43 B4 81 84 79 21 25 C9 96 D1 A4 50 89 14 A8 2A 3F 35 E0 F7 B5 5D 7D 6C FC B3 10 6D 51 21 AA B9 9A AC A6 39 40 11 C9 30 FC FD E3 F4 D7 AB 17 EB F3 D3 3F AF 3F AF BF 3D 87 40 D0 BA DD DB 5E 09 8B 6B 21 B9 45 15 4A 52 A6 50 2D 75 8D 40 B6 8D 3A 01 F2 6C D4 F5 0C 2F BB 13 78 76 D7 9B 1A 6F 2A AD 54 F1 7F 07 87 4C 37 FE 80 68 82 A1 36 93 C9 94 BE CC 31 45 0C 24 4D 6B 91 2F 0F 1B 1D 90 19 CF E8 2F D6 1B 08 55".hexToBytes(),
                        serviceId = 1,
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "https://y.music.163.com/m/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                        pbReserve = "78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 CA 04 00 D2 05 02 08 41".hexToBytes(),
                    ),
                )
            )
            message(
                net.mamoe.mirai.message.data.SimpleServiceMessage(
                    serviceId = 1,
                    content = """
                    <?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID="1" templateID="123" action="" brief="[分享]ジェリーフィッシュ" sourceMsgId="0" url="https://y.music.163.com/m/song?id=562591636&amp;uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&amp;app_version=8.7.46" flag="0" adverSign="0" multiMsgFlag="0"><item layout="2" advertiser_id="0" aid="0"><picture cover="http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg" w="0" h="0" /><title>ジェリーフィッシュ</title><summary>Yunomi/ローラーガール</summary></item><source name="网易云音乐" icon="https://i.gtimg.cn/open/app_icon/00/49/50/85/100495085_100_m.png" action="" a_actionData="tencent100495085://" appid="100495085" /></msg>
                """.trimIndent()
                )
            )
        }.doDecoderChecks()
    }

    @Test
    fun encode() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    richMsg = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.RichMsg(
                        template1 = "01 78 9C 7D 52 41 6F D3 30 14 FE 2B 96 A5 D1 13 71 92 36 59 03 71 2A 95 AA A8 4C 30 8D 82 44 85 50 E5 BA 9E EB 29 76 A2 C4 D9 28 C7 46 42 1C 99 10 12 37 0E 08 21 18 9C 38 00 13 FC 9A 14 10 FF 02 27 5D 27 4E 48 D6 F3 7B F6 F7 FC FC 7D EF 85 BD C7 32 06 C7 2C CB 45 A2 70 CB B1 EC 16 60 8A 26 73 A1 38 6E DD BF 37 BC DA 6D 81 5C 13 35 27 71 A2 18 6E 2D 59 DE 02 BD 28 94 39 07 39 CB 8E 05 65 A3 01 86 0E 04 9A C9 34 26 7A 13 BA 6D 08 08 D5 F5 A3 10 82 59 26 D8 21 86 0F 7F 3E 7B BA 3E 3F 7B 54 AD BE 56 AB 77 55 F9 A1 2A BF 57 E5 CB 6A F5 A6 2A CB 6A F5 A5 2A DF 42 90 27 45 46 D9 ED 9C 8F E6 18 DA 10 14 59 8C E1 42 EB 34 BF 86 D0 D2 92 45 2E A8 E5 F8 6D 8B 26 12 49 94 27 8A F7 C4 1C 7B BE EB 05 E6 D8 BF 42 64 7A BD A0 1A 1F EC D9 A3 7D BA E3 0E C7 37 46 FB 5D DE BF 73 73 C7 ED 9F D0 59 CE 77 DA 03 B3 1A 20 49 D3 E9 96 7D D7 DA B5 3A 3E 04 87 31 E1 4D 69 32 37 57 63 C1 55 13 C9 22 D6 C2 FC 6B 78 71 1D 85 C2 50 06 31 59 26 85 C6 D0 BD C0 6B 61 64 99 8A CD E7 C9 66 8F C2 54 50 5D 64 0C D0 C4 40 36 7C 0C 9D D4 D9 F2 71 7D 4B 31 8D F6 C8 64 3C 0B 92 C9 C1 93 45 EC 3E E8 B3 5B F4 A8 7B 97 63 8C 1C 3B 08 3C C7 F1 3D C7 F5 7C DB D9 B5 5D EB 28 E5 10 9C 34 65 16 8D 45 51 A8 85 8E 59 F4 1F 7D 43 B4 81 84 79 21 25 C9 96 D1 A4 50 89 14 A8 2A 3F 35 E0 F7 B5 5D 7D 6C FC B3 10 6D 51 21 AA B9 9A AC A6 39 40 11 C9 30 FC FD E3 F4 D7 AB 17 EB F3 D3 3F AF 3F AF BF 3D 87 40 D0 BA DD DB 5E 09 8B 6B 21 B9 45 15 4A 52 A6 50 2D 75 8D 40 B6 8D 3A 01 F2 6C D4 F5 0C 2F BB 13 78 76 D7 9B 1A 6F 2A AD 54 F1 7F 07 87 4C 37 FE 80 68 82 A1 36 93 C9 94 BE CC 31 45 0C 24 4D 6B 91 2F 0F 1B 1D 90 19 CF E8 2F D6 1B 08 55".hexToBytes(),
                        serviceId = 1,
                    ),
                ),
            )
            message(
                net.mamoe.mirai.message.data.SimpleServiceMessage(
                    serviceId = 1,
                    content = """
                    <?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID="1" templateID="123" action="" brief="[分享]ジェリーフィッシュ" sourceMsgId="0" url="https://y.music.163.com/m/song?id=562591636&amp;uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&amp;app_version=8.7.46" flag="0" adverSign="0" multiMsgFlag="0"><item layout="2" advertiser_id="0" aid="0"><picture cover="http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg" w="0" h="0" /><title>ジェリーフィッシュ</title><summary>Yunomi/ローラーガール</summary></item><source name="网易云音乐" icon="https://i.gtimg.cn/open/app_icon/00/49/50/85/100495085_100_m.png" action="" a_actionData="tencent100495085://" appid="100495085" /></msg>
                """.trimIndent()
                )
            )
        }.doEncoderChecks()
    }

    // no encoder. specially handled, no test for now.


    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////


    @Serializable
    data class PolymorphicWrapperRichMessage(
        override val message: @Polymorphic RichMessage
    ) : PolymorphicWrapper

    private fun <M : RichMessage> testPolymorphicInRichMessage(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInRichMessage") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperRichMessage.serializer(),
            polyConstructor = ::PolymorphicWrapperRichMessage,
            data = data,
            expectedSerialName = expectedSerialName,
            expectedInstance = expectedInstance
        )
    })

    @TestFactory
    fun `test serialization for RichMessage`(): DynamicTestsResult {
        val data = net.mamoe.mirai.message.data.SimpleServiceMessage(
            serviceId = 1,
            content = """
                    <?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID="1" templateID="123" action="" brief="[分享]ジェリーフィッシュ" sourceMsgId="0" url="https://y.music.163.com/m/song?id=562591636&amp;uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&amp;app_version=8.7.46" flag="0" adverSign="0" multiMsgFlag="0"><item layout="2" advertiser_id="0" aid="0"><picture cover="http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg" w="0" h="0" /><title>ジェリーフィッシュ</title><summary>Yunomi/ローラーガール</summary></item><source name="网易云音乐" icon="https://i.gtimg.cn/open/app_icon/00/49/50/85/100495085_100_m.png" action="" a_actionData="tencent100495085://" appid="100495085" /></msg>
                """.trimIndent()
        )

        val serialName = net.mamoe.mirai.message.data.SimpleServiceMessage.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInRichMessage(data, serialName),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }

    @TestFactory
    fun `test serialization for LightApp`(): DynamicTestsResult {
        val data = net.mamoe.mirai.message.data.LightApp(
            content = """
                    <?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID="1" templateID="123" action="" brief="[分享]ジェリーフィッシュ" sourceMsgId="0" url="https://y.music.163.com/m/song?id=562591636&amp;uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&amp;app_version=8.7.46" flag="0" adverSign="0" multiMsgFlag="0"><item layout="2" advertiser_id="0" aid="0"><picture cover="http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg" w="0" h="0" /><title>ジェリーフィッシュ</title><summary>Yunomi/ローラーガール</summary></item><source name="网易云音乐" icon="https://i.gtimg.cn/open/app_icon/00/49/50/85/100495085_100_m.png" action="" a_actionData="tencent100495085://" appid="100495085" /></msg>
                """.trimIndent()
        )

        val serialName = net.mamoe.mirai.message.data.LightApp.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInRichMessage(data, serialName),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}