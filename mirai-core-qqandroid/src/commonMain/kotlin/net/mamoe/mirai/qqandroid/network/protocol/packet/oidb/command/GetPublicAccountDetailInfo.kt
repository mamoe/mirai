package net.mamoe.mirai.qqandroid.network.protocol.packet.oidb.command

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.MessageMicro

/**
 * oidb_cmd0xcf8$GetPublicAccountDetailInfoRequest
 */
@Serializable
class GetPublicAccountDetailInfoRequest(
    val seqno: Int, // uint
    val uinLong: Long,
    val version: Int, // uint
    val versionInfo: String
) : MessageMicro

class GetPublicAccountDetailInfoResponse(

) : MessageMicro