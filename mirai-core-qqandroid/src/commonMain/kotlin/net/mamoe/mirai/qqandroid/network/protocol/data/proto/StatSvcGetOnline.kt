package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

class StatSvcGetOnline {
    @Serializable
    class Instance(
        @SerialId(1) val instanceId: Int = 0,
        @SerialId(2) val clientType: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1) val errorCode: Int = 0,
        @SerialId(2) val errorMsg: String = "",
        @SerialId(3) val uin: Long = 0L,
        @SerialId(4) val appid: Int = 0,
        @SerialId(5) val timeInterval: Int = 0,
        @SerialId(6) val msgInstances: List<StatSvcGetOnline.Instance>? = null
    ) : ProtoBuf
}