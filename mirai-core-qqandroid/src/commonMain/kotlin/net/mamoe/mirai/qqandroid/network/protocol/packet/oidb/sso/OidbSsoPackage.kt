package net.mamoe.mirai.qqandroid.network.protocol.packet.oidb.sso

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.MessageMicro

/**
 * oidb_sso$OIDBSSOPkg
 */
@Serializable
class OidbSsoPackage(
    @SerialId(1) val command: Int, // uint
    @SerialId(2) val serviceType: Int, // uint
    @SerialId(3) val result: Int, // uint
    @SerialId(4) val bodyBuffer: ByteArray,
    @SerialId(5) val errorMessage: String,
    @SerialId(6) val clientVersion: String
) : MessageMicro




