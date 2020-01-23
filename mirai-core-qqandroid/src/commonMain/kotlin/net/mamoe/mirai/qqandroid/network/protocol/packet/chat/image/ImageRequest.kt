package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.currentTimeSeconds

interface ImgReq

@Serializable
internal class UploadImgReq(
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
internal class GetImgUrlReq(
    @SerialId(1) val srcUni: Int,
    @SerialId(2) val dstUni: Int,
    @SerialId(3) val fileResID: String,//UUID
    /**
     * UUID例子: 没有找到
     */
    @SerialId(4) val urlFlag: Int = 1,
    //5 unknown, 好像没用
    @SerialId(6) val urlType: Int = 4,
    @SerialId(7) val requestTerm: Int = 5,//确定
    @SerialId(8) val requestPlatformType: Int = 9,//确定
    @SerialId(9) val srcFileType: Int = 1,//2=ftn，1=picplatform，255
    @SerialId(10) val innerIP: Int = 0,//确定
    @SerialId(11) val addressBook: Int = 0,//[ChatType.internalID]== 1006为1[为CONTACT时] 我觉得发0没问题
    @SerialId(12) val buType: Int = 1,//确定
    @SerialId(13) val buildVer: String = "8.2.0.1296",//版本号
    @SerialId(14) val timestamp: Int = currentTimeSeconds.toInt(),//(pic_up_timestamp)
    @SerialId(15) val requestTransferType: Int = 1
) : ImgReq