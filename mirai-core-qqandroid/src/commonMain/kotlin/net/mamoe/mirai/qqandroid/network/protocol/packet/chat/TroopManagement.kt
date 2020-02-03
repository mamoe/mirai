package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket

internal class TroopManagement {

    internal object Mute : OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("OidbSvc.0x570_8") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
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