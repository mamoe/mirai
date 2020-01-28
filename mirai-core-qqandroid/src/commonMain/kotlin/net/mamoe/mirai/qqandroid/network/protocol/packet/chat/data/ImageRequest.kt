package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.protobuf.ProtoBuf
import net.mamoe.mirai.utils.currentTimeSeconds

interface ImgReq : ProtoBuf

@Serializable
internal class UploadImgReq(
    @SerialId(1) val srcUin: Int,
    @SerialId(2) val dstUin: Int,
    @SerialId(3) val fileId: Int = 0,//从0开始的自增数？貌似有一个连接就要自增1, 但是又会重置回0
    @SerialId(4) val fileMd5: String,
    @SerialId(5) val fileSize: Int,
    @SerialId(6) val fileName: String,//默认为md5+".jpg"
    @SerialId(7) val srcTerm: Int = 5,
    @SerialId(8) val platformType: Int = 9,
    @SerialId(9) val innerIP: Int = 0,
    @SerialId(10) val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
    @SerialId(11) val retry: Int = 0,//default
    @SerialId(12) val buType: Int,//1或96 不确定
    @SerialId(13) val imgOriginal: Int,//是否为原图
    @SerialId(14) val imgWidth: Int,
    @SerialId(15) val imgHeight: Int,
    @SerialId(16) val imgType: Int = 1000,
    /**
     * ImgType:
     *  JPG:    1000
     *  PNG:    1001
     *  WEBP:   1002
     *  BMP:    1005
     *  GIG:    2000
     *  APNG:   2001
     *  SHARPP: 1004
     * */
    @SerialId(17) val buildVer: String = "8.2.0.1296",//版本号
    @SerialId(18) val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,//default
    @SerialId(19) val fileStoreDays: Int = 0,//default
    @SerialId(20) val stepFlag: Int = 0,//default
    @SerialId(21) val rejectTryFast: Int = 0,//bool
    @SerialId(22) val srvUpload: Int = 1,//typeHotPic[1/2/3]
    @SerialId(23) val transferUrl: ByteArray = EMPTY_BYTE_ARRAY//rawDownloadUrl, 如果没有就是EMPTY_BYTE_ARRAY
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