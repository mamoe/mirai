/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin

import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.miscBitMap
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.login.WtLogin
import net.mamoe.mirai.internal.network.subAppId
import net.mamoe.mirai.internal.network.subSigMap


internal object WtLogin2 : WtLoginExt {
    fun SubmitSliderCaptcha(
        client: QQAndroidClient,
        ticket: String
    ) = WtLogin.Login.buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, EncryptMethodECDH(client.ecdh), 0x0810) {
                writeShort(2) // subCommand
                writeShort(4) // count of TLVs
                t193(ticket)
                t8(2052)
                t104(client.t104)
                t116(client.miscBitMap, client.subSigMap)
            }
        }
    }

    fun SubmitPictureCaptcha(
        client: QQAndroidClient,
        captchaSign: ByteArray,
        captchaAnswer: String
    ) = WtLogin.Login.buildLoginOutgoingPacket(client, bodyType = 2) { sequenceId ->
        writeSsoPacket(client, client.subAppId, WtLogin.Login.commandName, sequenceId = sequenceId) {
            writeOicqRequestPacket(client, EncryptMethodECDH(client.ecdh), 0x0810) {
                writeShort(2) // subCommand
                writeShort(4) // count of TLVs
                t2(captchaAnswer, captchaSign, 0)
                t8(2052)
                t104(client.t104)
                t116(client.miscBitMap, client.subSigMap)
            }
        }
    }
}
