/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.PushResp
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.qqandroid.utils.toUHexString
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.PushReq as PushReqJceStruct


internal class ConfigPushSvc {
    object PushReq : IncomingPacketFactory<PushReqJceStruct?>(
        receivingCommandName = "ConfigPushSvc.PushReq",
        responseCommandName = "ConfigPushSvc.PushResp"
    ) {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): PushReqJceStruct? {
            val bytes = this.readBytes()
            return kotlin.runCatching {
                bytes.loadAs(PushReqJceStruct.serializer())
            }.getOrElse {
                println(bytes.toUHexString())
                // 10 02 2C 3C 4C 56 23 51 51 53 65 72 76 69 63 65 2E 43 6F 6E 66 69 67 50 75 73 68 53 76 63 2E 4D 61 69 6E 53 65 72 76 61 6E 74 66 07 50 75 73 68 52 65 71 7D 00 01 08 7E 08 00 01 06 07 50 75 73 68 52 65 71 18 00 01 06 12 43 6F 6E 66 69 67 50 75 73 68 2E 50 75 73 68 52 65 71 1D 00 01 08 56 0A 10 02 2D 00 01 08 44 09 00 01 0A 16 0C 31 38 33 2E 35 37 2E 35 33 2E 31 36 21 1F 90 0B 19 00 01 0A 16 0B 31 38 30 2E 39 36 2E 31 2E 33 30 20 50 0B 29 00 02 0A 16 0D 36 31 2E 31 38 33 2E 31 36 34 2E 32 37 20 50 0B 0A 16 0B 31 34 2E 31 37 2E 34 33 2E 34 38 20 50 0B 39 00 06 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 33 35 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 31 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 34 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 34 35 20 50 0B 0A 16 0E 31 31 39 2E 31 34 37 2E 31 39 2E 32 35 32 20 50 0B 0A 16 11 73 63 61 6E 6E 6F 6E 2E 33 67 2E 71 71 2E 63 6F 6D 20 50 0B 49 00 04 0A 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 37 30 21 01 BB 0B 0A 16 0C 35 39 2E 33 36 2E 38 39 2E 32 35 32 21 1F 90 0B 0A 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 20 50 0B 0A 16 0C 31 30 31 2E 39 31 2E 35 2E 31 38 37 21 01 BB 0B 5A 09 00 03 0A 00 01 19 00 04 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 37 30 21 01 BB 0B 0A 00 01 16 0C 35 39 2E 33 36 2E 38 39 2E 32 35 32 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 20 50 0B 0A 00 01 16 0C 31 30 31 2E 39 31 2E 35 2E 31 38 37 21 01 BB 0B 29 0C 3C 0B 0A 00 05 19 00 04 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 37 30 21 01 BB 0B 0A 00 01 16 0C 35 39 2E 33 36 2E 38 39 2E 32 35 32 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 20 50 0B 0A 00 01 16 0C 31 30 31 2E 39 31 2E 35 2E 31 38 37 21 01 BB 0B 29 0C 3C 0B 0A 00 0A 19 00 04 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 37 30 21 01 BB 0B 0A 00 01 16 0C 35 39 2E 33 36 2E 38 39 2E 32 35 32 21 1F 90 0B 0A 00 01 16 0E 31 32 33 2E 31 35 30 2E 37 36 2E 31 36 38 20 50 0B 0A 00 01 16 0C 31 30 31 2E 39 31 2E 35 2E 31 38 37 21 01 BB 0B 29 00 05 0A 0C 11 20 00 20 10 30 01 0B 0A 00 01 11 20 00 20 08 30 02 0B 0A 00 02 11 20 00 20 08 30 01 0B 0A 00 03 12 00 01 00 00 20 08 30 02 0B 0A 00 04 11 20 00 20 08 30 02 0B 3C 0B 1D 00 00 68 67 84 36 BC AB 01 05 B2 79 8B F8 C3 51 00 DA AF 1E EF D8 E6 01 AF 08 05 B5 8B A6 61 9B 1B 1C 5A 0B AC FB D8 4C FB 2D 47 3F D0 8D 56 2D 6C FF 9B 48 B0 1D BC 14 34 F8 64 36 F2 0D EA 8B 63 C5 CC 37 54 0A A0 81 27 7D B8 91 EB 88 DC 69 2B 5C 88 BD 7B D3 B6 31 33 46 E1 BA BE A3 88 52 17 8B E6 11 5F DA C0 D2 DA 31 BB 2D 00 00 10 78 56 76 4A 44 49 62 33 6B 76 53 52 61 62 74 52 32 76 E4 B8 DD 40 01 5D 00 01 02 4F 8A 50 CB 04 0A 68 67 84 36 BC AB 01 05 B2 79 8B F8 C3 51 00 DA AF 1E EF D8 E6 01 AF 08 05 B5 8B A6 61 9B 1B 1C 5A 0B AC FB D8 4C FB 2D 47 3F D0 8D 56 2D 6C FF 9B 48 B0 1D BC 14 34 F8 64 36 F2 0D EA 8B 63 C5 CC 37 54 0A A0 81 27 7D B8 91 EB 88 DC 69 2B 5C 88 BD 7B D3 B6 31 33 46 E1 BA BE A3 88 52 17 8B E6 11 5F DA C0 D2 DA 31 BB 12 10 78 56 76 4A 44 49 62 33 6B 76 53 52 61 62 74 52 1A 41 08 01 12 0E 08 01 15 7B 96 4C AA 18 BB 03 20 02 28 01 12 0E 08 01 15 3B 24 59 FC 18 90 3F 20 01 28 01 12 0D 08 01 15 7B 96 4C A8 18 50 20 02 28 00 12 0E 08 01 15 65 5B 05 BB 18 BB 03 20 04 28 00 1A 41 08 05 12 0E 08 01 15 7B 96 4C AA 18 BB 03 20 02 28 01 12 0E 08 01 15 3B 24 59 FC 18 90 3F 20 01 28 01 12 0D 08 01 15 7B 96 4C A8 18 50 20 02 28 00 12 0E 08 01 15 65 5B 05 BB 18 BB 03 20 04 28 00 1A 79 08 0A 12 0E 08 01 15 7B 96 4C AA 18 BB 03 20 02 28 01 12 0E 08 01 15 3B 24 59 FC 18 90 3F 20 01 28 01 12 0D 08 01 15 7B 96 4C A8 18 50 20 02 28 00 12 0E 08 01 15 65 5B 05 BB 18 BB 03 20 04 28 00 22 09 08 00 10 80 40 18 10 20 01 22 09 08 01 10 80 40 18 08 20 02 22 09 08 02 10 80 40 18 08 20 01 22 0A 08 03 10 80 80 04 18 08 20 02 22 09 08 04 10 80 40 18 08 20 02 20 01 32 04 08 00 10 01 3A 2A 08 10 10 10 18 09 20 09 28 0F 30 0F 38 05 40 05 48 5A 50 01 58 5A 60 5A 68 5A 70 5A 78 0A 80 01 0A 88 01 0A 90 01 0A 98 01 0A 42 0A 08 00 10 00 18 00 20 00 28 00 4A 06 08 01 10 01 18 03 52 42 08 01 12 0A 08 00 10 80 80 04 18 10 20 02 12 0A 08 01 10 80 80 04 18 08 20 02 12 0A 08 02 10 80 80 01 18 08 20 01 12 0A 08 03 10 80 80 04 18 08 20 02 12 0A 08 04 10 80 80 04 18 08 20 02 18 00 20 00 5A 40 08 01 12 0A 08 00 10 80 80 04 18 10 20 02 12 0A 08 01 10 80 80 04 18 08 20 02 12 0A 08 02 10 80 80 01 18 08 20 01 12 0A 08 03 10 80 80 04 18 08 20 02 12 0A 08 04 10 80 80 04 18 08 20 02 18 00 0B 69 00 01 0A 16 26 69 6D 67 63 61 63 68 65 2E 71 71 2E 63 6F 6D 2E 73 63 68 65 64 2E 70 31 76 36 2E 74 64 6E 73 76 36 2E 63 6F 6D 2E 20 50 0B 79 00 02 0A 16 0E 31 30 31 2E 32 32 37 2E 31 33 31 2E 36 37 20 50 0B 0A 16 0C 31 30 31 2E 38 39 2E 33 39 2E 32 31 20 50 0B 8A 06 0E 31 37 31 2E 31 31 32 2E 32 32 34 2E 31 30 10 03 0B 9A 09 00 0C 0A 00 0F 19 00 01 0A 12 71 19 A3 B4 20 50 0B 29 0C 0B 0A 00 04 19 00 01 0A 12 0B 27 59 65 20 50 0B 29 0C 0B 0A 00 0D 19 00 02 0A 12 5B A0 6A 72 20 50 0B 0A 12 71 EB 3F 3B 20 50 0B 29 0C 0B 0A 00 03 19 00 02 0A 12 5B A0 6A 72 20 50 0B 0A 12 71 EB 3F 3B 20 50 0B 29 0C 0B 0A 00 07 19 00 01 0A 12 75 A2 E3 65 20 50 0B 29 0C 0B 0A 00 09 19 00 02 0A 12 15 8C D7 0E 20 50 0B 0A 12 18 8C D7 0E 20 50 0B 29 0C 0B 0A 00 0A 19 00 02 0A 12 15 8C D7 0E 20 50 0B 0A 12 18 8C D7 0E 20 50 0B 29 0C 0B 0A 00 0B 19 00 02 0A 12 6D 01 B1 6F 20 50 0B 0A 12 4D 01 B1 6F 20 50 0B 29 0C 0B 0A 00 05 19 00 01 0A 12 1D E2 03 B7 20 50 0B 29 0C 0B 0A 00 08 19 00 02 0A 12 DF 3F 5B 65 20 50 0B 0A 12 DE 3F 5B 65 20 50 0B 29 0C 0B 0A 00 06 19 00 02 0A 12 2C B7 97 3D 20 50 0B 0A 12 1B 45 5B 65 20 50 0B 29 0C 0B 0A 00 0E 19 00 01 0A 12 76 01 B1 6F 20 50 0B 29 0C 0B 0B AD 00 01 01 5A 08 01 10 80 EE D3 1D 18 00 22 0A 31 39 39 34 37 30 31 30 32 31 28 AB E1 81 57 32 12 08 8E A4 D8 9D 0A 10 50 18 89 D8 AC A0 0E 20 50 28 64 32 12 08 8E A4 D8 B5 0A 10 50 18 89 D8 AC B0 09 20 50 28 64 32 13 08 B4 C7 DA F0 01 10 50 18 8A EE D4 92 08 20 50 28 C8 01 32 13 08 E5 B6 F9 A1 09 10 50 18 89 88 E0 B4 08 20 50 28 C8 01 32 13 08 DF CF DE A2 07 10 50 18 8A EE D4 82 0E 20 50 28 AC 02 32 13 08 F8 98 CB C0 0A 10 50 18 E4 E0 8D A5 0E 20 50 28 AC 02 3A 1E 0A 10 24 0E 00 E1 A9 00 00 10 00 00 00 00 00 00 00 1F 10 50 18 89 88 E0 CC 04 20 50 28 64 3A 1E 0A 10 24 0E 00 E1 A9 00 00 10 00 00 00 00 00 00 00 42 10 50 18 89 88 E0 F4 0C 20 50 28 64 3A 1F 0A 10 24 02 4E 00 80 20 00 02 00 00 00 00 00 00 00 A9 10 50 18 89 E6 80 B8 02 20 50 28 C8 01 3A 1F 0A 10 24 02 4E 00 80 20 00 02 00 00 00 00 00 00 00 A7 10 50 18 89 E6 80 A0 06 20 50 28 C8 01 3A 1F 0A 10 24 08 80 F1 00 31 00 10 00 00 00 00 00 00 00 40 10 50 18 89 88 8C DC 06 20 50 28 AC 02 3A 1F 0A 10 24 02 4E 00 80 10 00 00 00 00 00 00 00 00 01 58 10 50 18 89 DC C4 DC 03 20 50 28 AC 02 33 00 00 00 02 6C 97 2A BD 0B 8C 98 0C A8 0C

                // 02 00 00 01 2C 00 01 00 02 40 63 83 C1 08 08 12 15 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 38 39 10 90 3F 18 00 12 13 0A 0D 31 31 33 2E 39 36 2E 31 33 2E 31 32 35 10 50 18 00 12 13 0A 0C 34 32 2E 38 31 2E 31 37 32 2E 36 33 10 B0 6D 18 00 12 15 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 33 34 10 BB 03 18 00 12 13 0A 0D 31 32 35 2E 39 34 2E 36 30 2E 31 34 36 10 50 18 00 12 13 0A 0D 34 32 2E 38 31 2E 31 37 32 2E 31 34 37 10 50 18 00 12 18 0A 11 6D 73 66 77 69 66 69 2E 33 67 2E 71 71 2E 63 6F 6D 10 90 3F 18 00 12 13 0A 0D 34 32 2E 38 31 2E 31 37 32 2E 31 34 37 10 50 18 00 1A 14 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 32 32 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 1A 14 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 32 32 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 03
                // 02 00 00 01 2B 00 01 00 02 CB 1E 16 27 08 08 12 13 0A 0C 34 32 2E 38 31 2E 31 36 39 2E 34 36 10 90 3F 18 00 12 12 0A 0C 34 32 2E 38 31 2E 31 37 32 2E 38 31 10 50 18 00 12 15 0A 0E 31 31 34 2E 32 32 31 2E 31 34 38 2E 35 39 10 B0 6D 18 00 12 14 0A 0D 34 32 2E 38 31 2E 31 37 32 2E 31 34 37 10 BB 03 18 00 12 13 0A 0D 31 32 35 2E 39 34 2E 36 30 2E 31 34 36 10 50 18 00 12 15 0A 0F 31 31 34 2E 32 32 31 2E 31 34 34 2E 32 31 35 10 50 18 00 12 18 0A 11 6D 73 66 77 69 66 69 2E 33 67 2E 71 71 2E 63 6F 6D 10 90 3F 18 00 12 12 0A 0C 34 32 2E 38 31 2E 31 37 32 2E 32 32 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 1A 14 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 32 32 10 50 18 00 1A 14 0A 0E 31 31 34 2E 32 32 31 2E 31 34 34 2E 32 32 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 1A 13 0A 0D 34 32 2E 38 31 2E 31 36 39 2E 31 30 35 10 50 18 00 03


                /*
                + '
42.81.169.46 ?
42.81.172.81P
114.221.148.59 m
42.81.172.147
125.94.60.146P
114.221.144.215P
msfwifi.3g.qq.com ?
42.81.172.22P
42.81.169.105P
114.221.144.22P
114.221.144.22P
42.81.169.105P
42.81.169.105P
                 */
                null
            }
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqJceStruct?, sequenceId: Int): OutgoingPacket? {
            if (packet == null) {
                return null
            }
            return network.run {
                buildResponseUniPacket(
                    client,
                    sequenceId = sequenceId
                ) {
                    writeJceStruct(
                        RequestPacket.serializer(),
                        RequestPacket(
                            iRequestId = 0,
                            iVersion = 3,
                            sServantName = "QQService.ConfigPushSvc.MainServant",
                            sFuncName = "PushResp",
                            sBuffer = jceRequestSBuffer(
                                "PushResp",
                                PushResp.serializer(),
                                PushResp(
                                    type = packet.type,
                                    seq = packet.seq,
                                    jcebuf = if (packet.type == 3) packet.jcebuf else null
                                )
                            )
                        ),
                        charset = JceCharset.UTF8
                    )
                    // writePacket(this.build().debugPrintThis())
                }
            }
        }
    }
}