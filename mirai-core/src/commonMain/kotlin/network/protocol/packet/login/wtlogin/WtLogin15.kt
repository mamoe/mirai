/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArray
import java.util.*
import kotlin.math.abs

internal object WtLogin15 : WtLoginExt {
    private const val subCommand = 15.toShort()

    private const val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
    ) = WtLogin.ExchangeEmp.buildOutgoingUniPacket(client, bodyType = 2, key = ByteArray(16)) {
//        writeSsoPacket(client, client.subAppId, WtLogin.ExchangeEmp.commandName, sequenceId = sequenceId) {
        writeOicqRequestPacket(
            client,
            EncryptMethodSessionKeyNew(
                client.wLoginSigInfo.wtSessionTicket.data,
                client.wLoginSigInfo.wtSessionTicketKey
            ),
            0x0810
        ) {
            writeShort(subCommand) // subCommand
            writeShort(24)


//            writeFully(("00 18 00 16 00 01 00 00 06 00 00 00 00 10 00 00 00 00 76 E4 B8 DD 00 00 00 00 00 01 00 14 00 01 5A 11 60 11 76 E4 B8 DD 60 0D 44 35 90 E8 11 1B 00 00 " +
//                    //"01 06 " +
//                    //client.wLoginSigInfo.encryptA1!!.size.toShort().toUHexString() + " " + client.wLoginSigInfo.encryptA1!!.toUHexString() + " "+
//                    "01 06 00 78 CD 52 75 7E B7 CF A0 FC 58 15 1E 79 29 86 F0 28 06 AE 4D E4 EF 39 6B 8B 64 19 90 7E DF 2B 71 48 53 E6 71 19 4A 7F 81 06 EB 19 A2 CA 8B B3 47 D9 07 73 04 71 99 B3 F2 7E 2B F0 93 6B A0 FA 94 D3 15 41 55 6C EF 87 96 47 22 5C 6B 10 0F EC 27 3A 8F 83 0A 75 8B 95 EA 81 02 AA C6 E7 C8 6C 7B C3 93 4E EC 08 2E DB 9B 3E 4B 17 7C 06 1C 66 31 F4 EE 70 55 87 49 A2 5B 25 " +
//                    "01 16 00 0E 00 0A F7 FF 7C 00 01 " +
//                    "04 00 01 5F 5E 10 E2 01 00 00 16 00 01 00 00 00 0F 00 00 00 10 20 02 FD E2 00 00 00 00 02 10 10 E0 01 07 00 06 00 00 00 00 00 01 01 44 01 B8 20 3F 4F ED A0 CD 3B CC 07 47 A3 91 8B B5 C8 18 EA D1 45 8A 0F F3 1F CD 95 85 00 7C AD 7F 18 7C 43 B6 A6 C3 FF 53 D5 F6 F4 E6 75 1D 4F AF A4 DF C5 EB 3D 6F C5 C3 21 E9 C7 66 8A EF 4A BD 45 CC D2 A5 34 E4 1D F5 9B 07 9B 65 6A 35 BD B4 0B D1 94 43 18 1C 48 1D B5 2C B5 62 FA E9 E1 35 14 13 BC EE BF FA FD 98 A4 72 A8 1A 71 9C 77 2D 4D BF 58 3A 0A 72 D4 B0 A2 DA 6C FC 04 49 F5 05 3D EF 60 E5 92 9A 04 96 AA A4 22 4E 13 8F 63 3B D9 54 B8 DC CC 2D A0 0B 8B B9 9A D7 8A D0 E4 A4 EE 4F 2E 8D 52 86 35 74 70 93 A0 21 0D 98 DE A4 5B B9 79 A5 8E 31 8D A5 AC 0B DB A8 65 6C 93 1C EE FB E9 A2 FD 22 90 0B A5 41 3D 1F 2B A9 84 FF D8 64 DF 94 48 B3 D6 20 47 F3 12 D3 63 F0 84 1C 6A 1E 0A 8B 13 02 EA F2 C3 3E 41 87 59 5A 65 80 E4 7C 3E FC 52 70 20 26 ED 27 91 01 C0 4D 50 23 B9 1F 59 77 AE D5 4D C8 57 DB 86 E9 5B 98 0C 95 A2 15 AB 01 3E 10 D5 3B 01 57 FE C8 88 80 4D 1A 8A 4D 64 89 C3 7F E6 73 D3 04 C8 EA 98 E2 F3 82 48 7D FC D7 CF 07 " +
//                    "F4 33 F1 1E D3 1C 0D 48 37 3A 50 0B 39 28 AB F3 4F BF C9 D8 70 6F B9 F2 FB 46 5A 4B 21 DE D8 B8 30 A1 FC C6 09 60 4E 07 21 28 F7 CC 7A A0 07 1C 87 42 90 D3 40 07 1B 35 52 56 31 E2 C0 6D 1F 79 43 6C 63 46 D4 92 EE 30 A2 D2 D6 0F 79 46 2A EF C7 C6 CF 54 1D 03 FE 80 D4 28 87 AF 2D 1A FF 71 99 FC 23 09 79 B0 9D B4 E9 0F 4E E3 D2 79 10 2C C7 6E 30 34 A5 66 2E 33 00 08 D0 58 2B 7F D8 E6 21 2E 7E 30 01 42 00 18 00 00 00 14 63 6F 6D 2E 74 65 6E 63 65 6E 74 2E 6D 6F 62 69 6C 65 71 71 01 45 00 10 E2 9A BF 20 0D 00 CA F0 4D 28 CA 66 BF 31 6E AF 01 6A 00 38 CA 78 6A 94 A7 A4 F3 BC 28 58 3C FF 53 41 4C A3 5B 98 AB 7C 21 FB 34 D9 28 59 91 D5 15 12 04 A0 7E 27 25 DF 7A A0 06 87 EB 13 12 10 CD 80 85 78 17 F9 1C D6 21 A7 AF 8C 01 54 00 04 00 01 38 E4 01 41 00 1C 00 01 00 10 43 68 69 6E 61 20 4D 6F 62 69 6C 65 20 47 53 4D 00 02 00 04 77 69 66 69 00 08 00 08 00 00 00 00 08 04 00 00 05 11 00 D3 00 0E 01 00 0A 74 65 6E 70 61 79 2E 63 6F 6D 01 00 11 6F 70 65 6E 6D 6F 62 69 6C 65 2E 71 71 2E 63 6F 6D 01 00 0B 64 6F 63 73 2E 71 71 2E 63 6F 6D 01 00 0E 63 6F 6E 6E 65 63 74 2E 71 71 2E 63 6F 6D 01 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 01 00 0A 76 69 70 2E 71 71 2E 63 6F 6D 01 00 11 67 61 6D 65 63 65 6E 74 65 72 2E 71 71 2E 63 6F 6D 01 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 01 00 0B 67 61 6D 65 2E 71 71 2E 63 6F 6D 01 00 0C 71 71 77 65 62 2E 71 71 2E 63 6F 6D 01 00 09 74 69 2E 71 71 2E 63 6F 6D 01 00 0D 6F 66 66 69 63 65 2E 71 71 2E 63 6F 6D 01 00 0B 6D 61 69 6C 2E 71 71 2E 63 6F 6D 01 00 0A 6D 6D 61 2E 71 71 2E 63 6F 6D 01 47 00 1D 00 00 00 10 00 05 38 2E 35 2E 35 00 10 A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D 01 77 00 11 01 5F EC 50 93 00 0A 36 2E 30 2E 30 2E 32 34 36 33 " +
//                    "04 00 00 48 2F C5 1F 51 62 56 7E A0 39 92 E9 DC 0D 55 00 E5 BA 24 8D 38 47 8F 1C 7F 6A 91 54 62 D9 B3 25 B0 2D 10 A2 15 5E B4 C7 5F 76 CA AC EF 76 88 92 F4 FF 16 55 98 EB 01 BB 4D 34 95 9E 8E 80 59 4E B3 99 1E 80 CD 4E 27 A4 8E" +
//                    " 01 87 00 10 30 86 EC F4 ED 94 5D D2 6F 88 8A 39 46 6C 22 7D 01 88 00 10 9D B6 A8 DF CB C1 79 06 EB 5D 95 FA 20 5A 9E 11 01 94 00 10 73 A1 26 09 B8 99 62 29 04 95 B9 9E 5C DA 22 8C 02 02 00 1B 00 10 36 85 4A A7 02 92 35 CE 9F B5 A2 D0 7A E4 88 F8 00 07 22 38 32 45 46 45 22 05 16 00 04 00 00 00 00 05 21 00 06 00 00 00 00 00 00 05 25 00 47 00 01 05 36 00 41 01 03 00 00 00 00 76 E4 B8 DD 04 1B 11 E8 90 60 0D 3C 28 20 02 FD E2 00 00 00 00 76 E4 B8 DD 04 1B 11 E8 90 60 0D 3C 28 20 02 FD E2 00 00 00 00 76 E4 B8 DD 04 1B 11 E8 90 60 0D 3C 95 20 02 FD E2 " +
//                    "").hexToBytes())
//            return@writeOicqRequestPacket

            t18(appId, uin = client.uin)
            t1(client.uin, ByteArray(4))

            //  t106(client = client)
            writeShort(0x106)
            val encryptA1 = client.wLoginSigInfo.encryptA1!!
            writeShortLVByteArray(encryptA1)
//            kotlin.run {
//                val key = (client.account.passwordMd5 + ByteArray(4) + client.uin.toInt().toByteArray()).md5()
//                kotlin.runCatching {
//                    TEA.decrypt(encryptA1, key).toUHexString()
//                }.soutv("DEC") // success
//            }

            // val a1 = kotlin.runCatching {
            //     TEA.decrypt(encryptA1, buildPacket {
            //         writeFully(client.device.guid)
            //         writeFully(client.dpwd)
            //         writeFully(client.randSeed)
            //     }.readBytes().md5())
            // }.recoverCatching {
            //     client.tryDecryptOrNull(encryptA1) { it }!!
            // }.getOrElse {
            //     encryptA1.soutv("ENCRYPT A1")
            //     client.soutv("CLIENT")
            //     // exitProcess(1)
            //     // error("Failed to decrypt A1")
            //     encryptA1
            // }

            t116(client.miscBitMap, client.subSigMap)
            //t116(0x08F7FF7C, 0x00010400)

            //t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)
            //t100(appId, 1, client.appClientVersion, client.ssoVersion, mainSigMap = 1048768)
            t100(appId, 2, client.appClientVersion, client.ssoVersion, client.mainSigMap)

            t107(0)

            //t108(client.ksid) // 第一次 exchange 没有 108
            t144(client)
            t142(client.apkId)
            t145(client.device.guid)

            val noPicSig =
                client.wLoginSigInfo.noPicSig ?: error("Internal error: doing exchange emp 15 while noPicSig=null")
            t16a(noPicSig)

            t154(0)
            t141(client.device.simInfo, client.networkType, client.device.apn)
            t8(2052)
            t511()
            t147(appId, client.apkVersionName, client.apkSignatureMd5)
            t177(buildTime = client.buildTime, buildVersion = client.sdkVersion)

            // new
            t400(
                g = client.G,
                uin = client.uin,
                guid = client.device.guid,
                dpwd = client.dpwd,
                appId = 1,
                subAppId = 16,
                randomSeed = client.randSeed
            )

            t187(client.device.macAddress)
            t188(client.device.androidId)
            t194(client.device.imsiMd5)
            t202(client.device.wifiBSSID, client.device.wifiSSID)
            t516()

            t521() // new
            t525(client.loginExtraData) // new
            //t544() // new
        }

        //  }
    }
}

@Suppress("FunctionName", "SpellCheckingInspection")
internal fun get_mpasswd(): String {
    var var5: String
    val random = Random()
    run label41@{
        val var6 = ByteArray(16)
        random.nextBytes(var6)
        var var0 = 0
        var var4 = ""
        while (true) {
            var5 = var4
            if (var0 >= var6.size) {
                return var5
            }
            val var3: Boolean = Random().nextBoolean()
            val var2: Int = abs(var6[var0] % 26)

            val var1: Byte = if (var3) {
                97
            } else {
                65
            }
            var4 += ((var1 + var2).toChar()).toString()

            ++var0
        }
    }
}
