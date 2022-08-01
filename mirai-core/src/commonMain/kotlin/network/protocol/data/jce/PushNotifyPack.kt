/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.NestedStructure
import net.mamoe.mirai.internal.utils.io.NestedStructureDesensitizer
import net.mamoe.mirai.internal.utils.io.ProtocolStruct
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.jvm.JvmField

@Suppress("ArrayInDataClass")
@Serializable
internal class RequestPushNotify(
    @TarsId(0) @JvmField val uin: Long? = 0L,
    @TarsId(1) @JvmField val ctype: Byte = 0,
    @TarsId(2) @JvmField val strService: String?,
    @TarsId(3) @JvmField val strCmd: String?,
    @TarsId(4) @JvmField val vNotifyCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @TarsId(5) @JvmField val usMsgType: Int?,
    @TarsId(6) @JvmField val wUserActive: Int?,
    @TarsId(7) @JvmField val wGeneralFlag: Int?,
    @TarsId(8) @JvmField val bindedUin: Long?,
    @TarsId(9) @JvmField val stMsgInfo: MsgInfo?,
    @TarsId(10) @JvmField val msgCtrlBuf: String?,
    @TarsId(11) @JvmField val serverBuf: ByteArray?,
    @TarsId(12) @JvmField val pingFlag: Long?,
    @TarsId(13) @JvmField val svrip: Int?,
) : JceStruct, Packet {
    override fun toString(): String {
        return "RequestPushNotify(usMsgType=$usMsgType)"
    }
}

@Serializable
internal class MsgInfo(
    @TarsId(0) @JvmField val lFromUin: Long = 0L,
    @TarsId(1) @JvmField val uMsgTime: Long = 0L,
    @TarsId(2) @JvmField val shMsgType: Short,
    @TarsId(3) @JvmField val shMsgSeq: Short,
    @TarsId(4) @JvmField val strMsg: String?,
    @TarsId(5) @JvmField val uRealMsgTime: Int?,
    @param:NestedStructure(VMsgDesensitizationSerializer::class)
    @TarsId(6) @JvmField val vMsg: ByteArray,
    @TarsId(7) @JvmField val uAppShareID: Long?,
    @TarsId(8) @JvmField val vMsgCookies: ByteArray? = EMPTY_BYTE_ARRAY,
    @TarsId(9) @JvmField val vAppShareCookie: ByteArray? = EMPTY_BYTE_ARRAY,
    @TarsId(10) @JvmField val lMsgUid: Long?,
    @TarsId(11) @JvmField val lLastChangeTime: Long?,
    @TarsId(12) @JvmField val vCPicInfo: List<CPicInfo>?,
    @TarsId(13) @JvmField val stShareData: ShareData?,
    @TarsId(14) @JvmField val lFromInstId: Long?,
    @TarsId(15) @JvmField val vRemarkOfSender: ByteArray?,
    @TarsId(16) @JvmField val strFromMobile: String?,
    @TarsId(17) @JvmField val strFromName: String?,
    @TarsId(18) @JvmField val vNickName: List<String>?, //,
    //@SerialId(19) @JvmField val stC2CTmpMsgHead: TempMsgHead?
) : JceStruct

internal object VMsgDesensitizationSerializer : NestedStructureDesensitizer<MsgInfo, ProtocolStruct> {
    override fun deserialize(context: MsgInfo, byteArray: ByteArray): ProtocolStruct? {
        return when (context.shMsgType.toUShort().toInt()) {
            0x210 -> byteArray.loadAs(MsgType0x210.serializer())
            else -> null
        }
    }
}


@Serializable
internal class ShareData(
    @TarsId(0) @JvmField val pkgname: String = "",
    @TarsId(1) @JvmField val msgtail: String = "",
    @TarsId(2) @JvmField val picurl: String = "",
    @TarsId(3) @JvmField val url: String = "",
) : JceStruct

@Serializable
internal class TempMsgHead(
    @TarsId(0) @JvmField val c2c_type: Int? = 0,
    @TarsId(1) @JvmField val serviceType: Int? = 0,
) : JceStruct

@Serializable
internal class CPicInfo(
    @TarsId(0) @JvmField val vPath: ByteArray = EMPTY_BYTE_ARRAY,
    @TarsId(1) @JvmField val vHost: ByteArray? = EMPTY_BYTE_ARRAY,
) : JceStruct