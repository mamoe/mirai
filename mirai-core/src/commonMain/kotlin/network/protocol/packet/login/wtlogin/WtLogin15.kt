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
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArray

internal object WtLogin15 : WtLoginExt {
    private const val subCommand = 15.toShort()

    private const val appId = 16L

    operator fun invoke(
        client: QQAndroidClient,
    ) = WtLogin.ExchangeEmp.buildOutgoingUniPacket(client, bodyType = 2, key = ByteArray(16)) { sequenceId ->
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
            writeShort(21) // doesn't matter

            t18(16, uin = client.uin)
            t1(client.uin, client.device.ipAddress)

            //  t106(client = client)
            writeShort(0x106)
            val encryptA1 = client.wLoginSigInfo.encryptA1!!
//            kotlin.run {
//                val key = (client.account.passwordMd5 + ByteArray(4) + client.uin.toInt().toByteArray()).md5()
//                kotlin.runCatching {
//                    TEA.decrypt(encryptA1, key).toUHexString()
//                }.soutv("DEC") // success
//            }

            writeShortLVByteArray(encryptA1)
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

            //t116(client.miscBitMap, client.subSigMap)
            t116(client.miscBitMap, client.subSigMap)

            //t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, client.mainSigMap)
            t100(appId, client.subAppId, client.appClientVersion, client.ssoVersion, mainSigMap = client.mainSigMap)

            t107(0)
            // t108(client.ksid) // new
            t144(client)
            t142(client.apkId)
            t145(client.device.guid)

            val noPicSig =
                client.wLoginSigInfo.noPicSig ?: error("Internal error: doing exchange emp 15 while noPicSig=null")
            t16a(noPicSig)

            t154(sequenceId)
            t141(client.device.simInfo, client.networkType, client.device.apn)
            t8(2052)
            t511()
            t147(appId, client.apkVersionName, client.apkSignatureMd5)
            t177(buildTime = client.buildTime, buildVersion = client.sdkVersion)

            // new
            t400(client.G, client.uin, client.device.guid, client.dpwd, appId, client.subAppId, client.randSeed)

            t187(client.device.macAddress)
            t188(client.device.androidId)
            t194(client.device.imsiMd5)
            t202(client.device.wifiBSSID, client.device.wifiSSID)
            t516()

            t521() // new
            t525() // new
            t544() // new
        }
        //  }
    }
}