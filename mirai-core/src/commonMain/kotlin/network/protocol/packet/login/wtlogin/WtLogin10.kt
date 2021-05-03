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
import net.mamoe.mirai.internal.utils.GuidSource
import net.mamoe.mirai.internal.utils.MacOrAndroidIdChangeFlag
import net.mamoe.mirai.internal.utils.guidFlag
import net.mamoe.mirai.utils.generateDeviceInfoData
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.toReadPacket

internal object WtLogin10 : WtLoginExt {

    const val appId: Long = 16L
    operator fun invoke(
        client: QQAndroidClient,
        subAppId: Long = 100,
        mainSigMap: Int = client.mainSigMap
    ) = WtLogin.ExchangeEmp.buildLoginOutgoingPacket(client, bodyType = 2, key = ByteArray(16)) { sequenceId ->
        writeSsoPacket(
            client,
            client.subAppId,
            WtLogin.ExchangeEmp.commandName,
            extraData = client.wLoginSigInfo.tgt.toReadPacket(),
            sequenceId = sequenceId
        ) {
            writeOicqRequestPacket(
                client,
                EncryptMethodECDH(client.ecdh),
                0x0810
            ) {
                writeShort(11) // subCommand
                writeShort(17)
                t100(appId, subAppId, client.appClientVersion, client.ssoVersion, mainSigMap)
                t10a(client.wLoginSigInfo.tgt)
                t116(client.miscBitMap, client.subSigMap)
                t108(client.ksid)
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
                    tgtgtKey = client.wLoginSigInfo.d2Key.md5()
                )
                //t112(client.account.phoneNumber.encodeToByteArray())
                t143(client.wLoginSigInfo.d2.data)
                t142(client.apkId)
                t154(sequenceId)
                t18(appId, uin = client.uin)
                t141(client.device.simInfo, client.networkType, client.device.apn)
                t8(2052)
                //t511()
                t147(appId, client.apkVersionName, client.apkSignatureMd5)
                t177(client.buildTime, client.sdkVersion)
                t187(client.device.macAddress)
                t188(client.device.androidId)
                t194(client.device.imsiMd5)
                t511()
                //t544()

            }
        }
    }
}