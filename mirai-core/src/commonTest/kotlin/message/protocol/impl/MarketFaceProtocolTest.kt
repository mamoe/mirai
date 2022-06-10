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
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.utils.hexToBytes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MarketFaceProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(MarketFaceProtocol(), TextProtocol())

    @BeforeEach
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    @Test
    fun `decode Dice`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E9 9A 8F E6 9C BA E9 AA B0 E5 AD 90 5D".hexToBytes(),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "48 23 D3 AD B1 5D F0 80 14 CE 5D 67 96 B7 6E E1".hexToBytes(),
                        tabId = 11464,
                        subType = 3,
                        key = "409e2a69b16918f9".toByteArray(), /* 34 30 39 65 32 61 36 39 62 31 36 39 31 38 66 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 34".hexToBytes(),
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[随机骰子]",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    generalFlags = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.GeneralFlags(
                        pbReserve = "78 00 90 01 01 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 CA 04 00 D2 05 02 08 37".hexToBytes(),
                    ),
                )
            )
            message(Dice(5))
            useOrdinaryEquality()
        }.doDecoderChecks()
    }


    @Test
    fun `encode Dice`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E9 AA B0 E5 AD 90 5D".hexToBytes(), // [骰子]
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "48 23 D3 AD B1 5D F0 80 14 CE 5D 67 96 B7 6E E1".hexToBytes(),
                        tabId = 11464,
                        subType = 3,
                        key = "409e2a69b16918f9".toByteArray(), /* 34 30 39 65 32 61 36 39 62 31 36 39 31 38 66 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 34".hexToBytes(),
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01 58 00 62 09 23 30 30 30 30 30 30 30 30 6A 09 23 30 30 30 30 30 30 30 30".hexToBytes(),
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[骰子]",
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                )
            )
            message(Dice(5))
        }.doEncoderChecks()
    }


    @Test
    fun `encode decode MarketFace from Android`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E5 8F 91 E5 91 86 5D".hexToBytes(),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "71 26 44 B5 27 94 46 11 99 8A EC 31 86 75 19 D2".hexToBytes(),
                        tabId = 10278,
                        subType = 3,
                        key = "726a53a5372b7289".toByteArray(), /* 37 32 36 61 35 33 61 35 33 37 32 62 37 32 38 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 10 64 1A 0B 51 51 E5 A4 A7 E9 BB 84 E8 84 B8 22 40 68 74 74 70 73 3A 2F 2F 7A 62 2E 76 69 70 2E 71 71 2E 63 6F 6D 2F 69 70 3F 5F 77 76 3D 31 36 37 37 38 32 34 31 26 66 72 6F 6D 3D 61 69 6F 45 6D 6F 6A 69 4E 65 77 26 69 64 3D 31 30 38 39 31 30 2A 06 E6 9D A5 E8 87 AA 30 B5 BB B4 E3 0D 38 B5 BB B4 E3 0D 40 01 50 00".hexToBytes(),
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[发呆]",
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                )
            )
            // MarketFaceImpl 不支持手动构造
            message(
                MarketFaceImpl(
                    net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E5 8F 91 E5 91 86 5D".hexToBytes(),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "71 26 44 B5 27 94 46 11 99 8A EC 31 86 75 19 D2".hexToBytes(),
                        tabId = 10278,
                        subType = 3,
                        key = "726a53a5372b7289".toByteArray(), /* 37 32 36 61 35 33 61 35 33 37 32 62 37 32 38 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 10 64 1A 0B 51 51 E5 A4 A7 E9 BB 84 E8 84 B8 22 40 68 74 74 70 73 3A 2F 2F 7A 62 2E 76 69 70 2E 71 71 2E 63 6F 6D 2F 69 70 3F 5F 77 76 3D 31 36 37 37 38 32 34 31 26 66 72 6F 6D 3D 61 69 6F 45 6D 6F 6A 69 4E 65 77 26 69 64 3D 31 30 38 39 31 30 2A 06 E6 9D A5 E8 87 AA 30 B5 BB B4 E3 0D 38 B5 BB B4 E3 0D 40 01 50 00".hexToBytes(),
                    )
                )
            )
        }.doBothChecks()
    }

    @Test
    fun `encode decode MarketFace from macOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E5 8F 91 E5 91 86 5D".hexToBytes(),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "71 26 44 B5 27 94 46 11 99 8A EC 31 86 75 19 D2".hexToBytes(),
                        tabId = 10278,
                        subType = 3,
                        key = "726a53a5372b7289".toByteArray(), /* 37 32 36 61 35 33 61 35 33 37 32 62 37 32 38 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[发呆]",
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                )
            )
            // MarketFaceImpl 不支持手动构造
            message(
                MarketFaceImpl(
                    net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "5B E5 8F 91 E5 91 86 5D".hexToBytes(),
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "71 26 44 B5 27 94 46 11 99 8A EC 31 86 75 19 D2".hexToBytes(),
                        tabId = 10278,
                        subType = 3,
                        key = "726a53a5372b7289".toByteArray(), /* 37 32 36 61 35 33 61 35 33 37 32 62 37 32 38 39 */
                        imageWidth = 200,
                        imageHeight = 200,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    )
                )
            )
        }.doBothChecks()
    }


}