/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.login


import io.ktor.util.InternalAPI
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
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.md5

internal class WtLogin{
    /**
     * OicqRequest
     */
    @Suppress("FunctionName")
    @UseExperimental(ExperimentalUnsignedTypes::class, MiraiInternalAPI::class)
    internal object Login : OutgoingPacketFactory<Login.LoginPacketResponse>("wtlogin.login") {
        private const val subAppId = 537062845L

        /**
         * 提交验证码
         */
        object SubCommand2 {
            fun SubmitSliderCaptcha(
                client: QQAndroidClient,
                ticket: String
            ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
                writeSsoPacket(client, subAppId, commandName, sequenceId = sequenceId) {
                    writeOicqRequestPacket(client, EncryptMethodECDH7(client.ecdh), 0x0810) {
                        writeShort(2) // subCommand
                        writeShort(4) // count of TLVs
                        t193(ticket)
                        t8(2052)
                        t104(client.t104)
                        t116(150470524, 66560)
                    }
                }
            }

            fun SubmitPictureCaptcha(
                client: QQAndroidClient,
                captchaSign: ByteArray,
                captchaAnswer: String
            ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
                writeSsoPacket(client, subAppId, commandName, sequenceId = sequenceId) {
                    writeOicqRequestPacket(client, EncryptMethodECDH7(client.ecdh), 0x0810) {
                        writeShort(2) // subCommand
                        writeShort(4) // count of TLVs
                        t2(captchaAnswer, captchaSign, 0)
                        t8(2052)
                        t104(client.t104)
                        t116(150470524, 66560)
                    }
                }
            }
        }

        object SubCommand20 {

            operator fun invoke(
                client: QQAndroidClient,
                t402: ByteArray
            ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
                writeSsoPacket(client, subAppId, commandName, sequenceId = sequenceId) {
                    writeOicqRequestPacket(client, EncryptMethodECDH7(client.ecdh), 0x0810) {
                        writeShort(20) // subCommand
                        writeShort(4) // count of TLVs, probably ignored by server?
                        t8(2052)
                        t104(client.t104)
                        t116(150470524, 66560)
                        t401(md5(client.device.guid + "stMNokHgxZUGhsYp".toByteArray() + t402))
                    }
                }
            }
        }

        /**
         * 提交 SMS
         */
        object SubCommand7 {
            operator fun invoke(
                client: QQAndroidClient
            ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
                writeSsoPacket(client, subAppId, commandName, sequenceId = sequenceId, unknownHex = "01 00 00 00 00 00 00 00 00 00 01 00") {
                    writeOicqRequestPacket(client, EncryptMethodECDH7(client.ecdh), 0x0810) {
                        writeShort(8) // subCommand
                        writeShort(6) // count of TLVs, probably ignored by server?TODO
                        t8(2052)
                        t104(client.t104)
                        t116(150470524, 66560)
                        t174(EMPTY_BYTE_ARRAY)
                        t17a(9)
                        t197(byteArrayOf(0.toByte()))
                        //t401(md5(client.device.guid + "12 34567890123456".toByteArray() + t402))
                        //t19e(0)//==tlv408
                    }
                }
            }
        }

        /**
         * 密码登录
         */
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
                        t100(appId, subAppId, client.appClientVersion)
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
                            guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag(0)),
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
            object Success : LoginPacketResponse() {
                override fun toString(): String = "LoginPacketResponse.Success"
            }

            data class Error(
                val title: String,
                val message: String,
                val errorInfo: String
            ) : LoginPacketResponse()

            sealed class Captcha : LoginPacketResponse() {

                class Slider(
                    val url: String
                ) : Captcha() {
                    override fun toString(): String = "LoginPacketResponse.Captcha.Slider"
                }

                class Picture(
                    val data: IoBuffer,
                    val sign: ByteArray
                ) : Captcha() {
                    override fun toString(): String = "LoginPacketResponse.Captcha.Picture"
                }
            }

            data class UnsafeLogin(val url: String) : LoginPacketResponse()

            class SMSVerifyCodeNeeded(val t402: ByteArray, val t403: ByteArray) : LoginPacketResponse() {
                override fun toString(): String {
                    return "LoginPacketResponse.SMSVerifyCodeNeeded"
                }
            }

            class DeviceLockLogin(val t402: ByteArray) : LoginPacketResponse() {
                override fun toString(): String = "WtLogin.Login.LoginPacketResponse.DeviceLockLogin"
            }
        }

        @InternalAPI
        @UseExperimental(MiraiDebugAPI::class)
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacketResponse {

            discardExact(2) // subCommand
            // println("subCommand=$subCommand")
            val type = readUByte()
            // println("type=$type")

            discardExact(2)
            val tlvMap: TlvMap = this.readTLVMap()
            // tlvMap.printTLVMap()
            return when (type.toInt()) {
                0 -> onLoginSuccess(tlvMap, bot)
                1, 15 -> onErrorMessage(tlvMap)
                2 -> onSolveLoginCaptcha(tlvMap, bot)
                160 /*-96*/ -> onUnsafeDeviceLogin(tlvMap)
                204 /*-52*/ -> onSMSVerifyNeeded(tlvMap, bot)
                else -> tlvMap[0x149]?.let { analysisTlv149(it) } ?: error("unknown login result type: $type")
            }
        }


        private fun onSMSVerifyNeeded(
            tlvMap: TlvMap,
            bot: QQAndroidBot
        ): LoginPacketResponse.DeviceLockLogin {
            bot.client.t104 = tlvMap.getOrFail(0x104)
            // println("403： " + tlvMap[0x403]?.toUHexString())
            return LoginPacketResponse.DeviceLockLogin(tlvMap.getOrFail(0x402))
        }

        private fun onUnsafeDeviceLogin(tlvMap: TlvMap): LoginPacketResponse.UnsafeLogin {
            return LoginPacketResponse.UnsafeLogin(tlvMap.getOrFail(0x204).toReadPacket().readBytes().encodeToString())
        }

        private fun onErrorMessage(tlvMap: TlvMap): LoginPacketResponse.Error {
            return tlvMap[0x146]?.toReadPacket()?.run {
                readShort() // ver
                readShort() // code

                val title = readUShortLVString()
                val message = readUShortLVString()
                val errorInfo = readUShortLVString()

                LoginPacketResponse.Error(title, message, errorInfo)
            } ?: error("Cannot find error message")
        }

        @InternalAPI
        @UseExperimental(MiraiDebugAPI::class)
        private fun onSolveLoginCaptcha(tlvMap: TlvMap, bot: QQAndroidBot): LoginPacketResponse.Captcha {
            // val ret = tlvMap[0x104]?.let { println(it.toUHexString()) }
            tlvMap[0x192]?.let {
                bot.client.t104 = tlvMap.getOrFail(0x104)
                return LoginPacketResponse.Captcha.Slider(it.encodeToString())
            }
            tlvMap[0x165]?.let { question ->
                if (question[18].toInt() == 0x36) {
                    //图片验证
                    DebugLogger.debug("是一个图片验证码")
                    bot.client.t104 = tlvMap.getOrFail(0x104)
                    val imageData = tlvMap.getOrFail(0x105).toReadPacket()
                    val signInfoLength = imageData.readShort()
                    imageData.discardExact(2)//image Length
                    val sign = imageData.readBytes(signInfoLength.toInt())
                    return LoginPacketResponse.Captcha.Picture(
                        data = imageData.readBytes().toIoBuffer(),
                        sign = sign
                    )
                } else error("UNKNOWN CAPTCHA QUESTION: ${question.toUHexString()}")
            }

            error("UNKNOWN CAPTCHA")
        }

        @UseExperimental(MiraiDebugAPI::class)
        private fun onLoginSuccess(tlvMap: TlvMap, bot: QQAndroidBot): LoginPacketResponse.Success {
            val client = bot.client
            //println("TLV KEYS: " + tlvMap.keys.joinToString { it.contentToString() })

            tlvMap[0x150]?.let { client.analysisTlv150(it) }
            //  tlvMap[0x305]?.let { println("TLV 0x305=${it.toUHexString()}") }
            tlvMap[0x161]?.let { client.analysisTlv161(it) }
            tlvMap[0x119]?.let { t119Data ->
                t119Data.decryptBy(client.tgtgtKey).read {
                    discardExact(2) // always discarded.  00 1C
                    // 00 1C
                    // 01 08 00 10 A1 73 76 98 64 E0 38 C6 C8 18 73 FA D3 85 DA D6 01 6A 00 30 1D 99 4A 28 7E B3 B8 AC 74 B9 C4 BB 6D BB 41 72 F7 5C 9F 0F 79 8A 82 4F 1F 69 34 6D 10 D6 BB E8 A3 4A 2B 5D F1 C7 05 3C F8 72 EF CF 67 E4 3C 94 01 06 00 78 B4 ED 9F 44 ED 10 18 A8 85 0A 8A 85 79 45 47 7F 25 AA EE 2C 53 83 80 0A B3 B0 47 3E 95 51 A4 AE 3E CA A0 1D B4 91 F7 BB 2E 94 76 A8 C8 97 02 C4 5B 15 02 B7 03 9A FC C2 58 6D 17 92 46 AE EB 2F 6F 65 B8 69 6C D6 9D AC 18 6F 07 53 AC FE FA BC BD CE 57 13 10 2D 5A C6 50 AA C2 AE 18 D4 FD CD F2 E0 D1 25 29 56 21 35 8F 01 9D D6 69 44 8F 06 D0 23 26 D3 0E E6 E6 B7 01 0C 00 10 73 32 61 4E 2C 72 35 58 68 28 47 3E 2B 6E 52 62 01 0A 00 48 A4 DA 48 FB B4 8D DA 7B 86 D7 A7 FE 01 1B 70 6F 54 F8 55 38 B0 AD 1B 0C 0B B9 F6 94 24 F8 9E 30 32 22 99 0C 22 CD 44 B8 B0 8A A8 65 E1 B8 F0 49 EF E1 23 D7 0D A3 F1 BB 52 B7 4B AF BD 50 EA BF 15 02 78 2B 8B 10 FB 15 01 0D 00 10 29 75 38 72 21 5D 3F 24 37 46 67 79 2B 65 6D 34 01 14 00 60 00 01 5E 19 65 8C 00 58 93 DD 4D 2C 2D 01 44 99 62 B8 7A EF 04 C5 71 0B F1 BE 4C F4 21 F2 97 B0 14 67 0E 14 9F D8 A2 0B 93 40 90 80 F3 59 7A 69 45 D7 D4 53 4C 08 3A 56 1D C9 95 36 2C 7C 5E EE 36 47 5F AE 26 72 76 FD FD 69 E6 0C 2D 3A E8 CF D4 8D 76 C9 17 C3 E3 CD 21 AB 04 6B 70 C5 EC EC 01 0E 00 10 56 48 3E 29 3A 5A 21 74 55 6A 2C 72 58 73 79 71 01 03 00 30 9B A6 5D 85 5C 40 7C 28 E7 05 A9 25 CA F5 FC C0 51 40 85 F3 2F D2 37 F9 09 A6 E6 56 7F 7A 2E 7D 9F B9 1C 00 65 55 D2 A9 60 03 77 AB 6A F5 3F CE 01 33 00 30 F4 3A A7 08 E2 04 FA C8 9D 54 49 DE 63 EA F0 A5 1C C4 03 57 51 B6 AE 0B 55 41 F8 AB 22 F1 DC A3 B0 73 08 55 14 02 BF FF 55 87 42 4C 23 70 91 6A 01 34 00 10 61 C7 02 3F 1D BE A6 27 2F 24 D4 92 95 68 71 EF 05 28 00 1A 7B 22 51 49 4D 5F 69 6E 76 69 74 61 74 69 6F 6E 5F 62 69 74 22 3A 22 31 22 7D 03 22 00 10 CE 1E 2E DC 69 24 4F 9B FF 2F 52 D8 8F 69 DD 40 01 1D 00 76 5F 5E 10 E2 34 36 79 27 23 53 4D 65 6B 6A 33 6D 7D 4E 3C 5F 00 60 00 01 5E 19 65 8C 00 58 67 00 9C 02 E4 BC DB A3 93 98 A1 ED 4C 91 08 6F 0C 06 E0 12 6A DC 14 5B 4D 20 7C 82 83 AE 94 53 A2 4A A0 35 FF 59 9D F3 EF 82 42 61 67 2A 31 E7 87 7E 74 E7 A3 E7 5C A8 3C 87 CF 40 6A 9F E5 F7 20 4E 56 C6 4F 1C 98 3A 8B A9 4F 1D 10 35 C2 3B A1 08 7A 89 0B 25 0C 63 01 1F 00 0A 00 01 51 80 00 00 03 84 00 00 01 38 00 0E 00 00 00 01 01 0A 00 27 8D 00 00 00 00 00 01 1A 00 13 02 5B 06 01 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 05 22 00 14 00 00 00 00 76 E4 B8 DD AB 53 02 9F 5E 19 65 8C 20 02 ED BD 05 37 00 17 01 01 00 00 00 00 76 E4 B8 DD 04 AB 53 02 9F 5E 19 65 8C 20 02 ED BD 01 20 00 0A 4D 39 50 57 50 6E 4C 31 65 4F 01 6D 00 2C 31 7A 50 7A 63 72 70 4D 30 43 6E 31 37 4C 32 32 6E 77 2D 36 7A 4E 71 48 48 59 41 35 48 71 77 41 37 6D 76 4F 63 2D 4A 56 77 47 51 5F 05 12 03 5D 00 0E 00 0A 74 65 6E 70 61 79 2E 63 6F 6D 00 2C 6E 4A 72 55 55 74 63 2A 34 7A 32 76 31 66 6A 75 77 6F 6A 65 73 72 76 4F 68 70 66 45 76 4A 75 55 4B 6D 34 43 2D 76 74 38 4D 77 38 5F 00 00 00 11 6F 70 65 6E 6D 6F 62 69 6C 65 2E 71 71 2E 63 6F 6D 00 2C 78 59 35 65 62 4D 74 48 44 6D 30 53 6F 68 56 71 68 33 43 79 79 34 6F 63 65 4A 46 6A 51 58 65 68 30 44 61 75 55 30 6C 78 65 52 6B 5F 00 00 00 0B 64 6F 63 73 2E 71 71 2E 63 6F 6D 00 2C 64 6A 62 79 47 57 45 4F 34 58 34 6A 36 4A 73 48 45 65 6B 73 69 74 72 78 79 62 57 69 77 49 68 46 45 70 72 4A 59 4F 2D 6B 36 47 6F 5F 00 00 00 0E 63 6F 6E 6E 65 63 74 2E 71 71 2E 63 6F 6D 00 2C 64 4C 31 41 79 32 41 31 74 33 58 36 58 58 2A 74 33 64 4E 70 2A 31 61 2D 50 7A 65 57 67 48 70 2D 65 47 78 6B 59 74 71 62 69 6C 55 5F 00 00 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 2C 75 6A 55 5A 4F 6A 4F 48 52 61 75 6B 32 55 50 38 77 33 34 68 36 69 46 38 2A 77 4E 50 35 2D 66 54 75 37 67 39 56 67 44 57 2A 6B 6F 5F 00 00 00 0A 76 69 70 2E 71 71 2E 63 6F 6D 00 2C 37 47 31 44 6F 54 2D 4D 57 50 63 2D 62 43 46 68 63 62 32 56 38 6E 77 4A 75 41 51 63 54 39 77 45 49 62 57 43 4A 4B 44 4D 6C 6D 34 5F 00 00 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 2C 7A 73 70 5A 56 43 59 45 7A 35 2A 4F 6B 4E 68 6E 74 79 61 69 6E 6F 68 4D 32 6B 41 6C 2A 74 31 63 7A 48 57 77 30 41 6A 4B 50 4B 6B 5F 00 00 00 0B 67 61 6D 65 2E 71 71 2E 63 6F 6D 00 2C 32 6F 2D 51 53 36 65 43 70 37 6A 43 4E 34 6A 74 6E 47 4F 4B 33 67 73 32 63 4A 6F 56 71 58 65 44 48 61 55 39 65 34 2D 32 34 64 30 5F 00 00 00 0C 71 71 77 65 62 2E 71 71 2E 63 6F 6D 00 2C 63 54 4D 79 64 51 43 35 50 74 43 45 51 72 6F 33 53 54 41 66 7A 56 2D 44 76 46 56 35 58 6D 56 6B 49 31 68 4C 55 48 4E 65 76 56 38 5F 00 00 00 0D 6F 66 66 69 63 65 2E 71 71 2E 63 6F 6D 00 2C 6F 73 72 54 36 32 69 37 66 76 6D 49 50 64 6F 58 4B 48 74 38 58 52 59 56 77 72 7A 6E 69 31 58 7A 57 4C 77 2A 71 36 33 44 74 73 6F 5F 00 00 00 09 74 69 2E 71 71 2E 63 6F 6D 00 2C 41 61 77 4D 78 4D 32 79 58 51 47 75 72 75 55 6C 66 53 58 79 5A 57 48 53 78 52 57 58 50 74 6B 6B 4F 78 6F 66 4A 59 47 6C 71 68 34 5F 00 00 00 0B 6D 61 69 6C 2E 71 71 2E 63 6F 6D 00 2C 67 72 57 68 58 77 34 4C 6E 4B 49 4F 67 63 78 45 71 70 33 61 45 67 37 38 46 7A 77 4E 6D 4B 48 56 6E 6F 50 4C 4F 32 6D 57 6D 6E 38 5F 00 00 00 09 71 7A 6F 6E 65 2E 63 6F 6D 00 2C 72 61 47 79 51 35 54 72 4D 55 7A 6E 74 31 4E 52 44 2D 50 72 74 72 41 55 43 35 6A 61 2D 49 47 2D 73 77 4C 6D 49 51 51 41 44 4C 41 5F 00 00 00 0A 6D 6D 61 2E 71 71 2E 63 6F 6D 00 2C 39 73 2D 4F 51 30 67 76 39 42 6A 37 58 71 52 49 4E 30 35 46 32 64 4D 47 67 47 43 58 57 4A 62 68 63 30 38 63 7A 4B 52 76 6B 78 6B 5F 00 00 03 05 00 10 77 75 6E 54 5F 7E 66 7A 72 40 3C 6E 35 50 53 46 01 43 00 40 3A AE 30 87 81 3D EE BA 31 9C EA 9D 0D D4 73 B1 81 12 E0 94 71 73 7A B0 47 3D 09 47 E5 1B E1 E2 06 1A CB A4 E3 71 9E A6 EA 2A 73 5C C8 D3 B1 2A B1 C7 DA 04 A6 6D 12 26 DF 6B 8B EC C7 12 F8 E1 01 18 00 05 00 00 00 01 00 01 63 00 10 67 6B 60 23 24 6A 55 39 4E 58 24 5E 39 2B 7A 69 01 38 00 5E 00 00 00 09 01 06 00 27 8D 00 00 00 00 00 01 0A 00 24 EA 00 00 00 00 00 01 1C 00 1A 5E 00 00 00 00 00 01 02 00 01 51 80 00 00 00 00 01 03 00 00 1C 20 00 00 00 00 01 20 00 01 51 80 00 00 00 00 01 36 00 1B AF 80 00 00 00 00 01 43 00 1B AF 80 00 00 00 00 01 64 00 1B AF 80 00 00 00 00 01 30 00 0E 00 00 5E 19 65 8C 9F 02 53 AB 00 00 00 00
                    val tlvMap119 = this.readTLVMap()

                    // ???
                    tlvMap119[0x1c]?.read {
                        val bytes = readBytes()
                        DebugLogger.warning(bytes.toUHexString())
                        DebugLogger.warning(bytes.encodeToString())
                    }

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

            return LoginPacketResponse.Success
        }


        private fun TlvMap.getOrEmpty(key: Int): ByteArray {
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
         * 设置 [QQAndroidClient.uin]
         */
        private fun QQAndroidClient.analysisTlv113(t113: ByteArray) = t113.read {
            _uin = readUInt().toLong()

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
            timeDifference = readUInt().toLong() - currentTimeSeconds
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
        private fun analysisTlv149(t149: ByteArray): LoginPacketResponse.Error {

            return t149.read {
                discardExact(2) //type
                val title: String = readUShortLVString()
                val content: String = readUShortLVString()
                val otherInfo: String = readUShortLVString()

                // do not write class into read{} block. CompilationException!!
                LoginPacketResponse.Error(title = title, message = content, errorInfo = otherInfo) // nice toString
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
}