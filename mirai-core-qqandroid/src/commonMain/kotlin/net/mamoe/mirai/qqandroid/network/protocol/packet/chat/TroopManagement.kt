package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.*
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.utils.io.debugPrintThis
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement.Mute as Mute

internal class TroopManagement {

    internal object Mute : OutgoingPacketFactory<Mute.Response>("OidbSvc.0x570_8") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Mute.Response {
            this.debugPrintThis()
            return Response()
        }

        operator fun invoke(
            client: QQAndroidClient,
            member: Member,
            timeInSecond: Int
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 1392,
                        serviceType = 8,
                        result = 0,
                        bodybuffer = buildPacket {
                            writeInt(member.group.id.toInt())//id or UIN?
                            writeByte(32)
                            writeShort(1)
                            writeInt(member.id.toInt())
                            writeInt(timeInSecond)
                        }.readBytes()
                    )
                )
            }
        }

        fun unmute(
            client: QQAndroidClient,
            member: Member
        ): OutgoingPacket {
            return invoke(client, member, 0)
        }


        internal class Response() : Packet
    }


    internal object MuteAll : OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("OidbSvc.0x89a_0") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    internal object EditNametag : OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("OidbSvc.0x8fc_2") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    /*
    internal object Recall: OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("wtlogin.login"){
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
     */

}