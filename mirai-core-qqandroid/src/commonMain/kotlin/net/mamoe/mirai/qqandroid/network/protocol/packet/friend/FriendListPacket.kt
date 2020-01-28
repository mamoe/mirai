package net.mamoe.mirai.qqandroid.network.protocol.packet.friend

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory


internal object FriendListPacket :
    PacketFactory<FriendListPacket.GetFriendListResponse>("friendlist.GetFriendListReq") {

    class GetFriendListResponse() : Packet


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GetFriendListResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

