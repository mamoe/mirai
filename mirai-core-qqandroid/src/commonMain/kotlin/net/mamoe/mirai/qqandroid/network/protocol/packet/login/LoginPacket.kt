package net.mamoe.mirai.qqandroid.network.protocol.packet.login


import kotlinx.io.core.*
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.*
import net.mamoe.mirai.qqandroid.network.protocol.LoginType
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.utils.GuidSource
import net.mamoe.mirai.qqandroid.utils.MacOrAndroidIdChangeFlag
import net.mamoe.mirai.qqandroid.utils.guidFlag
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.io.discardExact

/**
 * OicqRequest
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
internal object LoginPacket : PacketFactory<LoginPacket.LoginPacketResponse>("wtlogin.login") {

    object SubCommand9 {
        private const val appId = 16L
        private const val subAppId = 537062845L

        @UseExperimental(MiraiInternalAPI::class)
        operator fun invoke(
            client: QQAndroidClient
        ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
            writeSsoPacket(client, subAppId, commandName, sequenceId = sequenceId) {
                writeOicqRequestPacket(client, EncryptMethodECDH7(client.ecdh), 0x0810) {
                    writeShort(9) // subCommand
                    writeShort(17) // count of TLVs, probably ignored by server?
                    //writeShort(LoginType.PASSWORD.value.toShort())

                    t18(appId, client.appClientVersion, client.uin)
                    t1(client.uin, client.device.ipAddress)
                    t106(
                        appId,
                        subAppId /* maybe 1*/,
                        client.appClientVersion,
                        client.uin,
                        1,
                        client.account.passwordMd5,
                        0,
                        client.uin.toByteArray(),
                        client.tgtgtKey,
                        true,
                        client.device.guid,
                        LoginType.PASSWORD
                    )

                    /* // from GetStWithPasswd
                    int mMiscBitmap = this.mMiscBitmap;
                    if (t.uinDeviceToken) {
                        mMiscBitmap = (this.mMiscBitmap | 0x2000000);
                    }


                    // defaults true
                    if (ConfigManager.get_loginWithPicSt()) appIdList = longArrayOf(1600000226L)
                    */
                    t116(client.miscBitMap, client.subSigMap)
                    t100(appId, subAppId, client.appClientVersion, client.mainSigMap or 0xC0)
                    t107(0)

                    // t108(byteArrayOf())
                    // ignored: t104()
                    t142(client.apkId)

                    // if login with non-number uin
                    // t112()
                    t144(
                        androidId = client.device.androidId,
                        androidDevInfo = client.device.generateDeviceInfoData(),
                        osType = client.device.osType,
                        osVersion = client.device.version.release,
                        networkType = client.networkType,
                        simInfo = client.device.simInfo,
                        unknown = byteArrayOf(),
                        apn = client.device.apn,
                        isGuidFromFileNull = false,
                        isGuidAvailable = true,
                        isGuidChanged = false,
                        guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag.NoChange),
                        buildModel = client.device.model,
                        guid = client.device.guid,
                        buildBrand = client.device.brand,
                        tgtgtKey = client.tgtgtKey
                    )

                    //this.build().debugPrint("傻逼")
                    t145(client.device.guid)
                    t147(appId, client.apkVersionName, client.apkSignatureMd5)

                    if (client.miscBitMap and 0x80 != 0) {
                        t166(1)
                    }

                    // ignored t16a because array5 is null

                    t154(sequenceId)
                    t141(client.device.simInfo, client.networkType, client.device.apn)
                    t8(2052)

                    t511(
                        listOf(
                            "tenpay.com",
                            "openmobile.qq.com",
                            "docs.qq.com",
                            "connect.qq.com",
                            "qzone.qq.com",
                            "vip.qq.com",
                            "qun.qq.com",
                            "game.qq.com",
                            "qqweb.qq.com",
                            "office.qq.com",
                            "ti.qq.com",
                            "mail.qq.com",
                            "qzone.com",
                            "mma.qq.com"
                        )
                    )

                    // ignored t172 because rollbackSig is null
                    // ignored t185 because loginType is not SMS
                    // ignored t400 because of first login

                    t187(client.device.macAddress)
                    t188(client.device.androidId)

                    val imsi = client.device.imsiMd5
                    if (imsi.isNotEmpty()) {
                        t194(imsi)
                    }
                    t191()

                    /*
                    t201(N = byteArrayOf())*/

                    val bssid = client.device.wifiBSSID
                    val ssid = client.device.wifiSSID
                    if (bssid != null && ssid != null) {
                        t202(bssid, ssid)
                    }

                    t177()
                    t516()
                    t521()

                    t525(buildPacket {
                        t536(buildPacket {
                            //com.tencent.loginsecsdk.ProtocolDet#packExtraData
                            writeByte(1) // const
                            writeByte(0) // data count
                        }.readBytes())
                    })
                    // this.build().debugPrint("傻逼")

                    // ignored t318 because not logging in by QR
                }
            }
        }
    }


    sealed class LoginPacketResponse : Packet {
        object Success : LoginPacketResponse()
        sealed class Captcha : LoginPacketResponse() {
            class Slider(
                val data: IoBuffer
            ) : Captcha()

            class Picture(
                val data: IoBuffer
            ) : Captcha()
        }
    }

    @UseExperimental(MiraiDebugAPI::class)
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacketResponse = this.debugPrint("login解析").run {
        // 00 09 sub cmd
        // 00 type
        // 00 02
        //
        // 01 19
        // [07 D8] 6E C6 D1 93 B9 E7 75 E0 BE 9A 96 CA DA 3E 63 92 32 4F D3 92 24 81 5B E8 99 A1 06 E1 66 7D 22 38 77 8B C8 8C 82 FD 04 A4 9A DA 99 64 A7 69 DA 2E 29 F8 DF BF 8A CF 3C 53 BA 24 FC E1 2D E7 72 A4 29 12 D8 91 D7 4C C3 8D D3 FE 6B 7A B7 A0 F0 82 E9 BF 3E BF D7 80 5C 79 46 FB ED 41 5C 54 69 5F 2F CC A3 27 CA 2F A2 DB 0A CF 1A 27 69 57 1E CB 70 28 8A 57 AE 76 1A AC 1E EC F7 2C 8E 90 32 F0 54 96 28 DF 33 54 CA C0 A8 A1 54 3F 48 2F 75 07 31 98 CE 3B 59 47 EA D2 76 A6 30 98 AF 71 A7 00 8E 8E 36 E5 02 68 89 C5 B6 B5 09 DD 04 7A 6A AB 13 AE 70 B7 C4 A6 68 87 7F 0F 66 95 A8 CD FD 52 7A 53 7B F0 5B 13 86 41 F8 04 13 8A 96 BC 8A 29 D3 DA 53 4A 5C 35 09 31 46 FA 24 08 14 69 06 94 97 6C 66 F7 6F FB C4 51 75 15 83 CB 0B 6B 7B 97 10 EE A3 59 76 05 14 2E 5E 85 87 CC 80 5E 8E 24 C9 A1 3B 1B 8D 88 3B 63 3D 5E A6 A1 5A FB 6D B1 39 93 E2 27 00 F5 4D 02 10 BD 06 67 0D AA 08 90 14 21 25 91 7A B3 75 47 02 B3 0A 8C 3F 12 03 BE F0 0A 0D 37 38 E0 CD 72 93 3B 1E 49 B6 1B 16 D8 42 38 EA 67 E7 F8 9E AE A7 50 27 1D 8D AA 91 08 9E 91 AE 4A D0 E0 EB B5 99 E4 3E EF E2 10 0D 16 71 F6 24 F5 DD E2 1A 9D 90 90 ED 42 EA 4D 05 48 24 F3 63 0F DE 4D 56 F6 CA 37 63 D8 C8 3F 97 25 07 2D 10 80 33 82 76 05 2B BE B6 6D 90 2B 63 B2 BA 28 11 EB 42 EB 5D 9C 7B 39 50 66 A1 2F BD 85 69 9C 87 7A B5 47 E2 5C 95 EB D6 C9 D6 1A 5E 9D 7E 6A ED C0 92 45 45 3B A4 04 05 13 EF AC 69 A3 13 4F CD 48 AD 1B 6B CD 40 F0 B8 1B 42 EC F8 AD C1 9E 8F E4 2D 83 63 A2 0F DD 2F 0D 6D C1 00 18 D5 9C 92 CC 72 7C C4 E8 A9 39 8A EF 22 C9 02 AE DE DE 18 31 44 9F B9 3D 4D CB 24 86 3D DF B9 AA 28 6B C5 70 F9 8A 62 75 C0 4D 2C 53 BE B0 36 81 78 3E E8 3A AE 79 9C 0B 2C 3A B2 A4 2B 78 FC F7 31 A2 BE 98 4B 07 B2 35 29 31 A0 D1 04 EB 05 2E 76 CE 1B C5 AE 74 68 E3 83 10 2B 65 E7 63 DD DC 4E 4C A7 90 4C C3 BE 87 EF 60 EA C7 FB 06 7A 77 6B 8E 56 00 9D 72 3A 6D 6E 7A C4 A0 5E 6D 57 E2 3C E4 74 E9 B7 02 AF 43 A3 EE 7C D2 69 57 99 0D 39 6D 55 2F 13 E0 E5 2E D0 3D 39 2C 9C 1C 1F 18 D9 30 0F 88 1A 66 E5 51 E6 60 1F 6F B3 D9 B5 56 4D 8F 01 52 C0 1E 00 BC 8A 2C 78 3B DB 68 59 EB 3A 84 E0 CB 6E E1 28 2E AE 86 1D 86 79 F3 13 41 EA 86 A2 7C BE 54 0E 6E CB DA BC 3A E9 F1 5F BF 2A D0 73 90 44 46 7B 8F A8 30 0F B1 35 A3 8D 24 32 A1 57 B8 B3 E3 FC 1F CC EB 3F 1F 64 9E 6B C1 03 06 5C 3B 68 B6 FA DA A5 C9 DF 50 19 22 5B B4 FA 0F 17 28 5B 6C 03 1D D6 7E D8 69 3F B3 B5 33 79 FC 1A EC 7C 82 7C C6 AB E0 B5 A7 9F A0 9E 67 09 51 E5 BE 6C 38 62 17 55 DD CC B7 81 49 D9 98 0E 19 A6 E6 4E 20 93 99 E8 3C CF C1 1B 46 DE 30 29 94 47 71 2F 5A C6 AD 41 55 95 78 2B E4 48 55 E0 D9 A8 D0 2A 80 65 B0 50 DC 53 D3 D6 38 53 72 67 50 FC BC CC 22 7D 03 0F DE DF BF 5E 50 63 5B 50 4C 0C 54 DA 6E 35 34 68 D0 DD 21 04 B6 B9 C2 06 C7 30 34 54 30 BA 1D 58 DE 99 53 A3 BC 85 71 6D 3B B3 4B F8 59 1D 33 9D 0A 09 12 F9 BD BC 37 14 1B 89 6D 9E C3 38 46 AE 55 6C A0 E8 89 8B 3E 3D F9 47 E0 03 17 FA E3 5D CA 3C 81 6F 4D F3 52 8A 7A 11 D2 0E 68 C9 4C DD 8F 4C 34 69 DA 13 BE 84 4D 3B A4 A1 FB 35 0B D8 CA 46 B0 DD 4B 78 81 24 F7 9F 6C AE 76 BD CB D6 02 A8 A5 7E 8A 6D 98 B3 10 90 ED 2E 5A D0 6B 66 AF 20 29 EF 66 BE DD 14 EA E3 1D 09 80 E6 DF 84 EA 3F 0B EA 2B D5 3F 91 49 4D 9D DD A4 07 47 8B 1C 1E 85 98 5D B9 48 37 09 E4 1D 57 28 06 5D D2 79 A7 47 F4 7E 39 CC 7E DD 16 B2 14 40 E4 1C EC 39 2F 63 02 C3 19 68 6A 80 C1 C5 0A 40 CC 37 60 90 CC 22 EE CF B7 3F AB 59 21 83 07 5E 9D AA C8 DB 53 65 7D 11 FA 42 53 E8 30 12 FB 9F E4 6C 31 5C 24 04 46 41 82 32 B2 E0 15 B2 D9 5C 22 B9 29 01 B1 36 3D 01 C9 45 CC D6 DC 0F DC 53 65 12 E6 01 8C CF DA 74 A9 08 37 34 F4 A8 F1 BF 86 00 F6 B5 8F 1C A8 60 6F 64 15 77 03 CE 7E D7 BE 05 C4 10 46 0B 32 78 9D DC 06 79 22 79 A1 6C 68 33 96 2A 20 49 42 DD 5B 89 28 E5 A9 E1 53 47 8A C2 44 C7 A5 BB 87 E7 3A 9F 88 01 B4 B9 78 C4 53 69 E7 8E AA 3E 70 AA FB 8D 4A E1 B6 C7 E4 EF 1F 9A A8 D3 5A 11 6A 01 C5 DB F0 EC 19 D1 9B BE D7 89 71 A8 4C 86 B4 B3 8B B5 47 C9 99 92 8B 8E D7 0B 0A 92 2D 40 9A 32 8D 72 EB 0F 12 09 90 7B B1 AE FB 8F 67 8D 02 6A 00 FF 30 9F CB A2 BC 29 C4 0A D5 07 B4 42 76 38 F2 F1 41 2F C9 F4 E5 5B 8D AF 49 A9 74 5C 76 5A E0 3A F3 9D 2B C7 05 90 3A 8C 9A E1 3D E0 07 17 6D 7B C9 61 D4 C6 CB BE CF 72 1F 2D 64 47 DC D0 97 02 85 EE A2 13 76 99 C6 AD 19 4C 5A E3 BE 48 81 88 84 FB F5 47 C3 6C 8A EF AD 60 7D A8 FD F9 C1 3D 1F CE 70 BC 06 CC 16 EC F6 15 BA 95 0F 9C 7D 20 F9 76 E2 8C B8 FF 63 15 1A CD 48 08 16 AA 11 4E 57 27 5F 5F E8 06 EB 21 94 82 BA 50 F8 39 FE 45 1E E8 7C 71 67 09 1C 0E 74 E1 40 75 BA 67 D2 0E DE 75 72 20 18 FC D3 C2 06 CF AC 53 11 08 6C 5D 16 8E B2 E9 D7 B4 54 46 6A 65 75 9B 83 76 76 09 0B 67 25 DE 79 E3 48 48 14 39 93 79 F2 B8 90 48 9F 78 A9 62 98 2F BC EB 06 A4 99 18 B6 D7 98 71 5E 02 6B A2 98 93 2F 42 B8 D4 51 8D 0D 16 73 90 B1 C5 5C 9B 0E 9C FD 02 4B 35 8A 85 B7 34 79 E3 68 C0 14 7B 5A FB 4B 8C 3B 95 66 23 2C F8 B5 0A D6 DC A4 3B 09 FF 81 65 45 37 21 61 38 C8 20 3C D9 A4 B6 88 0B 0C F7 75 7D 9F 47 FC F7 AB C2 FC F1 05 CF 38 D0 36 44 B2 7A 02 06 A6 54 43 82 C8 7E C2 DB 22 F9 44 41 1F 12 96 0D 66 AD 28 7D 22 0E E4 4B A1 03 A6 DA 32 08 0E 35 6F F2 4A 38 5E 31 C4 CC F8 85 C2 80 CD FF F1 1F 38 2D DA FD 28 6F 34 1A 70 16 04 42 0D 37 E6 A3 9F 93 CC DE 41 97 25 A2 9F DB 3F F6 C0 7B FF 8E 7A F1 D8 75 C2 1C E6 63 66 3D 63 1A 44 B8 D5 95 BA C5 5B 55 92 B1 BD 20 20 07 85 6C BE 45 B8 62 6D E1 8B 8E 14 EE AB 50 A2 45 5E 22 76 B8 AC AB A1 81 FC 95 F3 0E 31 48 C4 F7 6A 7B EE 2A 44 53 0D E2 43 98 8D A9 79 6D 41 3F AE 1D CE ED 87 D5 EF 57 4B F6 C3 4E D6 66 36 62 A3 F0 B9 CB 8C 80 44 AE 38 63 E6 DE 48 77 6E 10 24 B2 5A CE 7F E1 2A 69 0C 79 DC D8 AB 3C CB 4B 0E 92 5F E4 AE 23 1E 7D 15 19 ED 6C 4E 26 6E 52 BA A7 C4 D5 A3 7F 15 D9 81 3E CE D0 BD E1 19 8E 48 F7 8C 9D 62 9E 68 5C 10 EA 54 4C 72 7F BB 39 16 E4 D7 6E 7E D3 97 2E A9 F2 4A BD 9D C6 74 EB F9 78 42 43 78 95 8B F6 A3 68 7E 13 DD 00 5E F5 51 3F 81 78 B2 9C 7B 0E 62 E2 1E 99 08 1F AA 2E C1 CF 57 F0 39 C0 59 EB 86 C2 63 87 46 B0 DB 6B 33 56 A5 33 7A DF 78 07 56 63 3D C1 48 4E 62 B8 95 D8 F0 DB EB D3 BC 62 C3 1B 2D 86 90 A7 3A 20 29 6A 12 A6 11 34 4C 5C 43 3A 8F 29 C5 44 FA AA DF 8D 7F 5E 62 AF 48 9C 7E 34 35 5E 0A EC 2B 2D EC B3 56 0C 62 1C F0 3B BE B6 56 21 E0 39 56 27 2C 23 96 01 61 00 02 00 00


        val subCommand = readUShort().toInt()
        println("subCommand=$subCommand")
        val type = readByte()
        println("type=$type")
        when (type.toInt()) {
            0 -> {
                onLoginSuccess(bot)
            }
            1 -> {
                throw Exception("Wrong Password")
            }
            2 -> {
                onSolveLoginCaptcha(bot)
            }
        }
        if (type.toInt() != 0) {
            DebugLogger.debug("unknown login result type: $type")
        }
        return LoginPacketResponse.Success
    }

    @UseExperimental(MiraiDebugAPI::class)
    fun ByteReadPacket.onSolveLoginCaptcha(bot: QQAndroidBot) = this.debugPrint("login验证码解析").run {
        val client = bot.client
        val tlvMap: Map<Int, ByteArray> = this.readTLVMap()
        tlvMap[0x104]?.let { client.analysisTlv150(it) }
        tlvMap[0x192]?.let { client.analysisTlv150(it) }
    }

    @UseExperimental(MiraiDebugAPI::class)
    fun ByteReadPacket.onLoginSuccess(bot: QQAndroidBot) = this.debugPrint("login成功解析").run {
        val client = bot.client
        debugDiscardExact(2)
        val tlvMap: Map<Int, ByteArray> = this.readTLVMap()
        println("TLV KEYS: " + tlvMap.keys.joinToString { it.contentToString() })

        tlvMap[0x150]?.let { client.analysisTlv150(it) }
        tlvMap[0x305]?.let { println("TLV 0x305=${it.toUHexString()}") }
        tlvMap[0x161]?.let { client.analysisTlv161(it) }
        tlvMap[0x119]?.let { t119Data ->
            t119Data.decryptBy(client.tgtgtKey).toReadPacket().debugPrint("0x119data").apply {
                discardExact(2) // always discarded.  00 1C
                // 00 1C
                // 01 08 00 10 A1 73 76 98 64 E0 38 C6 C8 18 73 FA D3 85 DA D6 01 6A 00 30 1D 99 4A 28 7E B3 B8 AC 74 B9 C4 BB 6D BB 41 72 F7 5C 9F 0F 79 8A 82 4F 1F 69 34 6D 10 D6 BB E8 A3 4A 2B 5D F1 C7 05 3C F8 72 EF CF 67 E4 3C 94 01 06 00 78 B4 ED 9F 44 ED 10 18 A8 85 0A 8A 85 79 45 47 7F 25 AA EE 2C 53 83 80 0A B3 B0 47 3E 95 51 A4 AE 3E CA A0 1D B4 91 F7 BB 2E 94 76 A8 C8 97 02 C4 5B 15 02 B7 03 9A FC C2 58 6D 17 92 46 AE EB 2F 6F 65 B8 69 6C D6 9D AC 18 6F 07 53 AC FE FA BC BD CE 57 13 10 2D 5A C6 50 AA C2 AE 18 D4 FD CD F2 E0 D1 25 29 56 21 35 8F 01 9D D6 69 44 8F 06 D0 23 26 D3 0E E6 E6 B7 01 0C 00 10 73 32 61 4E 2C 72 35 58 68 28 47 3E 2B 6E 52 62 01 0A 00 48 A4 DA 48 FB B4 8D DA 7B 86 D7 A7 FE 01 1B 70 6F 54 F8 55 38 B0 AD 1B 0C 0B B9 F6 94 24 F8 9E 30 32 22 99 0C 22 CD 44 B8 B0 8A A8 65 E1 B8 F0 49 EF E1 23 D7 0D A3 F1 BB 52 B7 4B AF BD 50 EA BF 15 02 78 2B 8B 10 FB 15 01 0D 00 10 29 75 38 72 21 5D 3F 24 37 46 67 79 2B 65 6D 34 01 14 00 60 00 01 5E 19 65 8C 00 58 93 DD 4D 2C 2D 01 44 99 62 B8 7A EF 04 C5 71 0B F1 BE 4C F4 21 F2 97 B0 14 67 0E 14 9F D8 A2 0B 93 40 90 80 F3 59 7A 69 45 D7 D4 53 4C 08 3A 56 1D C9 95 36 2C 7C 5E EE 36 47 5F AE 26 72 76 FD FD 69 E6 0C 2D 3A E8 CF D4 8D 76 C9 17 C3 E3 CD 21 AB 04 6B 70 C5 EC EC 01 0E 00 10 56 48 3E 29 3A 5A 21 74 55 6A 2C 72 58 73 79 71 01 03 00 30 9B A6 5D 85 5C 40 7C 28 E7 05 A9 25 CA F5 FC C0 51 40 85 F3 2F D2 37 F9 09 A6 E6 56 7F 7A 2E 7D 9F B9 1C 00 65 55 D2 A9 60 03 77 AB 6A F5 3F CE 01 33 00 30 F4 3A A7 08 E2 04 FA C8 9D 54 49 DE 63 EA F0 A5 1C C4 03 57 51 B6 AE 0B 55 41 F8 AB 22 F1 DC A3 B0 73 08 55 14 02 BF FF 55 87 42 4C 23 70 91 6A 01 34 00 10 61 C7 02 3F 1D BE A6 27 2F 24 D4 92 95 68 71 EF 05 28 00 1A 7B 22 51 49 4D 5F 69 6E 76 69 74 61 74 69 6F 6E 5F 62 69 74 22 3A 22 31 22 7D 03 22 00 10 CE 1E 2E DC 69 24 4F 9B FF 2F 52 D8 8F 69 DD 40 01 1D 00 76 5F 5E 10 E2 34 36 79 27 23 53 4D 65 6B 6A 33 6D 7D 4E 3C 5F 00 60 00 01 5E 19 65 8C 00 58 67 00 9C 02 E4 BC DB A3 93 98 A1 ED 4C 91 08 6F 0C 06 E0 12 6A DC 14 5B 4D 20 7C 82 83 AE 94 53 A2 4A A0 35 FF 59 9D F3 EF 82 42 61 67 2A 31 E7 87 7E 74 E7 A3 E7 5C A8 3C 87 CF 40 6A 9F E5 F7 20 4E 56 C6 4F 1C 98 3A 8B A9 4F 1D 10 35 C2 3B A1 08 7A 89 0B 25 0C 63 01 1F 00 0A 00 01 51 80 00 00 03 84 00 00 01 38 00 0E 00 00 00 01 01 0A 00 27 8D 00 00 00 00 00 01 1A 00 13 02 5B 06 01 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 05 22 00 14 00 00 00 00 76 E4 B8 DD AB 53 02 9F 5E 19 65 8C 20 02 ED BD 05 37 00 17 01 01 00 00 00 00 76 E4 B8 DD 04 AB 53 02 9F 5E 19 65 8C 20 02 ED BD 01 20 00 0A 4D 39 50 57 50 6E 4C 31 65 4F 01 6D 00 2C 31 7A 50 7A 63 72 70 4D 30 43 6E 31 37 4C 32 32 6E 77 2D 36 7A 4E 71 48 48 59 41 35 48 71 77 41 37 6D 76 4F 63 2D 4A 56 77 47 51 5F 05 12 03 5D 00 0E 00 0A 74 65 6E 70 61 79 2E 63 6F 6D 00 2C 6E 4A 72 55 55 74 63 2A 34 7A 32 76 31 66 6A 75 77 6F 6A 65 73 72 76 4F 68 70 66 45 76 4A 75 55 4B 6D 34 43 2D 76 74 38 4D 77 38 5F 00 00 00 11 6F 70 65 6E 6D 6F 62 69 6C 65 2E 71 71 2E 63 6F 6D 00 2C 78 59 35 65 62 4D 74 48 44 6D 30 53 6F 68 56 71 68 33 43 79 79 34 6F 63 65 4A 46 6A 51 58 65 68 30 44 61 75 55 30 6C 78 65 52 6B 5F 00 00 00 0B 64 6F 63 73 2E 71 71 2E 63 6F 6D 00 2C 64 6A 62 79 47 57 45 4F 34 58 34 6A 36 4A 73 48 45 65 6B 73 69 74 72 78 79 62 57 69 77 49 68 46 45 70 72 4A 59 4F 2D 6B 36 47 6F 5F 00 00 00 0E 63 6F 6E 6E 65 63 74 2E 71 71 2E 63 6F 6D 00 2C 64 4C 31 41 79 32 41 31 74 33 58 36 58 58 2A 74 33 64 4E 70 2A 31 61 2D 50 7A 65 57 67 48 70 2D 65 47 78 6B 59 74 71 62 69 6C 55 5F 00 00 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 2C 75 6A 55 5A 4F 6A 4F 48 52 61 75 6B 32 55 50 38 77 33 34 68 36 69 46 38 2A 77 4E 50 35 2D 66 54 75 37 67 39 56 67 44 57 2A 6B 6F 5F 00 00 00 0A 76 69 70 2E 71 71 2E 63 6F 6D 00 2C 37 47 31 44 6F 54 2D 4D 57 50 63 2D 62 43 46 68 63 62 32 56 38 6E 77 4A 75 41 51 63 54 39 77 45 49 62 57 43 4A 4B 44 4D 6C 6D 34 5F 00 00 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 2C 7A 73 70 5A 56 43 59 45 7A 35 2A 4F 6B 4E 68 6E 74 79 61 69 6E 6F 68 4D 32 6B 41 6C 2A 74 31 63 7A 48 57 77 30 41 6A 4B 50 4B 6B 5F 00 00 00 0B 67 61 6D 65 2E 71 71 2E 63 6F 6D 00 2C 32 6F 2D 51 53 36 65 43 70 37 6A 43 4E 34 6A 74 6E 47 4F 4B 33 67 73 32 63 4A 6F 56 71 58 65 44 48 61 55 39 65 34 2D 32 34 64 30 5F 00 00 00 0C 71 71 77 65 62 2E 71 71 2E 63 6F 6D 00 2C 63 54 4D 79 64 51 43 35 50 74 43 45 51 72 6F 33 53 54 41 66 7A 56 2D 44 76 46 56 35 58 6D 56 6B 49 31 68 4C 55 48 4E 65 76 56 38 5F 00 00 00 0D 6F 66 66 69 63 65 2E 71 71 2E 63 6F 6D 00 2C 6F 73 72 54 36 32 69 37 66 76 6D 49 50 64 6F 58 4B 48 74 38 58 52 59 56 77 72 7A 6E 69 31 58 7A 57 4C 77 2A 71 36 33 44 74 73 6F 5F 00 00 00 09 74 69 2E 71 71 2E 63 6F 6D 00 2C 41 61 77 4D 78 4D 32 79 58 51 47 75 72 75 55 6C 66 53 58 79 5A 57 48 53 78 52 57 58 50 74 6B 6B 4F 78 6F 66 4A 59 47 6C 71 68 34 5F 00 00 00 0B 6D 61 69 6C 2E 71 71 2E 63 6F 6D 00 2C 67 72 57 68 58 77 34 4C 6E 4B 49 4F 67 63 78 45 71 70 33 61 45 67 37 38 46 7A 77 4E 6D 4B 48 56 6E 6F 50 4C 4F 32 6D 57 6D 6E 38 5F 00 00 00 09 71 7A 6F 6E 65 2E 63 6F 6D 00 2C 72 61 47 79 51 35 54 72 4D 55 7A 6E 74 31 4E 52 44 2D 50 72 74 72 41 55 43 35 6A 61 2D 49 47 2D 73 77 4C 6D 49 51 51 41 44 4C 41 5F 00 00 00 0A 6D 6D 61 2E 71 71 2E 63 6F 6D 00 2C 39 73 2D 4F 51 30 67 76 39 42 6A 37 58 71 52 49 4E 30 35 46 32 64 4D 47 67 47 43 58 57 4A 62 68 63 30 38 63 7A 4B 52 76 6B 78 6B 5F 00 00 03 05 00 10 77 75 6E 54 5F 7E 66 7A 72 40 3C 6E 35 50 53 46 01 43 00 40 3A AE 30 87 81 3D EE BA 31 9C EA 9D 0D D4 73 B1 81 12 E0 94 71 73 7A B0 47 3D 09 47 E5 1B E1 E2 06 1A CB A4 E3 71 9E A6 EA 2A 73 5C C8 D3 B1 2A B1 C7 DA 04 A6 6D 12 26 DF 6B 8B EC C7 12 F8 E1 01 18 00 05 00 00 00 01 00 01 63 00 10 67 6B 60 23 24 6A 55 39 4E 58 24 5E 39 2B 7A 69 01 38 00 5E 00 00 00 09 01 06 00 27 8D 00 00 00 00 00 01 0A 00 24 EA 00 00 00 00 00 01 1C 00 1A 5E 00 00 00 00 00 01 02 00 01 51 80 00 00 00 00 01 03 00 00 1C 20 00 00 00 00 01 20 00 01 51 80 00 00 00 00 01 36 00 1B AF 80 00 00 00 00 01 43 00 1B AF 80 00 00 00 00 01 64 00 1B AF 80 00 00 00 00 01 30 00 0E 00 00 5E 19 65 8C 9F 02 53 AB 00 00 00 00
                val tlvMap119 = this.readTLVMap()
                println("tlvMap119 KEYS: " + tlvMap119.keys.joinToString { it.contentToString() })
                tlvMap119[0x305]?.let { println("TLV 0x305=${it.toUHexString()}") }
                tlvMap119.filterValues { it.size == 16 }.forEach {
                    println(it.key.toUHexString("") + "=" + it.value.toUHexString())
                }

                // ???
                tlvMap119[0x1c]?.read {
                    val bytes = readBytes()
                    DebugLogger.warning(bytes.toUHexString())
                    DebugLogger.warning(bytes.encodeToString())
                }

                tlvMap119[0x149]?.let { client.analysisTlv149(it) }
                tlvMap119[0x130]?.let { client.analysisTlv130(it) }
                tlvMap119[0x113]?.let { client.analysisTlv113(it) }

                // t528, t530 QQ 中最终保存到 oicq.wlogin_sdk.request.WUserSigInfo#loginResultTLVMap
                tlvMap119[0x528]?.let { client.t528 = it }
                tlvMap119[0x530]?.let { client.t530 = it }

                tlvMap119[0x118]?.let { client.mainDisplayName = it }
                tlvMap119[0x108]?.let { client.ksid = it }

                var openId: ByteArray
                var openKey: ByteArray

                when (val t125 = tlvMap119[0x125]) {
                    null -> {
                        openId = byteArrayOf()
                        openKey = byteArrayOf()
                    }
                    else -> t125.read {
                        openId = readUShortLVByteArray()
                        openKey = readUShortLVByteArray()
                    }
                }

                /*
                util.LOGI("tgt len:" + util.buf_len(t10a.get_body_data()) +
                " tgt_key len:" + util.buf_len(t10d.get_body_data()) +
                " st len:" + util.buf_len(t114.get_body_data()) +
                " st_key len:" + util.buf_len(t10e.get_body_data()) +
                " stwx_web len:" + util.buf_len(t103Data) +
                " lskey len:" + util.buf_len(t11cData) +
                " skey len:" + util.buf_len(t120Data) +
                " sig64 len:" + util.buf_len(t121Data) +
                " openid len:" + util.buf_len(openId) +
                " openkey len:" + util.buf_len(openKey) +
                " pwdflag: " + t186.get_data_len() + t186.getPwdflag(), "" + this.field_61436.uin);

                 */
                tlvMap119[0x186]?.let { client.analysisTlv186(it) }
                tlvMap119[0x537]?.let { client.analysisTlv537(it) }
                tlvMap119[0x169]?.let { t169 ->
                    client.wFastLoginInfo = WFastLoginInfo(
                        outA1 = client.runCatching {
                            parseWFastLoginInfoDataOutA1(t169)
                        }.getOrElse { ByteReadPacket(byteArrayOf()) }
                    )
                }
                tlvMap119[0x167]?.let {
                    val imgType = byteArrayOf(readByte())
                    val imgFormat = byteArrayOf(readByte())
                    val imgUrl = readUShortLVByteArray()
                    // dont move into constructor, keep order
                    client.reserveUinInfo = ReserveUinInfo(imgType, imgFormat, imgUrl)
                }
                client.qrPushSig = tlvMap119[0x317] ?: byteArrayOf()


                val face: Int
                val gender: Int
                val nick: String
                val age: Int
                when (val t11a = tlvMap119[0x11a]) {
                    null -> {
                        face = 0
                        age = 0
                        gender = 0
                        nick = ""
                    }
                    else -> t11a.read {
                        face = readUShort().toInt()
                        age = readUByte().toInt()
                        gender = readUByte().toInt()
                        nick = readUByteLVString()
                    }
                }

                val payToken: ByteArray
                when (val t199 = tlvMap119[0x199]) {
                    null -> payToken = byteArrayOf()
                    else -> t199.read {
                        openId = readUShortLVByteArray()
                        payToken = readUShortLVByteArray()
                    }
                }

                val pf: ByteArray
                val pfKey: ByteArray
                when (val t200 = tlvMap119[0x200]) {
                    null -> {
                        pf = byteArrayOf()
                        pfKey = byteArrayOf()
                    }
                    else -> t200.read {
                        pf = readUShortLVByteArray()
                        pfKey = readUShortLVByteArray()
                    }
                }


                // TODO sigMap??? =0x21410e0 // from qq

                val creationTime = currentTimeSeconds
                val expireTime = creationTime + 2160000L

                val outPSKeyMap: PSKeyMap = mutableMapOf()
                val outPt4TokenMap: Pt4TokenMap = mutableMapOf()

                parsePSKeyMapAndPt4TokenMap(
                    tlvMap119[0x512] ?: error("Cannot find tlv 0x512, which is pskeyMap and pt4tokenMap"),
                    creationTime,
                    expireTime,
                    outPSKeyMap,
                    outPt4TokenMap
                )

                var a1: ByteArray? = null
                var noPicSig: ByteArray? = null
                tlvMap119[0x531]?.let {
                    analysisTlv0x531(it) { arg1, arg2 ->
                        a1 = arg1
                        noPicSig = arg2
                    }
                }

                client.wLoginSigInfo = WLoginSigInfo(
                    uin = client.uin,
                    encryptA1 = a1,
                    noPicSig = noPicSig,
                    G = byteArrayOf(), // defaults {}, from asyncContext._G
                    dpwd = byteArrayOf(), // defaults {}, from asyncContext._G
                    randSeed = tlvMap119.getOrEmpty(0x403), // or from asyncContext._t403.get_body_data()
                    simpleInfo = WLoginSimpleInfo(
                        uin = client.uin,
                        face = face,
                        age = age,
                        gender = gender,
                        nick = nick,
                        imgType = client.reserveUinInfo?.imgType ?: byteArrayOf(),
                        imgFormat = client.reserveUinInfo?.imgFormat ?: byteArrayOf(),
                        imgUrl = client.reserveUinInfo?.imgUrl ?: byteArrayOf(),
                        mainDisplayName = tlvMap119[0x118] ?: error("Cannot find tlv 0x118")
                    ),
                    appPri = tlvMap119[0x11f]?.let { it.read { discardExact(4); readUInt().toLong() } } ?: 4294967295L,
                    a2ExpiryTime = expireTime,
                    loginBitmap = 0, // from asyncContext._login_bitmap
                    tgt = tlvMap119.getOrEmpty(0x10a),
                    a2CreationTime = creationTime,
                    tgtKey = tlvMap119.getOrEmpty(0x10d),
                    sKey = SKey(tlvMap119.getOrEmpty(0x120), creationTime, expireTime),
                    userSig64 = UserSig64(tlvMap119.getOrEmpty(0x121), creationTime),
                    accessToken = AccessToken(tlvMap119.getOrEmpty(0x136), creationTime),
                    openId = openId,
                    openKey = OpenKey(openKey, creationTime),
                    d2 = D2(tlvMap119.getOrEmpty(0x143), creationTime, expireTime),
                    d2Key = tlvMap119.getOrEmpty(0x305),
                    sid = Sid(tlvMap119.getOrEmpty(0x164), creationTime, expireTime),
                    aqSig = AqSig(tlvMap119.getOrEmpty(0x171), creationTime),
                    psKeyMap = outPSKeyMap,
                    pt4TokenMap = outPt4TokenMap,
                    superKey = tlvMap119.getOrEmpty(0x16d),
                    payToken = payToken,
                    pf = pf,
                    pfKey = pfKey,
                    da2 = tlvMap119.getOrEmpty(0x203),
                    wtSessionTicket = WtSessionTicket(tlvMap119.getOrEmpty(0x133), creationTime),
                    wtSessionTicketKey = tlvMap119.getOrEmpty(0x134),
                    deviceToken = tlvMap119.getOrEmpty(0x322),
                    vKey = VKey(tlvMap119.getOrEmpty(0x136), creationTime, expireTime),
                    userStWebSig = UserStWebSig(tlvMap119.getOrEmpty(0x103), creationTime, expireTime),
                    userStSig = UserStSig((tlvMap119.getOrEmpty(0x114)), creationTime),
                    userStKey = tlvMap119.getOrEmpty(0x10e),
                    lsKey = LSKey(tlvMap119.getOrEmpty(0x11c), creationTime, expireTime),
                    userA5 = UserA5(tlvMap119.getOrEmpty(0x10b), creationTime),
                    userA8 = UserA8(tlvMap119.getOrEmpty(0x102), creationTime, expireTime)
                )
            }
        }

    }


    private fun Map<Int, ByteArray>.getOrEmpty(key: Int): ByteArray {
        return this[key] ?: byteArrayOf()
    }

    private inline fun analysisTlv0x531(t531: ByteArray, handler: (a1: ByteArray, noPicSig: ByteArray) -> Unit) {
        val map = t531.toReadPacket().readTLVMap()

        val t106 = map[0x106]
        val t16a = map[0x16a]
        val t113 = map[0x113]
        val t10c = map[0x10c]

        if (t106 != null && t16a != null && t113 != null && t10c != null) {
            handler(t106 + t10c, t16a)
        }
    }

    /**
     * @throws error
     */
    private fun QQAndroidClient.parseWFastLoginInfoDataOutA1(t169: ByteArray): ByteReadPacket {
        val map = t169.toReadPacket().readTLVMap()

        val t106 = map[0x106]
        val t10c = map[0x10c]
        val t16a = map[0x16a]

        check(t106 != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x106!!" }
        check(t10c != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x10c!!" }
        check(t16a != null) { "getWFastLoginInfoDataOutA1: Cannot find tlv 0x16a!!" }

        return buildPacket {
            writeByte(64)
            writeShort(4)

            // TLV
            writeShort(0x106)
            writeShortLVByteArray(t106)

            writeShort(0x10c)
            writeShortLVByteArray(t10c)

            writeShort(0x16a)
            writeShortLVByteArray(t16a)

            t145(device.guid)
        }
    }

    /**
     * login extra data
     */
    private fun QQAndroidClient.analysisTlv537(t537: ByteArray) = t537.read {
        //discardExact(2)
        loginExtraData = LoginExtraData( // args are to correct order
            uin = readUInt().toLong(),
            ip = readUByteLVByteArray(),
            time = readInt(), // correct
            version = readInt()
        )
    }

    /**
     * pwd flag
     */
    private fun QQAndroidClient.analysisTlv186(t186: ByteArray) = t186.read {
        discardExact(1)
        pwdFlag = readByte().toInt() == 1
    }

    /**
     */
    private fun QQAndroidClient.analysisTlv528(t528: ByteArray) = t528.read {
    }

    /**
     * 设置 [QQAndroidClient.uin]
     */
    private fun QQAndroidClient.analysisTlv113(t113: ByteArray) = t113.read {
        uin = readUInt().toLong()

        /*
        // nothing to do

          if (!asyncContext.ifQQLoginInQim(class_1048.productType)) {
              this.field_61436.method_62330(this.field_61436.field_63973, this.field_61436.uin);
          }
         */
    }

    /**
     * 设置 [QQAndroidClient.timeDifference] 和 [QQAndroidClient.ipFromT149]
     */
    private fun QQAndroidClient.analysisTlv130(t130: ByteArray) = t130.read {
        discardExact(2)
        timeDifference = readUInt().toLong() - currentTimeMillis
        ipFromT149 = readBytes(4)
    }

    private fun QQAndroidClient.analysisTlv150(t150: ByteArray) {
        this.t150 = Tlv(t150)
    }

    private fun QQAndroidClient.analysisTlv161(t161: ByteArray) {
        val tlv = t161.toReadPacket().apply { discardExact(2) }.readTLVMap()

        tlv[0x173]?.let { analysisTlv173(it) }
        tlv[0x17f]?.let { analysisTlv17f(it) }
        tlv[0x172]?.let { rollbackSig = it }
    }

    /**
     * 错误消息
     */
    private fun QQAndroidClient.analysisTlv149(t149: ByteArray) {
        data class ErrorMessage(
            val type: Short,
            val title: String,
            val content: String,
            val otherInfo: String
        )

        t149.read {
            val type: Short = readShort()
            val title: String = readUShortLVString()
            val content: String = readUShortLVString()
            val otherInfo: String = readUShortLVString()

            // do not write class into read{} block. CompilationException!!
            error("Got error message: " + ErrorMessage(type, title, content, otherInfo)) // nice toString
        }
    }

    /**
     * server host
     */
    private fun QQAndroidClient.analysisTlv173(t173: ByteArray) {
        t173.read {
            val type = readByte()
            val host = readUShortLVString()
            val port = readShort()

            bot.logger.warning("服务器: host=$host, port=$port, type=$type")
            // SEE oicq_request.java at method analysisT173
        }
    }

    /**
     * ipv6 address
     */
    private fun QQAndroidClient.analysisTlv17f(t17f: ByteArray) {
        t17f.read {
            val type = readByte()
            val host = readUShortLVString()
            val port = readShort()

            bot.logger.warning("服务器 ipv6: host=$host, port=$port, type=$type")
            // SEE oicq_request.java at method analysisT17f
        }
    }
}