/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.message.MessageSourceFromSendFriend
import net.mamoe.mirai.qqandroid.message.MessageSourceFromSendGroup
import net.mamoe.mirai.qqandroid.message.toRichTextElems
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MsgTransmit
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.MultiMsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiPlatformUtils
import net.mamoe.mirai.utils._miraiContentToString

internal class MessageValidationData @OptIn(MiraiInternalAPI::class) constructor(
    val data: ByteArray,
    val md5: ByteArray = MiraiPlatformUtils.md5(data),
) {
    override fun toString(): String {
        return "MessageValidationData(data=<size=${data.size}>, md5=${md5.contentToString()})"
    }
}

@OptIn(MiraiInternalAPI::class)
internal fun MessageChain.calculateValidationData(
    bot: Bot
): MessageValidationData {
    // top_package.akkv#method_42702
    val source: MessageSource by this.orElse { error("internal error: calculateValidationData: cannot find MessageSource, chain=${this._miraiContentToString()}") }

    check(source is MessageSourceFromSendGroup || source is MessageSourceFromSendFriend) {
        "internal error: calculateValidationData: MessageSource must be "
    }

    val richTextElems = this.toRichTextElems(source is MessageSourceFromSendGroup)

    val msgTransmit = MsgTransmit.PbMultiMsgTransmit(
        msg = listOf(
            MsgComm.Msg(
                msgHead = MsgComm.MsgHead(
                    fromUin = source.senderId,
                    msgSeq = source.sequenceId,
                    msgTime = source.time.toInt(),
                    msgUid = source.messageRandom.toLong(), // TODO: 2020/3/26 CHECK IT
                    mutiltransHead = MsgComm.MutilTransHead(
                        status = 0,
                        msgId = 1
                    ),
                    msgType = 82, // troop
                    groupInfo = MsgComm.GroupInfo(
                        groupCode = source.toUin,
                        groupCard = bot.nick,
                    ),
                ),
                msgBody = ImMsgBody.MsgBody(
                    richText = ImMsgBody.RichText(
                        elems = richTextElems
                    )
                )
            )
        )
    )

    val bytes = msgTransmit.toByteArray(MsgTransmit.PbMultiMsgTransmit.serializer())

    return MessageValidationData(MiraiPlatformUtils.zip(bytes))
}

/*

=======================处理客户端到服务器=======================
flag1=0x0000000B(11), flag2=1, sequenceId = 00 00 E0 90, flag3=00, // 解密 bodyouter by D2 key
Packet 20:02:51 : ByteReadPacket outer body decrypted=00 00 00 28 00 00 00 18 4D 65 73 73 61 67 65 53 76 63 2E 50 62 53 65 6E 64 4D 73 67 00 00 00 08 8A 51 B1 25 00 00 00 04 00 00 02 7A 0A 08 12 06 08 F6 DD 96 FC 03 12 07 08 01 10 00 18 F2 46 1A D5 04 0A D2 04 12 A9 03 62 A6 03 0A A1 03 01 78 9C 7D 91 4B 4F DB 40 10 C7 BF CA 6A 2F 3E 81 1F 21 0D 91 6C 23 F1 48 15 CA A3 28 04 89 5C AA C5 1E 9B 15 6B 3B F5 AE 97 38 27 E8 05 04 5C B9 21 71 A1 95 B8 D0 1E 2A 24 54 F5 CB 20 25 F0 31 18 1B AA DE 90 46 AB 9D 9D D9 F9 CF 6F C6 5D 18 25 82 68 C8 25 CF 52 CF B0 67 2D 83 40 1A 64 21 4F 63 CF E8 6F 77 66 E6 0D 22 15 4B 43 26 B2 14 3C A3 04 69 90 05 DF 4D 64 4C 24 E4 9A 07 D0 5D F6 68 A3 49 89 82 64 28 98 AA 7D 9B 12 16 A8 AA 26 D5 1C 0E D7 0B A1 F8 BA 8C 29 D9 CB 39 44 18 77 1A 68 8E 8D 16 49 A9 A5 0E AB 83 E9 10 4D 6B 4A 92 2F 39 48 1E 7A 74 73 BB BD B1 16 7C B4 07 65 B9 37 E2 6D 4B 1F 9A 81 B5 3B B6 16 57 F9 4A F0 69 D5 71 3E 2C 2D F6 0F 76 3A 5D 25 C7 73 5B 03 D9 1F 8D 8B AF 83 9E 39 6A 44 9F 75 6F 4D 55 A5 22 2E 60 83 25 80 B2 CD F9 A6 DD 68 B5 5A 4E DB B6 28 91 59 91 07 80 7D 75 51 09 FD 22 17 1E A5 24 12 2C 46 24 44 08 71 32 3D 1E A7 75 34 79 83 E8 D4 61 9B FA 2E 47 64 22 58 99 15 EA F5 41 71 25 C0 7F 1F EE F1 E8 87 6B BE 26 BA FB 39 D9 E7 61 08 28 10 31 21 01 5B 52 A5 80 5A CE F4 5D 59 24 09 CB 4B FF E9 DB C3 E4 E4 CF F4 FA FB D3 D5 F9 E4 EE 62 7A F9 7B 7A 7F 3A 3D FE E9 9A FF 32 5C B3 EA 05 7F D4 44 24 AD 69 9F 8F CF 26 37 B7 CF 77 BF 26 7F 2F 29 E1 41 B5 8D FF 7B C1 DB 70 58 8D 78 C6 AE C5 4C 5C A9 FF 02 0F 69 BA A1 10 23 12 4D 0A 4B 0A 49 E4 BD A0 E7 9A 84 51 51 E6 9A 82 E4 B8 8D E6 94 AF E6 8C 81 E6 9F A5 E7 9C 8B 5B E8 BD AC E5 8F 91 E5 A4 9A E6 9D A1 E6 B6 88 E6 81 AF 5D EF BC 8C E8 AF B7 E6 9C 9F E5 BE 85 E5 90 8E E7 BB AD E7 89 88 E6 9C AC E3 80 82 12 55 AA 02 52 30 01 3A 40 4F 54 39 4E 4C 63 47 31 5A 79 79 62 78 69 39 30 76 77 2F 63 30 59 7A 30 42 4A 69 45 63 4B 4A 32 32 36 43 42 55 6B 56 46 49 74 73 7A 34 51 5A 73 55 78 7A 75 71 5A 53 2F 78 33 66 50 76 53 4C 74 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 92 3F 28 E9 9B 97 86 04 40 00
// 尝试解 Uni
// head
Packet Debug 20:02:51 : head=00 00 00 18 4D 65 73 73 61 67 65 53 76 63 2E 50 62 53 65 6E 64 4D 73 67 00 00 00 08 8A 51 B1 25 00 00 00 04
Packet 20:02:51 : commandName=MessageSvc.PbSendMsg
MessageSvc.PbSendMsg
  unknown4Bytes=8A 51 B1 25
  extraData=
Packet Debug 20:02:51 : Real body=0A 08 12 06 08 F6 DD 96 FC 03 12 07 08 01 10 00 18 F2 46 1A D5 04 0A D2 04 12 A9 03 62 A6 03 0A A1 03 01 78 9C 7D 91 4B 4F DB 40 10 C7 BF CA 6A 2F 3E 81 1F 21 0D 91 6C 23 F1 48 15 CA A3 28 04 89 5C AA C5 1E 9B 15 6B 3B F5 AE 97 38 27 E8 05 04 5C B9 21 71 A1 95 B8 D0 1E 2A 24 54 F5 CB 20 25 F0 31 18 1B AA DE 90 46 AB 9D 9D D9 F9 CF 6F C6 5D 18 25 82 68 C8 25 CF 52 CF B0 67 2D 83 40 1A 64 21 4F 63 CF E8 6F 77 66 E6 0D 22 15 4B 43 26 B2 14 3C A3 04 69 90 05 DF 4D 64 4C 24 E4 9A 07 D0 5D F6 68 A3 49 89 82 64 28 98 AA 7D 9B 12 16 A8 AA 26 D5 1C 0E D7 0B A1 F8 BA 8C 29 D9 CB 39 44 18 77 1A 68 8E 8D 16 49 A9 A5 0E AB 83 E9 10 4D 6B 4A 92 2F 39 48 1E 7A 74 73 BB BD B1 16 7C B4 07 65 B9 37 E2 6D 4B 1F 9A 81 B5 3B B6 16 57 F9 4A F0 69 D5 71 3E 2C 2D F6 0F 76 3A 5D 25 C7 73 5B 03 D9 1F 8D 8B AF 83 9E 39 6A 44 9F 75 6F 4D 55 A5 22 2E 60 83 25 80 B2 CD F9 A6 DD 68 B5 5A 4E DB B6 28 91 59 91 07 80 7D 75 51 09 FD 22 17 1E A5 24 12 2C 46 24 44 08 71 32 3D 1E A7 75 34 79 83 E8 D4 61 9B FA 2E 47 64 22 58 99 15 EA F5 41 71 25 C0 7F 1F EE F1 E8 87 6B BE 26 BA FB 39 D9 E7 61 08 28 10 31 21 01 5B 52 A5 80 5A CE F4 5D 59 24 09 CB 4B FF E9 DB C3 E4 E4 CF F4 FA FB D3 D5 F9 E4 EE 62 7A F9 7B 7A 7F 3A 3D FE E9 9A FF 32 5C B3 EA 05 7F D4 44 24 AD 69 9F 8F CF 26 37 B7 CF 77 BF 26 7F 2F 29 E1 41 B5 8D FF 7B C1 DB 70 58 8D 78 C6 AE C5 4C 5C A9 FF 02 0F 69 BA A1 10 23 12 4D 0A 4B 0A 49 E4 BD A0 E7 9A 84 51 51 E6 9A 82 E4 B8 8D E6 94 AF E6 8C 81 E6 9F A5 E7 9C 8B 5B E8 BD AC E5 8F 91 E5 A4 9A E6 9D A1 E6 B6 88 E6 81 AF 5D EF BC 8C E8 AF B7 E6 9C 9F E5 BE 85 E5 90 8E E7 BB AD E7 89 88 E6 9C AC E3 80 82 12 55 AA 02 52 30 01 3A 40 4F 54 39 4E 4C 63 47 31 5A 79 79 62 78 69 39 30 76 77 2F 63 30 59 7A 30 42 4A 69 45 63 4B 4A 32 32 36 43 42 55 6B 56 46 49 74 73 7A 34 51 5A 73 55 78 7A 75 71 5A 53 2F 78 33 66 50 76 53 4C 74 88 01 00 9A 01 08 78 00 F8 01 00 C8 02 00 20 92 3F 28 E9 9B 97 86 04 40 00
Packet 20:02:51 : ByteReadPacket uni packet=
Packet 20:02:51 : =======================共有 1 个包=======================

=======================处理服务器到客户端客户端=======================

=======================处理服务器到客户端客户端=======================
Packet 20:02:51 : ByteReadPacket 正在处理=00 00 00 0B 01 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 0B 1C 26 01 C3 F8 46 01 ED 8D 1E C8 86 C1 62 89 9C F4 16 57 67 99 4F 39 E7 69 4F 74 33 6E 92 89 74 49 09 84 19 10 6F 3C 81 DA 0C 92 DD 04 B7 60 C9 DB C8 4F F8 60 57 A2 3F CF 95 5F 01 F8 0A 79 E7 28 B9 6A F6 AD 0A 71 BA 54 F8 8C DF AF CF D3
Packet 20:02:51 : flag1(0A/0B) = 0B
Packet 20:02:51 : 包类型(flag2) = 1. (可能是 uni)
Packet 20:02:51 : 成功使用 d2Key 解密
Packet 20:02:51 : ByteReadPacket sso/uni body==00 00 00 34 00 00 E0 90 00 00 00 00 00 00 00 04 00 00 00 18 4D 65 73 73 61 67 65 53 76 63 2E 50 62 53 65 6E 64 4D 73 67 00 00 00 08 8A 51 B1 25 00 00 00 00 00 00 00 0C 08 00 18 EA 90 ED F3 05
Packet 20:02:51 : sequenceId = 57488
Packet 20:02:51 : sso(inner)extraData =
Packet Debug 20:02:51 : commandName=MessageSvc.PbSendMsg
Packet 20:02:51 : 不是oicq response(可能是 UNI/PB)= 00 00 00 0C 08 00 18 EA 90 ED F3 05
Packet 20:02:51 : =======================共有 0 个包=======================

 */

/*

=======================处理客户端到服务器=======================
flag1=0x0000000B(11), flag2=1, sequenceId = 00 00 E0 8D, flag3=00, // 解密 bodyouter by D2 key
Packet 20:02:50 : ByteReadPacket outer body decrypted=00 00 00 24 00 00 00 14 4D 75 6C 74 69 4D 73 67 2E 41 70 70 6C 79 55 70 00 00 00 08 8A 51 B1 25 00 00 00 04 00 00 00 3B 08 01 10 05 18 09 20 03 2A 0A 38 2E 32 2E 30 2E 31 32 39 36 32 1F 08 F6 DD 96 FC 03 10 CF 05 1A 10 BB 45 B9 71 2C F4 D3 06 5D A7 A7 A2 FF D4 62 D2 20 03 28 00 40 01
// 尝试解 Uni
// head
Packet Debug 20:02:50 : head=00 00 00 14 4D 75 6C 74 69 4D 73 67 2E 41 70 70 6C 79 55 70 00 00 00 08 8A 51 B1 25 00 00 00 04
Packet 20:02:50 : commandName=MultiMsg.ApplyUp
MultiMsg.ApplyUp
  unknown4Bytes=8A 51 B1 25
  extraData=
Packet Debug 20:02:50 : Real body=08 01 10 05 18 09 20 03 2A 0A 38 2E 32 2E 30 2E 31 32 39 36 32 1F 08 F6 DD 96 FC 03 10 CF 05 1A 10 BB 45 B9 71 2C F4 D3 06 5D A7 A7 A2 FF D4 62 D2 20 03 28 00 40 01
Packet 20:02:50 : ByteReadPacket uni packet=
Packet 20:02:50 : =======================共有 1 个包=======================

=======================处理服务器到客户端客户端=======================
Packet 20:02:50 : ByteReadPacket 正在处理=00 00 00 0B 01 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 8A B2 8A B1 DA C9 60 28 D8 55 AB 39 B9 07 A6 D8 BA F2 55 87 C2 C9 29 08 53 CC AF 99 3F 22 26 1F 66 01 09 60 F2 2A 3C F1 A4 DC 74 5A 27 1C 47 E2 F0 7E 57 0C 9B 50 7D 0D 52 A3 17 BB B7 8D 9B 62 3A B3 E2 65 6D 7C 74 24 79 11 A5 23 78 83 63 35 8C C9 34 4A D9 CD 61 4D 0D 73 74 DF 49 F3 AD 65 2D 1A 87 14 2F 03 5F 0B 16 1F 87 CE 2A 53 3E 9F 8F CF 0F B8 C3 6B E1 6C 42 46 0D 59 F2 89 7E 8A 47 A8 CC 52 C0 E7 5C E4 CD 00 A0 00 61 FA AF 95 C1 C4 1B 8C C3 24 48 A5 4D 4F D7 59 38 F1 AE 4A 3B 18 7E 52 96 D5 2D 5D 67 D0 B8 0C BC F0 FD 3E 45 2C 7F 2E 1B AC FF F1 86 04 9B 8E 16 DF 7F C0 1C 25 13 36 21 D8 87 B1 FA BA 6E D2 DA E3 02 D2 31 45 9D 61 D4 43 07 F6 B5 D3 B0 6D 72 8B 83 FA B5 90 A7 BA 7A 32 2C 28 96 67 AC AB 42 37 EF 51 5B A1 A8 2D 17 93 F9 2C 22 51 6C 49 0A ED 38 AF 88 A1 E4 C7 09 BC DA 11 3F 46 DF D3 60 51 0E 92 89 56 D6 0D B4 66 DC 74 77 64 42 95 56 BE 89 61 75 CB F7 8C 33 D4 6B 40 4F 07 43 5B D9 A4 38 E1 DC 2A 0D 4D D6 8D 2B F5 E4 A2 45 3D EF 77 E5 24 F5 09 5E 1C 9C 14 CA 33 4D 3D 63 83 2E 38 94 13 1D 7A 0D 62 DB 89 0D 27 8D E2 58 5D 24 25 BC 9F D3 E3 3A 55 F2 FB 93 69 61 F0 25 E6 7F 7F B6 25 87 33 5B 5F 35 C1 E0 C4 6E 25 41 A0 12 B5 E6 DA 1A C9 F4 20 31 86 D3 B2 C9 D3 2D 96 40 92 BC BD 38 AD D6 94 E9 25 14 12 2D B6 32 6E D5 37 7D C6 E3 A8 E5 1E AD 97 52 FA DD CC 7E 96 5A E0 CB AF 79 4B CB BC E3 9F 57 4C 94 C7 9D 58 83 D0 11 41 BD E6 9C E1 98 7B BB 5B
Packet 20:02:50 : flag1(0A/0B) = 0B
Packet 20:02:50 : 包类型(flag2) = 1. (可能是 uni)
Packet 20:02:50 : 成功使用 d2Key 解密
Packet 20:02:50 : ByteReadPacket sso/uni body==00 00 00 30 00 00 E0 8D 00 00 00 00 00 00 00 04 00 00 00 14 4D 75 6C 74 69 4D 73 67 2E 41 70 70 6C 79 55 70 00 00 00 08 8A 51 B1 25 00 00 00 00 00 00 01 88 08 01 12 FF 02 08 00 12 40 4F 54 39 4E 4C 63 47 31 5A 79 79 62 78 69 39 30 76 77 2F 63 30 59 7A 30 42 4A 69 45 63 4B 4A 32 32 36 43 42 55 6B 56 46 49 74 73 7A 34 51 5A 73 55 78 7A 75 71 5A 53 2F 78 33 66 50 76 53 4C 74 1A 98 01 1B 76 62 FB B2 C6 24 C3 1F 39 47 0D 45 5C 77 BD 0C 8F 69 FB C8 4F D8 76 83 26 60 EA A3 24 BC FD F6 C8 B4 64 DA 47 9D 6C 1A FA F4 EF 02 FC A4 76 1F 87 EB FF 51 62 20 E9 1F 74 6B 2F 7B 7C 53 EC 6D A2 53 AC 2B 93 B4 79 83 6D E6 D8 86 E1 D5 E2 4D EE 75 03 A3 3B 72 EB 0A 3E 13 3A 80 70 EF CC B4 0D F9 42 E3 DF 5F 7A 4C 36 BC 3B 9C 31 5A B1 40 B4 5B 49 26 CE 65 BD 2F 86 8D 9D 0C 34 1B 5E 32 6E EF 60 4B E1 60 7F 1A 98 CF 14 42 85 A6 F8 BE A5 EE A7 A6 C7 9E 11 20 FB AE FA 95 0A 20 B7 87 A4 8F 0E 20 FB AE FA 9D 0A 20 E5 B6 95 B0 0A 28 50 28 90 3F 28 BB 03 28 50 40 00 4A 10 4E 64 43 67 6D 61 71 35 6D 52 73 43 53 38 41 58 52 68 AF 63 72 0B 4D 5B 17 6E D8 35 C1 D3 3F C8 D7 FC F0 A8 0A 67 4D B5 A6 B3 B7 E2 E1 9F 96 68 D3 BC AD 4A 6A 20 72 E8 D2 44 C3 8B 93 60 F3 3C 4B 46 83 E4 75 A2 3C 72 A4 F7 31 D9 88 89 23 34 9A AF EF FC 17 29 5D 6C D0 2B F1 63 D5 9F E2 B9 B5 49 D2 62 E3 D0 F9 19 C5 0D 20 AF 78 D5 34 7E BB B7 E2 8E 5C 69 F4 38 38 E7
Packet 20:02:50 : sequenceId = 57485
Packet 20:02:50 : sso(inner)extraData =
Packet Debug 20:02:50 : commandName=MultiMsg.ApplyUp
找不到包 PacketFactory
Packet 20:02:50 : 传递给 PacketFactory 的数据 = 00 00 01 88 08 01 12 FF 02 08 00 12 40 4F 54 39 4E 4C 63 47 31 5A 79 79 62 78 69 39 30 76 77 2F 63 30 59 7A 30 42 4A 69 45 63 4B 4A 32 32 36 43 42 55 6B 56 46 49 74 73 7A 34 51 5A 73 55 78 7A 75 71 5A 53 2F 78 33 66 50 76 53 4C 74 1A 98 01 1B 76 62 FB B2 C6 24 C3 1F 39 47 0D 45 5C 77 BD 0C 8F 69 FB C8 4F D8 76 83 26 60 EA A3 24 BC FD F6 C8 B4 64 DA 47 9D 6C 1A FA F4 EF 02 FC A4 76 1F 87 EB FF 51 62 20 E9 1F 74 6B 2F 7B 7C 53 EC 6D A2 53 AC 2B 93 B4 79 83 6D E6 D8 86 E1 D5 E2 4D EE 75 03 A3 3B 72 EB 0A 3E 13 3A 80 70 EF CC B4 0D F9 42 E3 DF 5F 7A 4C 36 BC 3B 9C 31 5A B1 40 B4 5B 49 26 CE 65 BD 2F 86 8D 9D 0C 34 1B 5E 32 6E EF 60 4B E1 60 7F 1A 98 CF 14 42 85 A6 F8 BE A5 EE A7 A6 C7 9E 11 20 FB AE FA 95 0A 20 B7 87 A4 8F 0E 20 FB AE FA 9D 0A 20 E5 B6 95 B0 0A 28 50 28 90 3F 28 BB 03 28 50 40 00 4A 10 4E 64 43 67 6D 61 71 35 6D 52 73 43 53 38 41 58 52 68 AF 63 72 0B 4D 5B 17 6E D8 35 C1 D3 3F C8 D7 FC F0 A8 0A 67 4D B5 A6 B3 B7 E2 E1 9F 96 68 D3 BC AD 4A 6A 20 72 E8 D2 44 C3 8B 93 60 F3 3C 4B 46 83 E4 75 A2 3C 72 A4 F7 31 D9 88 89 23 34 9A AF EF FC 17 29 5D 6C D0 2B F1 63 D5 9F E2 B9 B5 49 D2 62 E3 D0 F9 19 C5 0D 20 AF 78 D5 34 7E BB B7 E2 8E 5C 69 F4 38 38 E7
Packet 20:02:50 : 不是oicq response(可能是 UNI/PB)=
Packet 20:02:50 : =======================共有 0 个包=======================
 */

internal class MultiMsg {

    object ApplyUp : OutgoingPacketFactory<ApplyUp.Response>("MultiMsg.ApplyUp") {
        class Response(
            val proto: MultiMsg.MultiMsgApplyUpRsp
        ) : Packet

        fun createForLongMessage(
            client: QQAndroidClient,
            message: MessageChain,
            dstUin: Long, // group uin
        ): OutgoingPacket = createForLongMessage(client, message.calculateValidationData(client.bot), dstUin)

        // captured from group
        private fun createForLongMessage(
            client: QQAndroidClient,
            messageData: MessageValidationData,
            dstUin: Long // group uin
        ): OutgoingPacket = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                MultiMsg.ReqBody.serializer(),
                MultiMsg.ReqBody(
                    subcmd = 1,
                    termType = 5,
                    platformType = 9,
                    netType = 3, // wifi=3, wap=5
                    buildVer = client.buildVer,
                    buType = 1,
                    multimsgApplyupReq = listOf(
                        MultiMsg.MultiMsgApplyUpReq(
                            applyId = 0,
                            dstUin = dstUin,
                            msgMd5 = messageData.md5,
                            msgSize = messageData.data.size.toLong(),
                            msgType = 3 // TODO 3 for group?
                        )
                    )
                )
            )
        }

        /*
        RspBody#195600860 {
            multimsgApplyupRsp=[MultiMsgApplyUpRsp#314337396 {
                    applyId=0x00000000(0)
                    blockSize=0x0000000000000000(0)
                    msgKey=4E 64 43 67 6D 61 71 35 6D 52 73 43 53 38 41 58
                    msgResid=4F 54 39 4E 4C 63 47 31 5A 79 79 62 78 69 39 30 76 77 2F 63 30 59 7A 30 42 4A 69 45 63 4B 4A 32 32 36 43 42 55 6B 56 46 49 74 73 7A 34 51 5A 73 55 78 7A 75 71 5A 53 2F 78 33 66 50 76 53 4C 74
                    msgSig=AF 63 72 0B 4D 5B 17 6E D8 35 C1 D3 3F C8 D7 FC F0 A8 0A 67 4D B5 A6 B3 B7 E2 E1 9F 96 68 D3 BC AD 4A 6A 20 72 E8 D2 44 C3 8B 93 60 F3 3C 4B 46 83 E4 75 A2 3C 72 A4 F7 31 D9 88 89 23 34 9A AF EF FC 17 29 5D 6C D0 2B F1 63 D5 9F E2 B9 B5 49 D2 62 E3 D0 F9 19 C5 0D 20 AF 78 D5 34 7E BB B7 E2 8E 5C 69 F4 38 38 E7
                    msgUkey=1B 76 62 FB B2 C6 24 C3 1F 39 47 0D 45 5C 77 BD 0C 8F 69 FB C8 4F D8 76 83 26 60 EA A3 24 BC FD F6 C8 B4 64 DA 47 9D 6C 1A FA F4 EF 02 FC A4 76 1F 87 EB FF 51 62 20 E9 1F 74 6B 2F 7B 7C 53 EC 6D A2 53 AC 2B 93 B4 79 83 6D E6 D8 86 E1 D5 E2 4D EE 75 03 A3 3B 72 EB 0A 3E 13 3A 80 70 EF CC B4 0D F9 42 E3 DF 5F 7A 4C 36 BC 3B 9C 31 5A B1 40 B4 5B 49 26 CE 65 BD 2F 86 8D 9D 0C 34 1B 5E 32 6E EF 60 4B E1 60 7F 1A 98 CF 14 42 85 A6 F8 BE A5 EE A7 A6 C7 9E 11
                    result=0x00000000(0)
                    uint32UpIp=[0xA2BE977B(-1564567685), 0xE1E903B7(-504822857), 0xA3BE977B(-1547790469), 0xA6055B65(-1509598363)]
                    uint32UpPort=[0x00000050(80), 0x00001F90(8080), 0x000001BB(443), 0x00000050(80)]
                    upOffset=0x0000000000000000(0)
            }]
            subcmd=0x00000001(1)
    }
         */
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val response = readProtoBuf(MultiMsg.MultiMsgApplyUpRsp.serializer())
            check(response.result == 0) {
                kotlin.run {
                    println(response._miraiContentToString())
                }.let { "Protocol error: MultiMsg.ApplyUp failed with result ${response.result}" }
            }
            return Response(response)
        }
    }
}