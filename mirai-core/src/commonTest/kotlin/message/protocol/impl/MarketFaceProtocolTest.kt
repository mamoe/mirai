/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import io.ktor.utils.io.core.*
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.dynamicTest
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.Dice
import net.mamoe.mirai.message.data.MarketFace
import net.mamoe.mirai.message.data.RockPaperScissors
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class MarketFaceProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(MarketFaceProtocol(), TextProtocol())

    @BeforeTest
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
    fun `decode RockPaperScissors`() {
        // region WinQQ PC
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "E5 D8 89 F1 DF 79 B2 B4 51 83 F6 25 58 44 65 D3".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 100,
                        imageHeight = 100,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                        attr7Buf = "01".hexToBytes(),
                    ),
                ),
            )

            message(RockPaperScissors.ROCK)
            useOrdinaryEquality()
        }.doDecoderChecks()
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "62 8F A4 AB 7B 6C 2B CC FC DC D0 C2 DA F7 A6 0C".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 100,
                        imageHeight = 100,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                        attr7Buf = "01".hexToBytes(),
                    ),
                ),
            )

            message(RockPaperScissors.SCISSORS)
            useOrdinaryEquality()
        }.doDecoderChecks()
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "45 7C DE 42 0F 59 8E B4 24 CE D2 E9 05 D3 8D 8B".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 100,
                        imageHeight = 100,
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                        attr7Buf = "01".hexToBytes(),
                    ),
                ),
            )

            message(RockPaperScissors.PAPER)
            useOrdinaryEquality()
        }.doDecoderChecks()
        // endregion

        // region AndroidQQ 8.4.18.49145
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=2".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 32 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )
            message(RockPaperScissors.PAPER)
            useOrdinaryEquality()
        }.doDecoderChecks()
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=0".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 30 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )
            message(RockPaperScissors.ROCK)
            useOrdinaryEquality()
        }.doDecoderChecks()
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=1".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 31 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )
            message(RockPaperScissors.SCISSORS)
            useOrdinaryEquality()
        }.doDecoderChecks()
        // endregion

        // region MacOS
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=0".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 30 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )

            message(RockPaperScissors.ROCK)
            useOrdinaryEquality()
        }.doDecoderChecks()
        // endregion

        // region iOS
        buildCodingChecks {
            elem(
                // ROCK
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=0".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 30 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )
            message(RockPaperScissors.ROCK)
            useOrdinaryEquality()
        }.doDecoderChecks()
        buildCodingChecks { // paper
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=2".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 32 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
            )
            message(RockPaperScissors.PAPER)
            useOrdinaryEquality()
        }.doDecoderChecks()
        // endregion
    }

    @Test
    fun `encode RockPaperScissors`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=0".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 30 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                ),
            )
            message(RockPaperScissors.ROCK)
        }.doBothChecks()

        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=1".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 31 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ),
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                ),
            )
            message(RockPaperScissors.SCISSORS)
        }.doBothChecks()

        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    marketFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.MarketFace(
                        faceName = "[猜拳]".toByteArray(), /* 5B E7 8C 9C E6 8B B3 5D */
                        itemType = 6,
                        faceInfo = 1,
                        faceId = "83 C8 A2 93 AE 65 CA 14 0F 34 81 20 A7 74 48 EE".hexToBytes(),
                        tabId = 11415,
                        subType = 3,
                        key = "7de39febcf45e6db".toByteArray(), /* 37 64 65 33 39 66 65 62 63 66 34 35 65 36 64 62 */
                        imageWidth = 200,
                        imageHeight = 200,
                        mobileParam = "rscType?1;value=2".toByteArray(), /* 72 73 63 54 79 70 65 3F 31 3B 76 61 6C 75 65 3D 32 */
                        pbReserve = "0A 06 08 C8 01 10 C8 01 40 01".hexToBytes(),
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    text = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Text(
                        str = "[猜拳]",
                    ),
                ), net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    extraInfo = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.ExtraInfo(
                        flags = 8,
                        groupMask = 1,
                    ),
                )
            )
            message(RockPaperScissors.PAPER)
        }.doBothChecks()

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

    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    @Serializable
    data class PolymorphicWrapperMarketFace(
        override val message: @Polymorphic MarketFace
    ) : PolymorphicWrapper

    @Serializable
    data class StaticWrapperDice(
        override val message: Dice
    ) : PolymorphicWrapper

    @Serializable
    data class StaticWrapperRockPaperScissors(
        override val message: RockPaperScissors
    ) : PolymorphicWrapper

    private fun <M : MarketFace> testPolymorphicInMarketFace(
        data: M,
        expectedSerialName: String,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInMarketFace") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperMarketFace.serializer(),
            polyConstructor = ::PolymorphicWrapperMarketFace,
            data = data,
            expectedSerialName = expectedSerialName, // MarketFaceImpl is 'MarketFace', Dice is 'Dice', should include discriminator
            expectedInstance = expectedInstance,
        )
    })

    private fun testStaticDice(
        data: Dice,
        expectedInstance: Dice = data,
    ) = listOf(dynamicTest("testStaticDice") {
        testPolymorphicIn(
            polySerializer = StaticWrapperDice.serializer(),
            polyConstructor = ::StaticWrapperDice,
            data = data,
            expectedSerialName = null,
            expectedInstance = expectedInstance,
        )
    })

    private fun testStaticRockPaperScissors(
        data: RockPaperScissors,
        expectedInstance: RockPaperScissors = data,
    ) = listOf(dynamicTest("testStaticRockPaperScissors") {
        testPolymorphicIn(
            polySerializer = StaticWrapperRockPaperScissors.serializer(),
            polyConstructor = ::StaticWrapperRockPaperScissors,
            data = data,
            expectedSerialName = null,
            expectedInstance = expectedInstance,
        )
    })

    @TestFactory
    fun `test serialization for MarketFaceImpl`(): DynamicTestsResult {
        val data = MarketFaceImpl(
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
        val serialName = MarketFaceImpl.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMarketFace(data, serialName),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName, targetType = MarketFace::class),
        )
    }

    @TestFactory
    fun `test serialization for RockPaperScissors`(): DynamicTestsResult {
        val data = RockPaperScissors.PAPER

        val serialName = RockPaperScissors.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMarketFace(data, serialName),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
            testContextual(data, serialName, targetType = MarketFace::class),
            testStaticRockPaperScissors(data),
        )
    }

    @TestFactory
    fun `test serialization for Dice`(): DynamicTestsResult {
        val data = Dice(1)
        val serialName = Dice.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInMarketFace(data, serialName),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName, targetType = Dice::class),
            testStaticDice(data),
        )
    }
}