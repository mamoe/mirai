package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class StatSvcGetOnline {
    @Serializable
    internal class Instance(
        @ProtoNumber(1) @JvmField val instanceId: Int = 0,
        @ProtoNumber(2) @JvmField val clientType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val errorCode: Int = 0,
        @ProtoNumber(2) @JvmField val errorMsg: String = "",
        @ProtoNumber(3) @JvmField val uin: Long = 0L,
        @ProtoNumber(4) @JvmField val appid: Int = 0,
        @ProtoNumber(5) @JvmField val timeInterval: Int = 0,
        @ProtoNumber(6) @JvmField val msgInstances: List<StatSvcGetOnline.Instance>? = null
    ) : ProtoBuf
}