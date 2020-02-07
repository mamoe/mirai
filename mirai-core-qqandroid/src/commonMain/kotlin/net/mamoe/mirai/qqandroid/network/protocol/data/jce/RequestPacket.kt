package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

private val EMPTY_MAP = mapOf<String, String>()

@Serializable
internal class RequestPacket(
    @SerialId(1) val iVersion: Short = 3,
    @SerialId(2) val cPacketType: Byte = 0,
    @SerialId(3) val iMessageType: Int = 0,
    @SerialId(4) val iRequestId: Int,
    @SerialId(5) val sServantName: String = "",
    @SerialId(6) val sFuncName: String = "",
    @SerialId(7) val sBuffer: ByteArray = EMPTY_BYTE_ARRAY,
    @SerialId(8) val iTimeout: Int? = 0,
    @SerialId(9) val context: Map<String, String>? = EMPTY_MAP,
    @SerialId(10) val status: Map<String, String>? = EMPTY_MAP
) : JceStruct

@Serializable
internal class RequestDataVersion3(
    @SerialId(0) val map: Map<String, ByteArray> // 注意: ByteArray 不能直接放序列化的 JceStruct!! 要放类似 RequestDataStructSvcReqRegister 的
) : JceStruct

@Serializable
internal class RequestDataVersion2(
    @SerialId(0) val map: Map<String, Map<String, ByteArray>>
) : JceStruct

@Serializable
internal class RequestDataStructSvcReqRegister(
    @SerialId(0) val struct: SvcReqRegister
) : JceStruct