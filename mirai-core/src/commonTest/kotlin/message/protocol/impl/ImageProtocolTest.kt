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
import net.mamoe.mirai.internal.message.image.OfflineFriendImage
import net.mamoe.mirai.internal.message.image.OfflineGroupImage
import net.mamoe.mirai.internal.message.protocol.MessageProtocol
import net.mamoe.mirai.internal.testFramework.DynamicTestsResult
import net.mamoe.mirai.internal.testFramework.TestFactory
import net.mamoe.mirai.internal.testFramework.dynamicTest
import net.mamoe.mirai.internal.testFramework.runDynamicTests
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

internal class ImageProtocolTest : AbstractMessageProtocolTest() {
    override val protocols: Array<out MessageProtocol> = arrayOf(ImageProtocol())

    @BeforeTest
    fun `init group`() {
        defaultTarget = bot.addGroup(123, 1230003).apply {
            addMember(1230003, "user3", MemberPermission.OWNER)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // receive from macOS client
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image receive from macOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        fileId = -1866484636,
                        useful = 1,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        thumbUrl = "/gchatpic_new/123456/12345678-2428482660-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/gchatpic_new/123456/12345678-2428482660-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        width = 904,
                        height = 1214,
                        size = 170426,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/gchatpic_new/123456/12345678-2428482660-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = false
            })
            targetGroup()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    @Test
    fun `friend Image receive from macOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        fileLen = 170426,
                        downloadPath = "/123456-306012740-A7CBB52943A2127CE42659D29BAA8515",
                        oldVerSendFile = "16 20 31 32 32 31 30 31 31 31 31 41 42 20 20 20 20 31 37 30 34 32 36 6B 7B 41 37 43 42 42 35 32 39 2D 34 33 41 32 2D 31 32 37 43 2D 45 34 32 36 2D 35 39 44 32 39 42 41 41 38 35 31 35 7D 2E 6A 70 67 77 2F 31 30 34 30 34 30 30 32 39 30 2D 33 30 36 30 31 32 37 34 30 2D 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 41".hexToBytes(),
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123456-306012740-A7CBB52943A2127CE42659D29BAA8515",
                        thumbUrl = "/offpic_new/123456//123456-306012740-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/offpic_new/123456//123456-306012740-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123456//123456-306012740-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = false
            })
            targetFriend()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // receive from Android
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image receive from Android`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "A7CBB52943A2127CE42659D29BAA8515.jpg",
                        oldData = "15 36 20 38 36 65 41 31 42 61 66 34 35 64 37 38 63 36 66 31 65 39 30 33 66 20 20 20 20 20 20 35 30 57 4A 4B 53 53 71 52 79 61 52 46 42 7A 77 38 34 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 2E 6A 70 67 41".hexToBytes(),
                        fileId = -1354377332,
                        serverIp = 1864273983,
                        serverPort = 80,
                        fileType = 66,
                        signature = "WJKSSqRyaRFBzw84".toByteArray(), /* 57 4A 4B 53 53 71 52 79 61 52 46 42 7A 77 38 34 */
                        useful = 1,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        thumbUrl = "/gchatpic_new/123456/622457678-2940589964-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/gchatpic_new/123456/622457678-2940589964-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        imageType = 1000,
                        width = 904,
                        height = 1214,
                        source = 103,
                        size = 170426,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/gchatpic_new/123456/622457678-2940589964-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 01 10 00 32 00 4A 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 06".hexToBytes(),
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = true
            })
            targetGroup()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    @Test
    fun `friend Image receive from Android`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "A7CBB52943A2127CE42659D29BAA8515.jpg",
                        fileLen = 170426,
                        downloadPath = "/123456-113241016-A7CBB52943A2127CE42659D29BAA8515",
                        oldVerSendFile = "16 20 31 31 36 31 30 31 30 35 31 41 42 20 20 20 20 31 37 30 34 32 36 65 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 2E 6A 70 67 77 2F 31 30 34 30 34 30 30 32 39 30 2D 31 31 33 32 34 31 30 31 36 2D 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 41".hexToBytes(),
                        imgType = 1000,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123456-113241016-A7CBB52943A2127CE42659D29BAA8515",
                        thumbUrl = "/offpic_new/123456//123456-113241016-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/offpic_new/123456//123456-113241016-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        bizType = 5,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123456//123456-113241016-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 01 10 00 32 00 42 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 06".hexToBytes(),
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = true
            })
            targetFriend()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // receive from iOS
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image receive from iOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "C344D6240014DA35BB63A958BC435134.png",
                        fileLen = 108536,
                        downloadPath = "/123456-346835805-C344D6240014DA35BB63A958BC435134",
                        oldVerSendFile = "16 20 31 31 36 31 30 31 30 35 31 41 42 20 20 20 20 31 30 38 35 33 36 65 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 2E 70 6E 67 77 2F 32 36 35 32 33 38 36 32 32 38 2D 33 34 36 38 33 35 38 30 35 2D 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 41".hexToBytes(),
                        imgType = 1000,
                        picMd5 = "C3 44 D6 24 00 14 DA 35 BB 63 A9 58 BC 43 51 34".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123456-346835805-C344D6240014DA35BB63A958BC435134",
                        thumbUrl = "/offpic_new/123456//123456-346835805-C344D6240014DA35BB63A958BC435134/198?term=2",
                        origUrl = "/offpic_new/123456//123456-346835805-C344D6240014DA35BB63A958BC435134/0?term=2",
                        bizType = 4,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123456//123456-346835805-C344D6240014DA35BB63A958BC435134/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 00 10 00 18 00 50 00 78 04".hexToBytes(),
                    ),
                )
            )
            message(Image("{C344D624-0014-DA35-BB63-A958BC435134}.jpg") {
                width = 904
                height = 1214
                size = 108536
                type = ImageType.JPG
                isEmoji = false
            })
            targetGroup()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    @Test
    fun `friend Image receive from iOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "C344D6240014DA35BB63A958BC435134.png",
                        fileLen = 108536,
                        downloadPath = "/123455-346835805-C344D6240014DA35BB63A958BC435134",
                        oldVerSendFile = "16 20 31 31 36 31 30 31 30 35 31 41 42 20 20 20 20 31 30 38 35 33 36 65 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 2E 70 6E 67 77 2F 32 36 35 32 33 38 36 32 32 38 2D 33 34 36 38 33 35 38 30 35 2D 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 41".hexToBytes(),
                        imgType = 1000,
                        picMd5 = "C3 44 D6 24 00 14 DA 35 BB 63 A9 58 BC 43 51 34".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123455-346835805-C344D6240014DA35BB63A958BC435134",
                        thumbUrl = "/offpic_new/123455//123455-346835805-C344D6240014DA35BB63A958BC435134/198?term=2",
                        origUrl = "/offpic_new/123455//123455-346835805-C344D6240014DA35BB63A958BC435134/0?term=2",
                        bizType = 4,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123455//123455-346835805-C344D6240014DA35BB63A958BC435134/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 00 10 00 18 00 50 00 78 04".hexToBytes(),
                    ),
                )
            )
            message(Image("{C344D624-0014-DA35-BB63-A958BC435134}.jpg") {
                width = 904
                height = 1214
                size = 108536
                type = ImageType.JPG
                isEmoji = false
            })
            targetFriend()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // receive from Windows
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image receive from Windows`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "{C344D624-0014-DA35-BB63-A958BC435134}.jpg",
                        flag = "00 00 00 00".hexToBytes(),
                        oldData = "15 36 20 39 32 6B 41 31 43 39 32 65 39 64 35 30 64 34 39 38 64 33 33 37 39 20 20 20 20 20 20 35 30 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 7B 43 33 34 34 44 36 32 34 2D 30 30 31 34 2D 44 41 33 35 2D 42 42 36 33 2D 41 39 35 38 42 43 34 33 35 31 33 34 7D 2E 6A 70 67 41".hexToBytes(),
                        fileId = -1830169331,
                        serverIp = 1233990521,
                        serverPort = 80,
                        fileType = 67,
                        useful = 1,
                        picMd5 = "C3 44 D6 24 00 14 DA 35 BB 63 A9 58 BC 43 51 34".hexToBytes(),
                        thumbUrl = "/gchatpic_new/123456/123456-2464797965-C344D6240014DA35BB63A958BC435134/198?term=2",
                        origUrl = "/gchatpic_new/123456/123456-2464797965-C344D6240014DA35BB63A958BC435134/0?term=2",
                        imageType = 1000,
                        width = 904,
                        height = 1214,
                        size = 108536,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/gchatpic_new/123456/123456-2464797965-C344D6240014DA35BB63A958BC435134/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                    ),
                )
            )
            message(Image("{C344D624-0014-DA35-BB63-A958BC435134}.jpg") {
                width = 904
                height = 1214
                size = 108536
                type = ImageType.JPG
                isEmoji = false
            })
            targetGroup()
        }.doDecoderChecks()
    }

    @Test
    fun `friend Image receive from Windows`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "J9R_MJL9@9Z02}QZ0LTTX77.jpg",
                        fileLen = 108536,
                        downloadPath = "/123456-2313394132-C344D6240014DA35BB63A958BC435134",
                        oldVerSendFile = "16 20 31 31 37 31 30 31 30 36 31 43 42 20 20 20 20 31 30 38 35 33 36 65 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 2E 6A 70 67 78 2F 33 32 37 39 38 32 36 34 38 34 2D 32 33 31 33 33 39 34 31 33 32 2D 43 33 34 34 44 36 32 34 30 30 31 34 44 41 33 35 42 42 36 33 41 39 35 38 42 43 34 33 35 31 33 34 41".hexToBytes(),
                        imgType = 1000,
                        picMd5 = "C3 44 D6 24 00 14 DA 35 BB 63 A9 58 BC 43 51 34".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123456-2313394132-C344D6240014DA35BB63A958BC435134",
                        flag = "00 00 00 00".hexToBytes(),
                        thumbUrl = "/offpic_new/123456//123456-2313394132-C344D6240014DA35BB63A958BC435134/198?term=2",
                        origUrl = "/offpic_new/123456//123456-2313394132-C344D6240014DA35BB63A958BC435134/0?term=2",
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123456//123456-2313394132-C344D6240014DA35BB63A958BC435134/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                    ),
                )
            )
            message(Image("{C344D624-0014-DA35-BB63-A958BC435134}.jpg") {
                width = 904
                height = 1214
                size = 108536
                type = ImageType.JPG
                isEmoji = false
            })
            targetFriend()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // receive from iPadOS
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image receive from iPadOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        oldData = "15 36 20 39 32 6B 41 31 E8 38 63 34 65 39 63 38 39 20 20 20 20 20 20 20 30 20 20 20 20 20 20 20 30 71 76 4A 51 79 6D 7A 37 4D 77 7A 7A 33 6A 74 4E 7B 41 37 43 42 42 35 32 39 2D 34 33 41 32 2D 31 32 37 43 2D 45 34 32 36 2D 35 39 44 32 39 42 41 41 38 35 31 35 7D 2E 6A 70 67 41".hexToBytes(),
                        fileId = -1941005175,
                        fileType = -24,
                        signature = "qvJQymz7Mwzz3jtN".toByteArray(), /* 71 76 4A 51 79 6D 7A 37 4D 77 7A 7A 33 6A 74 4E */
                        useful = 1,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        thumbUrl = "/gchatpic_new/123456/123456-2353962121-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/gchatpic_new/123456/123456-2353962121-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        imageType = 1000,
                        width = 904,
                        height = 1214,
                        source = 203,
                        size = 170426,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/gchatpic_new/123456/123456-2353962121-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 01 10 00 18 00 2A 0C E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 4A 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 05".hexToBytes(),
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = true
            })
            targetGroup()
        }.doDecoderChecks()
    }

    @Test
    fun `friend Image receive from iPadOS`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "A7CBB52943A2127CE42659D29BAA8515.png",
                        fileLen = 170426,
                        downloadPath = "/1040400290-197707644-A7CBB52943A2127CE42659D29BAA8515",
                        oldVerSendFile = "16 20 31 31 36 31 30 31 30 35 31 41 42 20 20 20 20 31 37 30 34 32 36 65 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 2E 70 6E 67 77 2F 31 30 34 30 34 30 30 32 39 30 2D 31 39 37 37 30 37 36 34 34 2D 41 37 43 42 42 35 32 39 34 33 41 32 31 32 37 43 45 34 32 36 35 39 44 32 39 42 41 41 38 35 31 35 41".hexToBytes(),
                        imgType = 1000,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/123456-197707644-A7CBB52943A2127CE42659D29BAA8515",
                        thumbUrl = "/offpic_new/123456//123456-197707644-A7CBB52943A2127CE42659D29BAA8515/198?term=2",
                        origUrl = "/offpic_new/123456//123456-197707644-A7CBB52943A2127CE42659D29BAA8515/0?term=2",
                        bizType = 5,
                        thumbWidth = 147,
                        thumbHeight = 198,
                        _400Url = "/offpic_new/123456//123456-197707644-A7CBB52943A2127CE42659D29BAA8515/400?term=2",
                        _400Width = 285,
                        _400Height = 384,
                        pbReserve = "08 01 10 00 18 00 2A 0C E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 42 0E 5B E5 8A A8 E7 94 BB E8 A1 A8 E6 83 85 5D 50 00 78 05".hexToBytes(),
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = true
            })
            targetFriend()
            useOrdinaryEquality()
        }.doDecoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // send without dimension
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `group Image send without dimension`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        flag = byteArrayOf(0, 0, 0, 0),
                        fileType = 66,
                        useful = 1,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        bizType = 5,
                        imageType = 1000,
                        width = 1,
                        height = 1,
                        origin = 1,
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg"))
            targetGroup()
        }.doEncoderChecks()
    }

    @Test
    fun `friend Image send without dimension`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        downloadPath = "/000000000-000000000-A7CBB52943A2127CE42659D29BAA8515",
                        imgType = 1000,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        picHeight = 1,
                        picWidth = 1,
                        resId = "/000000000-000000000-A7CBB52943A2127CE42659D29BAA8515",
                        original = 1,
                        bizType = 5,
                        pbReserve = "x".toByteArray(), /* 78 02 */
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg"))
            targetFriend()
        }.doEncoderChecks()
    }

    ///////////////////////////////////////////////////////////////////////////
    // send with dimension
    ///////////////////////////////////////////////////////////////////////////


    @Test
    fun `group Image send with dimension`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    customFace = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.CustomFace(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        flag = byteArrayOf(0, 0, 0, 0),
                        fileType = 66,
                        useful = 1,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        bizType = 5,
                        imageType = 1000,
                        width = 904,
                        height = 1214,
                        size = 170426,
                        origin = 1,
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = false
            })
            targetGroup()
        }.doEncoderChecks()
    }

    private fun CodingChecksBuilder.targetGroup() {
        target(bot.addGroup(1, 1))
    }

    @Test
    fun `friend Image send with dimension`() {
        buildCodingChecks {
            elem(
                net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.Elem(
                    notOnlineImage = net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody.NotOnlineImage(
                        filePath = "{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg",
                        fileLen = 170426,
                        downloadPath = "/000000000-000000000-A7CBB52943A2127CE42659D29BAA8515",
                        imgType = 1000,
                        picMd5 = "A7 CB B5 29 43 A2 12 7C E4 26 59 D2 9B AA 85 15".hexToBytes(),
                        picHeight = 1214,
                        picWidth = 904,
                        resId = "/000000000-000000000-A7CBB52943A2127CE42659D29BAA8515",
                        original = 1,
                        bizType = 5,
                        pbReserve = "x".toByteArray(), /* 78 02 */
                    ),
                )
            )
            message(Image("{A7CBB529-43A2-127C-E426-59D29BAA8515}.jpg") {
                width = 904
                height = 1214
                size = 170426
                type = ImageType.JPG
                isEmoji = false
            })
            targetFriend()
        }.doEncoderChecks()
    }

    private fun CodingChecksBuilder.targetFriend() {
        target(bot.addFriend(1))
    }


    ///////////////////////////////////////////////////////////////////////////
    // serialization
    ///////////////////////////////////////////////////////////////////////////

    @Serializable
    data class PolymorphicWrapperImage(
        override val message: @Polymorphic Image
    ) : PolymorphicWrapper

    private fun <M : Image> testPolymorphicInImage(
        data: M,
        expectedInstance: M = data,
    ) = listOf(dynamicTest("testPolymorphicInImage") {
        testPolymorphicIn(
            polySerializer = PolymorphicWrapperImage.serializer(),
            polyConstructor = ::PolymorphicWrapperImage,
            data = data,
            expectedSerialName = null,
            expectedInstance = expectedInstance,
        )
    })

    @TestFactory
    fun `test serialization for OfflineGroupImage`(): DynamicTestsResult {
        val data = Image("{90CCED1C-2D64-313B-5D66-46625CAB31D7}.jpg")
        assertIs<OfflineGroupImage>(data)
        val serialName = Image.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInImage(data),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }

    @TestFactory
    fun `test serialization for OfflineFriendImage type 1`(): DynamicTestsResult {
        val data = Image("/f8f1ab55-bf8e-4236-b55e-955848d7069f") // type 1
        assertIs<OfflineFriendImage>(data)
        val serialName = Image.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInImage(data),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }

    @TestFactory
    fun `test serialization for OfflineFriendImage type 2`(): DynamicTestsResult {
        val data = Image("/000000000-3814297509-BFB7027B9354B8F899A062061D74E206") // type 1
        assertIs<OfflineFriendImage>(data)
        val serialName = Image.SERIAL_NAME
        return runDynamicTests(
            testPolymorphicInImage(data),
            testPolymorphicInMessageContent(data, serialName),
            testPolymorphicInSingleMessage(data, serialName),
            testInsideMessageChain(data, serialName),
            testContextual(data, serialName),
        )
    }
}