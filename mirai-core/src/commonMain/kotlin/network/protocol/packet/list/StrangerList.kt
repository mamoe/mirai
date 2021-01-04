package network.protocol.packet.list;

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.Stranger
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.StrangerRelationChangeEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x5d2
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x5d4
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal class StrangerList {
    object GetStrangerList : OutgoingPacketFactory<GetStrangerList.Response>("OidbSvc.0x5d2_0") {

        class Response(val result: Int, val strangerList: List<Oidb0x5d2.FriendEntry>) : Packet

        operator fun invoke(
            client: QQAndroidClient,
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 1490,
                        serviceType = 0,
                        bodybuffer = Oidb0x5d2.ReqBody(
                            subCmd = 1,
                            reqGetList = Oidb0x5d2.ReqGetList(
                                seq = client.strangerSeq
                            )
                        ).toByteArray(Oidb0x5d2.ReqBody.serializer())
                    )
                )
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            readProtoBuf(OidbSso.OIDBSSOPkg.serializer()).let { pkg ->
                if (pkg.result == 0) {
                    pkg.bodybuffer.loadAs(Oidb0x5d2.RspBody.serializer()).rspGetList!!.let {
                        bot.client.strangerSeq = it.seq
                        return Response(pkg.result, it.list)
                    }
                }
                return Response(pkg.result, emptyList())
            }
        }

    }

    object DelStranger : OutgoingPacketFactory<DelStranger.Response>("OidbSvc.0x5d4_0") {
        class Response(val result: Int) : Packet {
            val isSuccess = result == 0
            override fun toString(): String = "DelStranger.Response(success=${result == 0})"
        }

        operator fun invoke(
            client: QQAndroidClient,
            stranger: Stranger
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 1492,
                        serviceType = 0,
                        result = 0,
                        bodybuffer = Oidb0x5d4.ReqBody(
                            uinList = listOf(stranger.id)
                        ).toByteArray(Oidb0x5d4.ReqBody.serializer())
                    )
                )
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            readProtoBuf(OidbSso.OIDBSSOPkg.serializer()).let { pkg ->
                if (pkg.result == 0) {
                    pkg.bodybuffer.loadAs(Oidb0x5d4.RspBody.serializer()).result.forEach { delResult ->
                        bot.getStranger(delResult.uin)?.let {
                            bot.strangers.remove(delResult.uin)
                            StrangerRelationChangeEvent.Deleted(it).broadcast()
                        }
                    }
                }
                return Response(pkg.result)
            }
        }
    }


}
