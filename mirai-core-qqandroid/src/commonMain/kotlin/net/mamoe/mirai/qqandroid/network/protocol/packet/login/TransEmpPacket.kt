package net.mamoe.mirai.qqandroid.network.protocol.packet.login


import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.*

internal object TransEmpPacket : PacketFactory<TransEmpPacket.Response>() {

    init {
        _id = PacketId(0x0812, "wtlogin.trans_emp")
    }

    private const val appId = 16L
    private const val subAppId = 537062845L

    @Suppress("FunctionName")
    fun SubCommand1(
        client: QQAndroidClient
    ): OutgoingPacket = buildLoginOutgoingPacket(client, bodyType = 2) {
        writeOicqRequestPacket(client, EncryptMethodECDH135(client.ecdh), id) {

            // oicq.wlogin_sdk.request.trans_emp_1#packTransEmpBody
        }
    }

    object Response : Packet


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        TODO("not implemented")
    }
}