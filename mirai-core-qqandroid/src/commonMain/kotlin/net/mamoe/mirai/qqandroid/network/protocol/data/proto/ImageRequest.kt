package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.utils.currentTimeSeconds

interface ImgReq : ProtoBuf

// cmd0x352$ReqBody

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