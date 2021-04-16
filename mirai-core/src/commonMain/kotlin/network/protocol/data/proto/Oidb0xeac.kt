/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY

@Serializable
internal class Oidb0xeac : ProtoBuf {
    @Serializable
    internal class ArkMsg(
        @JvmField @ProtoNumber(1) val appName: String = "",
        @JvmField @ProtoNumber(2) val json: String = ""
    ) : ProtoBuf

    @Serializable
    internal class BatchReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val msgs: List<Oidb0xeac.MsgInfo> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class BatchRspBody(
        @JvmField @ProtoNumber(1) val wording: String = "",
        @JvmField @ProtoNumber(2) val errorCode: Int = 0,
        @JvmField @ProtoNumber(3) val succCnt: Int = 0,
        @JvmField @ProtoNumber(4) val msgProcInfos: List<Oidb0xeac.MsgProcessInfo> = emptyList(),
        @JvmField @ProtoNumber(5) val digestTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DigestMsg(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val msgSeq: Int = 0,
        @JvmField @ProtoNumber(3) val msgRandom: Int = 0,
        @JvmField @ProtoNumber(4) val msgContent: List<Oidb0xeac.MsgElem> = emptyList(),
        @JvmField @ProtoNumber(5) val textSize: Long = 0L,
        @JvmField @ProtoNumber(6) val picSize: Long = 0L,
        @JvmField @ProtoNumber(7) val videoSize: Long = 0L,
        @JvmField @ProtoNumber(8) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(9) val senderTime: Int = 0,
        @JvmField @ProtoNumber(10) val addDigestUin: Long = 0L,
        @JvmField @ProtoNumber(11) val addDigestTime: Int = 0,
        @JvmField @ProtoNumber(12) val startTime: Int = 0,
        @JvmField @ProtoNumber(13) val latestMsgSeq: Int = 0,
        @JvmField @ProtoNumber(14) val opType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FaceMsg(
        @JvmField @ProtoNumber(1) val index: Int = 0,
        @JvmField @ProtoNumber(2) val text: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GroupFileMsg(
        @JvmField @ProtoNumber(1) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val busId: Int = 0,
        @JvmField @ProtoNumber(3) val fileId: String = "",
        @JvmField @ProtoNumber(4) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(5) val deadTime: Long = 0L,
        @JvmField @ProtoNumber(6) val fileSha1: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val ext: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageMsg(
        @JvmField @ProtoNumber(1) val md5: String = "",
        @JvmField @ProtoNumber(2) val uuid: String = "",
        @JvmField @ProtoNumber(3) val imgType: Int = 0,
        @JvmField @ProtoNumber(4) val fileSize: Int = 0,
        @JvmField @ProtoNumber(5) val width: Int = 0,
        @JvmField @ProtoNumber(6) val height: Int = 0,
        @JvmField @ProtoNumber(101) val fileId: Int = 0,
        @JvmField @ProtoNumber(102) val serverIp: Int = 0,
        @JvmField @ProtoNumber(103) val serverPort: Int = 0,
        @JvmField @ProtoNumber(104) val filePath: String = "",
        @JvmField @ProtoNumber(201) val thumbUrl: String = "",
        @JvmField @ProtoNumber(202) val originalUrl: String = "",
        @JvmField @ProtoNumber(203) val resaveUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MsgElem(
        @JvmField @ProtoNumber(1) val msgType: Int = 0,
        @JvmField @ProtoNumber(11) val textMsg: Oidb0xeac.TextMsg? = null,
        @JvmField @ProtoNumber(12) val faceMsg: Oidb0xeac.FaceMsg? = null,
        @JvmField @ProtoNumber(13) val imageMsg: Oidb0xeac.ImageMsg? = null,
        @JvmField @ProtoNumber(14) val groupFileMsg: Oidb0xeac.GroupFileMsg? = null,
        @JvmField @ProtoNumber(15) val shareMsg: Oidb0xeac.ShareMsg? = null,
        @JvmField @ProtoNumber(16) val richMsg: Oidb0xeac.RichMsg? = null,
        @JvmField @ProtoNumber(17) val arkMsg: Oidb0xeac.ArkMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgInfo(
        @JvmField @ProtoNumber(1) val msgSeq: Int = 0,
        @JvmField @ProtoNumber(2) val msgRandom: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgProcessInfo(
        @JvmField @ProtoNumber(1) val msg: Oidb0xeac.MsgInfo? = null,
        @JvmField @ProtoNumber(2) val errorCode: Int = 0,
        @JvmField @ProtoNumber(3) val digestUin: Long = 0L,
        @JvmField @ProtoNumber(4) val digestTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val msgSeq: Int = 0,
        @JvmField @ProtoNumber(3) val msgRandom: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RichMsg(
        @JvmField @ProtoNumber(1) val serviceId: Int = 0,
        @JvmField @ProtoNumber(2) val xml: String = "",
        @JvmField @ProtoNumber(3) val longMsgResid: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val wording: String = "",
        @JvmField @ProtoNumber(2) val digestUin: Long = 0L,
        @JvmField @ProtoNumber(3) val digestTime: Int = 0,
        @JvmField @ProtoNumber(4) val msg: Oidb0xeac.DigestMsg? = null,
        @JvmField @ProtoNumber(10) val errorCode: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShareMsg(
        @JvmField @ProtoNumber(1) val type: String = "",
        @JvmField @ProtoNumber(2) val title: String = "",
        @JvmField @ProtoNumber(3) val summary: String = "",
        @JvmField @ProtoNumber(4) val brief: String = "",
        @JvmField @ProtoNumber(5) val url: String = "",
        @JvmField @ProtoNumber(6) val pictureUrl: String = "",
        @JvmField @ProtoNumber(7) val action: String = "",
        @JvmField @ProtoNumber(8) val source: String = "",
        @JvmField @ProtoNumber(9) val sourceUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TextMsg(
        @JvmField @ProtoNumber(1) val str: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}
