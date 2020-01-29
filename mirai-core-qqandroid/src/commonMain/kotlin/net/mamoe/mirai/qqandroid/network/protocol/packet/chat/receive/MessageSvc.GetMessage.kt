package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.Cmd0x352Packet
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.MsgSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.RequestPushNotify

internal object GetMsgRequest : PacketFactory<MsgSvc.PbGetMsgResp>("MessageSvc.PbGetMsg") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): MsgSvc.PbGetMsgResp {
        println("received MsgSvc.PbGetMsgResp")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
    @Serializable
    class PbGetMsgReq(
    @SerialId(1) val syncFlag: Int /* enum */ = 0,
    @SerialId(2) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
    @SerialId(3) val rambleFlag: Int = 1,
    @SerialId(4) val latestRambleNumber: Int = 20,
    @SerialId(5) val otherRambleNumber: Int = 3,
    @SerialId(6) val onlineSyncFlag: Int = 1,
    @SerialId(7) val contextFlag: Int = 0,
    @SerialId(8) val whisperSessionId: Int = 0,
    @SerialId(9) val msgReqType: Int = 0,
    @SerialId(10) val pubaccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
    @SerialId(11) val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY,
    @SerialId(12) val serverBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf


    @Serializable
    internal class RequestPushNotify(
    @SerialId(0) val uin: Long = 0L,
    @SerialId(1) val ctype: Byte = 0,
    @SerialId(2) val strService: String?,
    @SerialId(3) val strCmd: String?,
    @SerialId(4) val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @SerialId(5) val usMsgType: Int?,
    @SerialId(6) val wUserActive: Int?,
    @SerialId(7) val wGeneralFlag: Int?,
    @SerialId(8) val bindedUin: Long?,
    @SerialId(9) val stMsgInfo: MsgInfo?,
    @SerialId(10) val msgCtrlBuf: String?,
    @SerialId(11) val serverBuf: ByteArray?,
    @SerialId(12) val pingFlag: Long?,
    @SerialId(13) val svrip: Int?
    ) : JceStruct, Packet
     */
    operator fun invoke(
        client: QQAndroidClient,
        notify: RequestPushNotify
    ): OutgoingPacket = buildOutgingPacket(client, key = client.wLoginSigInfo.d2Key) {
        val req = MsgSvc.PbGetMsgReq(
            serverBuf = notify.serverBuf ?: EMPTY_BYTE_ARRAY,
            msgReqType = notify.usMsgType ?: 0,
            syncFlag = 0,
            rambleFlag = 0,
            contextFlag = 1,
            latestRambleNumber = 20,
            otherRambleNumber = 3,
            onlineSyncFlag = 1
        )

        val data = ProtoBuf.dump(
            MsgSvc.PbGetMsgReq.serializer(),
            req
        )

        writeInt(data.size)
        writeFully(data, 0, data.size)
    }

}

