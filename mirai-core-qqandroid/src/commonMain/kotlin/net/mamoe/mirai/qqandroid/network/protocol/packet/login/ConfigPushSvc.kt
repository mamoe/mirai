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
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.debugPrintThis
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.PushReq as PushReqJceStruct


internal class ConfigPushSvc {
    object PushReq : PacketFactory<PushReqJceStruct>("ConfigPushSvc.PushReq") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): PushReqJceStruct {
            discardExact(4)
            val pushReq = decodeUniPacket(PushReqJceStruct.serializer())
            println(pushReq.contentToString())
            return pushReq
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqJceStruct) {
            network.run {
                buildOutgoingUniPacket(
                    client,
                    sequenceId = client.configPushSvcPushReqSequenceId.also { println("configPushSvcPushReqSequenceId=${client.configPushSvcPushReqSequenceId}") },
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
                    writePacket(this.build().debugPrintThis())
                }.sendWithoutExpect()
            }
        }
    }
}