/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.login

import io.ktor.utils.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.JceCharset
import net.mamoe.mirai.qqandroid.io.serialization.decodeUniPacket
import net.mamoe.mirai.qqandroid.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.PushResp
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.PushReq as PushReqJceStruct


internal class ConfigPushSvc {
    object PushReq : IncomingPacketFactory<PushReqJceStruct>(
        receivingCommandName = "ConfigPushSvc.PushReq",
        responseCommandName = "ConfigPushSvc.PushResp"
    ) {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): PushReqJceStruct {
            return decodeUniPacket(PushReqJceStruct.serializer())
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqJceStruct, sequenceId: Int): OutgoingPacket? {
            return network.run {
                buildResponseUniPacket(
                    client,
                    sequenceId = sequenceId
                ) {
                    writeJceStruct(
                        RequestPacket.serializer(),
                        RequestPacket(
                            iRequestId = 0,
                            iVersion = 3,
                            sServantName = "QQService.ConfigPushSvc.MainServant",
                            sFuncName = "PushResp",
                            sBuffer = jceRequestSBuffer(
                                "PushResp",
                                PushResp.serializer(),
                                PushResp(
                                    type = packet.type,
                                    seq = packet.seq,
                                    jcebuf = if (packet.type == 3) packet.jcebuf else null
                                )
                            )
                        ),
                        charset = JceCharset.UTF8
                    )
                    // writePacket(this.build().debugPrintThis())
                }
            }
        }
    }
}