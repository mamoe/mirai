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
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.AbstractContact
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.message.protocol.outgoing.MessageProtocolStrategy
import net.mamoe.mirai.internal.message.protocol.outgoing.OutgoingMessageProcessorAdapter
import net.mamoe.mirai.internal.pipeline.replaceProcessor
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.MessageOrigin
import net.mamoe.mirai.message.data.MessageOriginKind
import net.mamoe.mirai.message.data.MusicKind.NeteaseCloudMusic
import net.mamoe.mirai.message.data.MusicShare
import net.mamoe.mirai.utils.castUp
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class MusicShareProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> =
        arrayOf(TextProtocol(), MusicShareProtocol(), RichMessageProtocol(), GeneralMessageSenderProtocol())

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
                    lightApp = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.LightAppElem(
                        data = "01 78 9C 7D 53 5D 6B D4 40 14 FD 2F 81 EE 53 99 CC E4 63 76 53 08 42 15 A5 16 2D B5 08 16 23 61 3A 99 4D D3 6E 66 86 64 D2 A5 96 7D D9 80 F8 68 11 C1 37 1F 44 44 AB 4F 3E A8 45 7F 4D AA E2 BF F0 4E 58 B7 50 44 32 24 F7 DC B9 33 E7 7E 9C 9C 38 4C 6B 67 CD E1 AA 44 46 48 2E A4 41 B5 A9 1A 6E CA 3A 77 56 9D 4C D4 1C B6 7F BF FA 74 F1 F5 19 E0 A3 42 4C 01 97 4D 5D 70 0B 45 05 08 23 FB 10 C0 BA 52 A5 36 E0 7A F8 E3 E9 93 8B F3 B3 47 DD FC 4B 37 7F DB B5 EF BB F6 5B D7 BE E8 E6 AF BB B6 ED E6 9F BB F6 0D 84 97 C2 30 67 ED 64 71 1D 18 8C 9B 42 49 38 0F 9B 4C 66 95 2A B2 54 1F E6 A9 64 A5 58 78 B5 4E CD B1 06 44 7A 50 64 60 61 1C 44 21 1E 85 AB 0E 9C B7 91 84 86 5E 44 A9 EF 7B CB 12 76 1B A9 CA 22 71 BB F6 63 9F CB 3B FB 9E 7F E8 ED 33 B8 F8 A0 29 F5 FD 6A 02 91 FB C6 E8 7A 2D 71 13 F7 18 F5 99 21 42 7D 04 1D 4A 5C 58 B5 92 F9 B5 22 8B 43 EA 85 11 6C D0 01 34 2B DE DE C4 1B 5B 7C C5 BB B9 73 7D 63 6B 94 AF DF BD B5 E2 AD 4F F9 5E 9D AF F8 37 60 0D 6C DE D0 AD 1A CA 8B 47 68 88 02 6A EB B7 B7 5F 92 F6 9C 57 18 2D 1D 38 45 56 B0 C4 55 8D 11 55 E2 36 D5 E4 4A 06 B5 A8 C0 E1 7B 01 1E 52 1F 0F 07 35 8F A7 E5 D1 C0 C8 B8 1F 8A 58 8C 6D 49 A2 C9 DF CA 3C 8A A4 30 89 BB C9 76 77 F6 22 B5 BB FD 78 7F E2 3D 58 17 B7 F9 C1 E8 5E 1E C7 89 4B 70 14 85 04 3A 4A BC 90 62 32 C4 1E 3A D0 56 1B B5 6A 2A 2E EE D4 F9 46 66 45 B0 F4 A4 05 EF 67 78 D9 C7 02 E5 30 97 1C 71 09 35 68 01 6F DB 0E 1B 96 B8 18 27 6E 10 25 6E 08 DF 51 68 E9 16 C3 4C C1 4A 4B A4 E5 25 57 DA F4 BD 02 6C 58 0E C6 AF EF A7 3F 5F 3E BF 38 3F 5D EA D3 14 66 62 95 F2 7F DD 35 05 E4 47 3C 3F 08 E9 6C 06 A2 51 72 5C E4 56 7F FF 90 CF 58 55 53 56 41 85 F0 57 08 20 50 87 C2 16 47 30 E1 41 38 1E 31 3F DB C3 8C 92 30 8B 08 1B B2 60 8C 23 1A 8D B9 6F 73 E9 65 EA 48 55 95 6C E2 CC 66 7F 00 45 5B 33 67".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = """
                                |[分享]ジェリーフィッシュ
                                |Yunomi / ローラーガール
                                |https://y.music.163.com/m/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46
                                |来自: 网易云音乐 """.trimMargin(),
                    ),
                ),
            )
            message(
                MessageOrigin(
                    LightApp(
                        """
                            {"app":"com.tencent.structmsg","desc":"音乐","view":"music","ver":"0.0.0.1","prompt":"[分享]ジェリーフィッシュ","meta":{"music":{"action":"","android_pkg_name":"","app_type":1,"appid":100495085,"ctime":1652966332,"desc":"Yunomi\/ローラーガール","jumpUrl":"https:\/\/y.music.163.com\/m\/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46","musicUrl":"http:\/\/music.163.com\/song\/media\/outer\/url?id=562591636&userid=324076307&sc=wmv&tn=","preview":"http:\/\/p1.music.126.net\/KaYSb9oYQzhl2XBeJcj8Rg==\/109951165125601702.jpg","sourceMsgId":"0","source_icon":"https:\/\/i.gtimg.cn\/open\/app_icon\/00\/49\/50\/85\/100495085_100_m.png","source_url":"","tag":"网易云音乐","title":"ジェリーフィッシュ","uin":123456}},"config":{"ctime":1652966332,"forward":true,"token":"101c45f8a3db0a615d91a7a4f0969fc3","type":"normal"}}
                        """.trimIndent()
                    ),
                    null,
                    MessageOriginKind.MUSIC_SHARE
                ),
                MusicShare(
                    kind = NeteaseCloudMusic,
                    title = "ジェリーフィッシュ",
                    summary = "Yunomi/ローラーガール",
                    jumpUrl = "https://y.music.163.com/m/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46",
                    pictureUrl = "http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg",
                    musicUrl = "http://music.163.com/song/media/outer/url?id=562591636&userid=324076307&sc=wmv&tn=",
                    brief = "[分享]ジェリーフィッシュ",
                )
            )
        }.doDecoderChecks()
    }

    @Test
    fun `can send MusicShare to group`() {
        val message = MusicShare(
            kind = NeteaseCloudMusic,
            title = "ジェリーフィッシュ",
            summary = "Yunomi/ローラーガール",
            jumpUrl = "https://y.music.163.com/m/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46",
            pictureUrl = "http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg",
            musicUrl = "http://music.163.com/song/media/outer/url?id=562591636&userid=324076307&sc=wmv&tn=",
            brief = "[分享]ジェリーフィッシュ",
        )

        runWithFacade {
            assertTrue {
                outgoingPipeline.replaceProcessor(
                    { it is MusicShareProtocol.Sender },
                    OutgoingMessageProcessorAdapter(object : MusicShareProtocol.Sender() {
                        override suspend fun sendMusicSharePacket(
                            bot: QQAndroidBot,
                            musicShare: MusicShare,
                            contact: AbstractContact,
                            strategy: MessageProtocolStrategy<*>
                        ) {
                            // nop
                        }
                    })
                )
            }
            preprocessAndSendOutgoingImpl(defaultTarget.castUp(), message, components).let { (context, receipts) ->
                val receipt = receipts.single()
                assertMessageEquals(message, receipt.source.originalMessage)
                assertMessageEquals(message, context.currentMessageChain)
            }
        }
    }

    @TestFactory
    fun `test serialization for MusicShare`(): DynamicTestsResult {
        val data = MusicShare(
            kind = NeteaseCloudMusic,
            title = "ジェリーフィッシュ",
            summary = "Yunomi/ローラーガール",
            jumpUrl = "https://y.music.163.com/m/song?id=562591636&uct=QK0IOc%2FSCIO8gBNG%2Bwcbsg%3D%3D&app_version=8.7.46",
            pictureUrl = "http://p1.music.126.net/KaYSb9oYQzhl2XBeJcj8Rg==/109951165125601702.jpg",
            musicUrl = "http://music.163.com/song/media/outer/url?id=562591636&userid=324076307&sc=wmv&tn=",
            brief = "[分享]ジェリーフィッシュ",
        )

        val serialName = MusicShare.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }


}