package net.mamoe.mirai.qqandroid.network.protocol.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
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
            discardExact(4)
            return decodeUniPacket(PushReqJceStruct.serializer())
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqJceStruct, sequenceId: Int): OutgoingPacket? {
            return network.run {
                buildResponseUniPacket(
                    client,
                    sequenceId = client.configPushSvcPushReqSequenceId,
                    commandName = "ConfigPushSvc.PushResp",
                    name = "ConfigPushSvc.PushResp"
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