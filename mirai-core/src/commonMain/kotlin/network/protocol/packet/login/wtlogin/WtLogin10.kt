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
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin

internal object WtLogin10 : WtLoginExt {
    private const val subCommand = 10.toShort()

    private const val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
    ) = WtLogin.ExchangeEmp.buildOutgoingUniPacket(client, bodyType = 2, key = KEY_16_ZEROS) { sequenceId ->
        writeOicqRequestPacket(
            client,
            EncryptMethodSessionKeyNew(
                client.wLoginSigInfo.wtSessionTicket.data,
                client.wLoginSigInfo.wtSessionTicketKey
            ),
            0x0810
        ) {
            writeShort(subCommand)
            writeShort(17)

            t100(appId, 2, client.appClientVersion, client.ssoVersion, client.mainSigMap)
            t10a(client.wLoginSigInfo.tgt)//
            t116(client.miscBitMap, client.subSigMap)
            t108(client.ksid)
            t144(client)
            t143(client.wLoginSigInfo.d2.data)//
            t142(client.apkId)
            t154(sequenceId)
            t18(appId, uin = client.uin)
            t141(client.device.simInfo, client.networkType, client.device.apn)
            t8()
            t147(appId, client.apkVersionName, client.apkSignatureMd5)
            t177(buildTime = client.buildTime, buildVersion = client.sdkVersion)
            t187(client.device.macAddress)
            t188(client.device.androidId)
            t194(client.device.imsiMd5)
            t202(client.device.wifiBSSID, client.device.wifiSSID)
            // t544()

            // code=15 你的用户身份已失效，为保证帐号安全，请你重新登录。 t10a tgt 内容有误
            // 0x9 服务连接中，请稍后再试。
            // 0x6 缺144/缺10a (缺tlv)
        }
    }
}