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
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.miscBitMap
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.subAppId
import net.mamoe.mirai.internal.network.subSigMap
import net.mamoe.mirai.utils.DeviceVerificationRequests

/**
 * Request SMS.
 * @see DeviceVerificationRequests.SmsRequest.requestSms
 */
internal object WtLogin8 : WtLoginExt {
    val subCommand: Short = 8
    operator fun invoke(
        client: QQAndroidClient,
        t174: ByteArray
    ) = WtLogin.Login.buildLoginOutgoingPacket(
        client, bodyType = 2, remark = "8:request-sms"
    ) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, commandId = 0x0810) {
                writeShort(subCommand) // subCommand
                writeShort(6) // count of TLVs

                t8(2052)
                t104(client.t104)
                t116(client.miscBitMap, client.subSigMap)
                t174(t174)
                t17a(9)
                t197()
            }
        }
    }
}
