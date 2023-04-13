/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.internal.utils.GuidSource
import net.mamoe.mirai.internal.utils.MacOrAndroidIdChangeFlag
import net.mamoe.mirai.internal.utils.guidFlag
import net.mamoe.mirai.utils._writeTlvMap
import net.mamoe.mirai.utils.generateDeviceInfoData

internal object WtLogin24 : WtLoginExt {
    private val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
        isSmsLogin: Boolean,
    ): OutgoingPacketWithRespType<WtLogin.Login.LoginPacketResponse> {
        return WtLogin.Login.buildLoginOutgoingPacket(
            client,
            bodyType = 2,
            remark = "24:get-salt-uin-list",
        ) { sequenceId ->
            writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
                writeOicqRequestPacket(client, uin = 0, commandId = 0x0810) {
                    writeShort(24)
                    _writeTlvMap {
                        t8()
                        t18(appId, client.appClientVersion, 0, 0)
                        t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)

                        t104(client.t104)

                        t108(client.ksid)
                        t109(client.device.androidId)
                        t116(client.miscBitMap, client.subSigMap)
                        tlv(0x11b) { writeByte(2) }
                        t124(
                            osType = client.device.osType,
                            osVersion = client.device.version.release,
                            networkType = client.networkType,
                            simInfo = client.device.simInfo,
                            address = byteArrayOf(),
                            apn = client.device.apn,
                        )
                        t128(
                            isGuidFromFileNull = false,
                            isGuidAvailable = true,
                            isGuidChanged = false,
                            guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag(0)),
                            buildModel = client.device.model,
                            guid = client.device.guid,
                            buildBrand = client.device.brand,
                        )

                        t142(client.apkId)
                        t145(client.device.guid)
                        t147(appId, client.apkVersionName, client.apkSignatureMd5)

                        t154(sequenceId)
                        t16e(client.device.model)

                        client.rollbackSig?.let { t172(it) }

                        t177(client.buildTime, client.sdkVersion)
                        t202(client.device.wifiBSSID, client.device.wifiSSID)

                        t400(
                            g = client.G,
                            uin = 0,
                            guid = client.device.guid,
                            dpwd = client.dpwd,
                            appId = 1,
                            subAppId = 16,
                            randomSeed = client.randSeed
                        )

                        t521()
                        t525(client.loginExtraData)
                        t52d(client.device.generateDeviceInfoData())
                        t544() // TODO
                        // ignored cuz qiemiListener is null
                        // tlv(0x545) {  }
                        //tlv(0x548) { writeFully(powTestData) }
                    }
                }
            }
        }
    }
}