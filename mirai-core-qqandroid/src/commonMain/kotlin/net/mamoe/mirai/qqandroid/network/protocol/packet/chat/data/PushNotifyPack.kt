package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data

import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.network.io.JceInput
import net.mamoe.mirai.qqandroid.network.io.JceOutput
import net.mamoe.mirai.qqandroid.network.io.JceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

class RequestPushNotify(
    val uin: Long,
    val ctype: Byte,
    val strService: String,
    val strCmd: String,
    val vNotifyCookie: ByteArray,
    val usMsgType: Int,
    val wUserActive: Int,
    val wGeneralFlag: Int,
    val bindedUin: Long,
    val stMsgInfo: MsgInfo,
    val msgCtrlBuf: String,
    val serverBuf: ByteArray,
    val pingFlag: Long,
    val svrip: Int
) : Packet, JceStruct() {
    override fun writeTo(builder: JceOutput) {
        //not needed
    }

    companion object : Factory<RequestPushNotify> {
        override fun newInstanceFrom(input: JceInput): RequestPushNotify {
            return RequestPushNotify(
                input.read(0L, 0),
                input.read(0.toByte(), 1),
                input.readString(2),
                input.readString(3),
                input.read(EMPTY_BYTE_ARRAY, 4),
                input.read(0, 5),
                input.read(0, 6),
                input.read(0, 7),
                input.read(0L, 8),
                input.readJceStruct(MsgInfo, 9),
                input.readString(10),
                input.readByteArray(11),
                input.readLong(12),
                input.readInt(13)
            )
        }
    }

}

class MsgInfo(
    val lFromUin: Long,
    val uMsgTime: Long,
    val shMsgType: Short,
    val shMsgSeq: Short,
    val strMsg: String,
    val uRealMsgTime: Int,
    val vMsg: ByteArray,
    val uAppShareID: Long,
    val vMsgCookies: ByteArray,
    val vAppShareCookie: ByteArray,
    val lMsgUid: Long,
    val lLastChangeTime: Long,
    val vCPicInfo: List<CPicInfo>,
    val stShareData: ShareData,
    val lFromInstId: Long,
    val vRemarkOfSender: ByteArray,
    val strFromMobile: String,
    val strFromName: String,
    val vNickName: List<String>,
    val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct() {
    companion object : Factory<MsgInfo> {
        override fun newInstanceFrom(input: JceInput): MsgInfo = with(input) {
            return MsgInfo(
                readLong(0),
                readLong(1),
                readShort(2),
                readShort(3),
                readString(4),
                readInt(5),
                readByteArray(6),
                readLong(7),
                readByteArray(8),
                readByteArray(9),
                readLong(10),
                readLong(11),
                readJceStructList(CPicInfo, 12),
                readJceStruct(ShareData, 13),
                readLong(14),
                readByteArray(15),
                readString(16),
                readString(17),
                readList(18),
                readJceStructOrNull(TempMsgHead, 19)
            )
        }

    }

    override fun writeTo(builder: JceOutput) {
        // not needed
    }
}

class ShareData(
    val pkgname: String = "",
    val msgtail: String = "",
    val picurl: String = "",
    val url: String = ""
) : JceStruct() {
    companion object : Factory<ShareData> {
        override fun newInstanceFrom(input: JceInput): ShareData {
            return ShareData(
                input.readString(0),
                input.readString(1),
                input.readString(2),
                input.readString(3)
            )
        }
    }

    override fun writeTo(builder: JceOutput) {
        // not needed
    }
}

class TempMsgHead(
    val c2c_type: Int,
    val serviceType: Int
) : JceStruct() {
    override fun writeTo(builder: JceOutput) {

    }

    companion object : Factory<TempMsgHead> {
        override fun newInstanceFrom(input: JceInput): TempMsgHead {
            return TempMsgHead(
                input.readInt(0),
                input.readInt(1)
            )
        }
    }
}

class CPicInfo(
    val vPath: ByteArray,
    val vHost: ByteArray?
) : JceStruct() {
    override fun writeTo(builder: JceOutput) {

    }

    companion object : Factory<CPicInfo> {
        override fun newInstanceFrom(input: JceInput): CPicInfo {
            return CPicInfo(
                input.readByteArray(0),
                input.readByteArray(1)
            )
        }
    }

}
