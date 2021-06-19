/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin

internal object WtLogin9 : WtLoginExt {
    private const val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
        allowSlider: Boolean
    ) = WtLogin.Login.buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, EncryptMethodECDH(client.ecdh), 0x0810) {
                writeShort(9) // subCommand
                writeShort(if (allowSlider) 0x18 else 0x17) // count of TLVs, probably ignored by server?
                //writeShort(LoginType.PASSWORD.value.toShort())

                t18(appId, client.appClientVersion, client.uin)
                t1(client.uin, client.device.ipAddress)

                t106(appId, client)

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
                t108(client.device.imei.toByteArray())

                // t108(byteArrayOf())
                // ignored: t104()
                t142(client.apkId)

                // if login with non-number uin
                // t112()
                t144(client)

                //this.build().debugPrint("傻逼")
                t145(client.device.guid)
                t147(appId, client.apkVersionName, client.apkSignatureMd5)

                /*
                if (client.miscBitMap and 0x80 != 0) {
                    t166(1)
                }
                */

                // ignored t16a because array5 is null

                t154(sequenceId)
                t141(client.device.simInfo, client.networkType, client.device.apn)
                t8(2052)

                t511()

                // ignored t172 because rollbackSig is null
                // ignored t185 because loginType is not SMS
                // ignored t400 because of first login

                t187(client.device.macAddress)
                t188(client.device.androidId)
                t194(client.device.imsiMd5)
                if (allowSlider) {
                    t191()
                }

                /*
                t201(N = byteArrayOf())*/

                t202(client.device.wifiBSSID, client.device.wifiSSID)

                t177(
                    buildTime = client.buildTime,
                    buildVersion = client.sdkVersion,
                )
                t516()
                t521()

                t525()
                // this.build().debugPrint("傻逼")

                // ignored t318 because not logging in by QR
            }
        }
    }
}
