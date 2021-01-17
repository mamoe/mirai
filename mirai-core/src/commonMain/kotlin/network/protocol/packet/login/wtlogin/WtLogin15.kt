/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.guid
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin

internal object WtLogin15 : WtLoginExt {
    private const val subCommand = 15.toShort()

    private const val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
    ) = WtLogin.ExchangeEmp.buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.ExchangeEmp.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, EncryptMethodECDH(client.ecdh), 0x0810) {
                writeShort(subCommand) // subCommand
                writeShort(21)

                t18(16, uin = client.uin)
                t1(client.uin, client.device.ipAddress)
                t106(appId, client)
                t116(client.miscBitMap, client.subSigMap)
                t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)
                t107(0)
                t142(client.apkId)
                t144(client)
                t145(client.device.guid)
                t16a(client.tlv16a ?: byteArrayOf()) // new
                t154(sequenceId)
                t141(client.device.simInfo, client.networkType, client.device.apn)
                t8(2052)
                t511()
                t147(appId, client.apkVersionName, client.apkSignatureMd5)
                t177(buildTime = client.buildTime, buildVersion = client.sdkVersion)
                t187(client.device.macAddress)
                t188(client.device.androidId)
                t194(client.device.imsiMd5)
                t202(client.device.wifiBSSID, client.device.wifiSSID)
                t516()
            }
        }
    }
}