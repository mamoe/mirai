package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.io.*
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.io.toUHexString

internal object PushNotify : PacketFactory<PushNotify.MessageNotification>("MessageSvc.PushNotify") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): MessageNotification {
        val request = RequestPacket.newInstanceFrom(this.apply { discardExact(4) }.asJceInput(CharsetUTF8))
        return MessageNotification.newInstanceFrom(request.sBuffer.asJceInput(CharsetUTF8))
    }


    class MessageNotification(
        val luni: Long,
        val ctype: Byte,
        val strService: String,
        val strCmd: String,
        val vNotifyCookie: ByteArray,
        val usMsgType: Int,
        val wUserActive: Int,
        val wGeneralFlag: Int,
        val lBindedUni: Long
    ) : Packet, JceStruct() {
        init {
            println(this.luni)
            println(this.ctype)
            println(this.strService)
            println(this.strCmd)
            println(this.vNotifyCookie.toUHexString())
            println(this.usMsgType)
            println(this.wUserActive)
            println(this.wGeneralFlag)
            println(this.lBindedUni)
        }

        override fun writeTo(builder: JceOutput) {
            //not needed
        }

        companion object : Factory<MessageNotification> {
            override fun newInstanceFrom(input: JceInput): MessageNotification {
                return MessageNotification(
                    input.read(0L, 0),
                    input.read(0.toByte(), 1),
                    input.readString(2),
                    input.readString(3),
                    input.read(EMPTY_BYTE_ARRAY, 4),
                    input.read(0, 5),
                    input.read(0, 6),
                    input.read(0, 7),
                    input.read(0L, 8)
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
        //val vCPicInfo: List<CPicInfo?>,
        //val stShareData: shareData,
        val lFromInstId: Long,
        val vRemarkOfSender: ByteArray,
        val strFromMobile: String,
        val strFromName: String,
        val vNickName: List<String>
        //val stC2CTmpMsgHead: TempMsgHead
    )

}