/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.utils._writeTlvMap
import net.mamoe.mirai.utils.currentTimeSeconds
import net.mamoe.mirai.utils.toByteArray

internal object WtLogin9 : WtLoginExt {
    private const val appId = 16L

    fun Password(
        client: QQAndroidClient,
        passwordMd5: ByteArray,
        allowSlider: Boolean
    ) = WtLogin.Login.buildLoginOutgoingPacket(
        client, encryptMethod = PacketEncryptType.Empty, remark = "9:password-login"
    ) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, commandId = 0x0810) {
                writeShort(9) // subCommand
                val useEncryptA1AndNoPicSig =
                    client.wLoginSigInfoInitialized
                            && client.wLoginSigInfo.noPicSig != null
                            && client.wLoginSigInfo.encryptA1 != null
                //writeShort(LoginType.PASSWORD.value.toShort())

                _writeTlvMap {

                    t18(appId, client.appClientVersion, client.uin)
                    t1(client.uin, (currentTimeSeconds() + client.timeDifference).toInt(), client.device.ipAddress)

                    if (useEncryptA1AndNoPicSig) {
                        t106(client.wLoginSigInfo.encryptA1!!)
                    } else {
                        t106(client, appId, passwordMd5)
                    }

                    /* // from GetStWithPasswd
                int mMiscBitmap = this.mMiscBitmap;
                if (t.uinDeviceToken) {
                    mMiscBitmap = (this.mMiscBitmap | 0x2000000);
                }


                // defaults true
                if (ConfigManager.get_loginWithPicSt()) appIdList = longArrayOf(1600000226L)
                */
                    t116(client.miscBitMap, client.subSigMap)
                    t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)
                    t107(0)
                    if (client.ksid.isNotEmpty()) {
                        t108(client.ksid)
                    }

                    // t108(byteArrayOf())
                    if (client.t104Initialized) {
                        t104(client.t104)
                    }

                    t142(client.apkId)

                    // if login with non-number uin
                    if (client.uin !in 10000L..4000000000L) {
                        t112(client.uin.toByteArray())
                    }
                    t144(client)

                    //this.build().debugPrint("傻逼")
                    t145(client.device.guid)
                    t147(appId, client.apkVersionName, client.apkSignatureMd5)


                    if (client.miscBitMap and 0x80 != 0) {
                        t166(1) // com.tencent.luggage.wxa.me.e.CTRL_INDEX
                    }
                    if (useEncryptA1AndNoPicSig) {
                        t16a(client.wLoginSigInfo.noPicSig!!)
                    }

                    t154(sequenceId)
                    t141(client.device.simInfo, client.networkType, client.device.apn)
                    t8(2052)

                    t511()

                    // ignored t172 because rollbackSig is null
                    // ignored t185 because loginType is not SMS
                    if (useEncryptA1AndNoPicSig) {
                        t400(
                            g = client.G,
                            uin = client.uin,
                            guid = client.device.guid,
                            dpwd = client.dpwd,
                            appId = appId,
                            subAppId = client.subAppId,
                            randomSeed = client.randSeed,
                        )
                    }

                    t187(client.device.macAddress)
                    t188(client.device.androidId)
                    t194(client.device.imsiMd5)
                    if (allowSlider) {
                        t191()
                    }


                    //t201(N = byteArrayOf())

                    t202(client.device.wifiBSSID, client.device.wifiSSID)

                    t177(
                        buildTime = client.buildTime,
                        buildVersion = client.sdkVersion,
                    )
                    t516()
                    t521()

                    t525()
                    t545(client.qimei16 ?: client.device.imei)
                    // t548()
                    // this.build().debugPrint("傻逼")

                    // ignored t318 because not logging in by QR
                    if (client.supportedEncrypt) {
                        t544ForToken(
                            client = client,
                            uin = client.uin,
                            protocol = client.bot.configuration.protocol,
                            guid = client.device.guid,
                            sdkVersion = client.sdkVersion,
                            subCommandId = 9,
                            commandStr = "810_9"
                        )
                    }
                }
            }
        }
    }

    @Suppress("DuplicatedCode")
    fun QRCode(
        client: QQAndroidClient,
        data: QRCodeLoginData,
    ) = WtLogin.Login.buildLoginOutgoingPacket(
        client, encryptMethod = PacketEncryptType.Empty, remark = "9:qrcode-login"
    ) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, commandId = 0x0810) {
                writeShort(9) // subCommand
//                writeShort(0x19) // count of TLVs, probably ignored by server?

                _writeTlvMap {
                    t18(appId, client.appClientVersion, client.uin)
                    t1(client.uin, (currentTimeSeconds() + client.timeDifference).toInt(), client.device.ipAddress)

                    t106(data.tmpPwd)

                    t116(client.miscBitMap, client.subSigMap)
                    t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)
                    t107(0)
                    t108(client.device.imei.toByteArray())

                    t142(client.apkId)

                    t144(client)

                    t145(client.device.guid)
                    t147(appId, client.apkVersionName, client.apkSignatureMd5)

                    t16a(data.noPicSig)

                    t154(sequenceId)
                    t141(client.device.simInfo, client.networkType, client.device.apn)
                    t8(2052)

                    t511()

                    t187(client.device.macAddress)
                    t188(client.device.androidId)
                    t194(client.device.imsiMd5)
                    t191(0x00)

                    t202(client.device.wifiBSSID, client.device.wifiSSID)

                    t177(client.buildTime, client.sdkVersion)
                    t516()
                    t521(8)
                    t318(data.tgtQR)

                    if (client.supportedEncrypt) {
                        t544ForToken(
                            client = client,
                            uin = client.uin,
                            protocol = client.bot.configuration.protocol,
                            guid = client.device.guid,
                            sdkVersion = client.sdkVersion,
                            subCommandId = 9,
                            commandStr = "810_9"
                        )
                    }
                }
            }
        }
    }
}
