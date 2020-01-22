package net.mamoe.mirai.qqandroid.network.protocol.packet.image

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.PacketId
import net.mamoe.mirai.utils.currentTimeSeconds

@UseExperimental(ExperimentalUnsignedTypes::class)
internal object ImagePacket : PacketFactory<ImagePacket.RequestImgUrlResponse>() {
    init {
        this._id = PacketId(commandId = 0x0000, commandName = "LongConn.OffPicDown")
    }


    sealed class RequestImgUrlResponse : Packet {
        object Success : RequestImgUrlResponse()
    }


    fun createCmd0x325Packet(req: ImgReq, networkType: Int = 5): Cmd0x352Packet {
        if (req is UploadImgReq)
            return Cmd0x352Packet(1, req, null, null, networkType)
        if (req is GetImgUrlReq)
            return Cmd0x352Packet(2, null, req, null, networkType)
        error("Unknown ImgReq")
    }

    @Serializable
    internal class Cmd0x352Packet(
        @SerialId(1) val subCommand: Int, //2是GetImgUrlReq 1是UploadImgReq
        @SerialId(2) val uploadImgReq: UploadImgReq? = null,// optional
        @SerialId(3) val getImgUrlReq: GetImgUrlReq? = null,// optional
        @SerialId(4) val deleteImgReq: String? = "",// optional (没有做也不准备做, 没用)
        @SerialId(10) val networkType: Int = 5// 数据网络=5
    )

    interface ImgReq
    @Serializable
    class UploadImgReq(
        @SerialId(1) val srcUni: Int,
        @SerialId(2) val dstUni: Int,
        @SerialId(3) val fileId: Int,
        @SerialId(4) val fileMd5: ByteArray,
        @SerialId(5) val fileSize: Int,
        @SerialId(6) val fileName: String,
        @SerialId(7) val srcTerm: Int,
        @SerialId(8) val platformType: Int,
        @SerialId(9) val innerIP: Int = 0,
        @SerialId(10) val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        @SerialId(11) val retry: Int,
        @SerialId(12) val buType: Int,
        @SerialId(13) val imgOriginal: Int,//是否为原图
        @SerialId(14) val imgWidth: Int,
        @SerialId(15) val imgHeight: Int,
        @SerialId(16) val imgType: Int,
        @SerialId(17) val buildVer: String = "8.2.0.1296",//版本号
        @SerialId(18) val fileIndex: ByteArray,
        @SerialId(19) val fileStoreDays: Int,
        @SerialId(20) val stepFlag: Int,
        @SerialId(21) val rejectTryFast: Int,//bool
        @SerialId(22) val srvUpload: Int,
        @SerialId(23) val transferUrl: ByteArray
    ) : ImgReq

    @Serializable
    class GetImgUrlReq(
        @SerialId(1) val srcUni: Int,
        @SerialId(2) val dstUni: Int,
        @SerialId(3) val fileResID: String,//UUID
        /**
         * UUID例子:
         */
        @SerialId(4) val urlFlag: Int = 1,
        //5 unknown, 好像没用
        @SerialId(6) val urlType: Int = 4,
        @SerialId(7) val requestTerm: Int = 5,//确定
        @SerialId(8) val requestPlatformType: Int = 9,//确定
        @SerialId(9) val srcFileType: Int = 1,//2=ftn，1=picplatform，255
        @SerialId(10) val innerIP: Int = 0,//确定
        @SerialId(11) val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        /**
         * chattype
         * 1008时为Troop
         * 1   时为?
         * 9999时为？
         * 1036时为？
         * 1006时为？
         */
        @SerialId(12) val buType: Int = 1,//确定
        @SerialId(13) val buildVer: String = "8.2.0.1296",//版本号
        @SerialId(14) val timestamp: Int = currentTimeSeconds.toInt(),//(pic_up_timestamp)
        @SerialId(15) val requestTransferType: Int = 1
    ) : ImgReq


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestImgUrlResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}


