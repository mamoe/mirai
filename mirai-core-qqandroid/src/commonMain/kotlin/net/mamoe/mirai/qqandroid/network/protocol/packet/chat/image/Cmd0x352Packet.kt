package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable

@Serializable
internal class Cmd0x352Packet(
    @SerialId(1) val subCommand: Int, //2是GetImgUrlReq 1是UploadImgReq
    @SerialId(2) val uploadImgReq: UploadImgReq? = null,// optional
    @SerialId(3) val getImgUrlReq: GetImgUrlReq? = null,// optional
    @SerialId(4) val deleteImgReq: String? = "",// optional (没有做也不准备做, 没用)
    @SerialId(10) val networkType: Int = 5// 数据网络=5
) {
    companion object {
        fun createByImageRequest(req: ImgReq, networkType: Int = 5): Cmd0x352Packet {
            if (req is UploadImgReq)
                return Cmd0x352Packet(
                    1,
                    req,
                    null,
                    null,
                    networkType
                )
            if (req is GetImgUrlReq)
                return Cmd0x352Packet(
                    2,
                    null,
                    req,
                    null,
                    networkType
                )
            error("Unknown ImgReq")
        }
    }

}