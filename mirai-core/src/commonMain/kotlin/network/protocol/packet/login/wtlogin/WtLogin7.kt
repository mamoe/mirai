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
import net.mamoe.mirai.utils.DeviceVerificationRequests
import net.mamoe.mirai.utils._writeTlvMap

/**
 * Submit SMS.
 * @see DeviceVerificationRequests.SmsRequest.requestSms
 */
internal object WtLogin7 : WtLoginExt {
    operator fun invoke(
        client: QQAndroidClient,
        t174: ByteArray,
        code: String
    ) = WtLogin.Login.buildLoginOutgoingPacket(
        client, encryptMethod = PacketEncryptType.Empty, remark = "7:submit-sms"
    ) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, commandId = 0x0810) {
                writeShort(7) // subCommand

                _writeTlvMap {

                    t8(2052)
                    t104(client.t104)
                    t116(client.miscBitMap, client.subSigMap)
                    t174(client.t174 ?: t174)
                    t17c(code.encodeToByteArray())
                    t401(client.G)
                    t198()
                    if (client.supportedEncrypt) {
                        t544ForVerify(
                            client = client,
                            uin = client.uin,
                            protocol = client.bot.configuration.protocol,
                            guid = client.device.guid,
                            sdkVersion = client.sdkVersion,
                            subCommandId = 7,
                            commandStr = "810_7"
                        )
                    }
                }
            }
        }
    }
}
