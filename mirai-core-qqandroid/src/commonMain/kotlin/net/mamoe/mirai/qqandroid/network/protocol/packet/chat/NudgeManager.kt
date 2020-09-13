package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0xed3
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

class NudgeManager {
    internal object Nudge : OutgoingPacketFactory<Nudge.Response>("OidbSvc.0xed3") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            with(readBytes().loadAs(OidbSso.OIDBSSOPkg.serializer())) {
                return Response(result == 0, result);
            }
        }

        class Response(val success: Boolean, val code: Int) : Packet {
            override fun toString(): String = "NudgeResponse(success=$success,code=$code)"
        }

        fun friendInvoke(
            client: QQAndroidClient,
            targetUin: Long,
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 3795,
                        serviceType = 1,
                        result = 0,
                        bodybuffer = Cmd0xed3.ReqBody(
                            toUin = targetUin,
                            aioUin = targetUin
                        ).toByteArray(Cmd0xed3.ReqBody.serializer())
                    )
                )
            }
        }

        fun troopInvoke(
            client: QQAndroidClient,
            groupCode: Long,
            targetUin: Long,
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 3795,
                        serviceType = 1,
                        result = 0,
                        bodybuffer = Cmd0xed3.ReqBody(
                            toUin = targetUin,
                            groupCode = groupCode
                        ).toByteArray(Cmd0xed3.ReqBody.serializer())
                    )
                )
            }
        }

    }
}