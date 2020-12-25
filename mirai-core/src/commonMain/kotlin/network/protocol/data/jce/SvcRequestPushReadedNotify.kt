/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

@Serializable
internal class SvcRequestPushReadedNotify(
    @JvmField @TarsId(0) val notifyType: Byte,
    @JvmField @TarsId(1) val vC2CReadedNotify: List<C2CMsgReadedNotify>? = null,
    @JvmField @TarsId(2) val vGroupReadedNotify: List<GroupMsgReadedNotify>? = null,
    @JvmField @TarsId(3) val vDisReadedNotify: List<DisMsgReadedNotify>? = null
) : JceStruct


@Serializable
internal class C2CMsgReadedNotify(
    @JvmField @TarsId(0) val peerUin: Long? = null,
    @JvmField @TarsId(1) val lastReadTime: Long? = null,
    @JvmField @TarsId(2) val flag: Long? = null,
    @JvmField @TarsId(3) val phoneNum: String? = "",
    @JvmField @TarsId(4) val bindedUin: Long? = null
) : JceStruct

@Serializable
internal class DisMsgReadedNotify(
    @JvmField @TarsId(0) val disUin: Long? = null,
    @JvmField @TarsId(1) val opType: Long? = null,
    @JvmField @TarsId(2) val memberSeq: Long? = null,
    @JvmField @TarsId(3) val disMsgSeq: Long? = null
) : JceStruct


@Serializable
internal class GPicInfo(
    @JvmField @TarsId(0) val vPath: ByteArray,
    @JvmField @TarsId(1) val vHost: ByteArray? = null
) : JceStruct


@Serializable
internal class GroupMsgHead(
    @JvmField @TarsId(0) val usCmdType: Int,
    @JvmField @TarsId(1) val totalPkg: Byte,
    @JvmField @TarsId(2) val curPkg: Byte,
    @JvmField @TarsId(3) val usPkgSeq: Int,
    @JvmField @TarsId(4) val dwReserved: Long
) : JceStruct

@Serializable
internal class GroupMsgReadedNotify(
    @JvmField @TarsId(0) val groupCode: Long? = null,
    @JvmField @TarsId(1) val opType: Long? = null,
    @JvmField @TarsId(2) val memberSeq: Long? = null,
    @JvmField @TarsId(3) val groupMsgSeq: Long? = null
) : JceStruct

@Serializable
internal class RequestPushGroupMsg(
    @JvmField @TarsId(0) val uin: Long,
    @JvmField @TarsId(1) val type: Byte,
    @JvmField @TarsId(2) val service: String = "",
    @JvmField @TarsId(3) val cmd: String = "",
    @JvmField @TarsId(4) val groupCode: Long,
    @JvmField @TarsId(5) val groupType: Byte,
    @JvmField @TarsId(6) val sendUin: Long,
    @JvmField @TarsId(7) val lsMsgSeq: Long,
    @JvmField @TarsId(8) val uMsgTime: Int,
    @JvmField @TarsId(9) val infoSeq: Long,
    @JvmField @TarsId(10) val shMsgLen: Short,
    @JvmField @TarsId(11) val vMsg: ByteArray,
    @JvmField @TarsId(12) val groupCard: String? = "",
    @JvmField @TarsId(13) val uAppShareID: Long? = null,
    @JvmField @TarsId(14) val vGPicInfo: List<GPicInfo>? = null,
    @JvmField @TarsId(15) val vAppShareCookie: ByteArray? = null,
    @JvmField @TarsId(16) val stShareData: ShareData? = null,
    @JvmField @TarsId(17) val fromInstId: Long? = null,
    @JvmField @TarsId(18) val stGroupMsgHead: GroupMsgHead? = null,
    @JvmField @TarsId(19) val wUserActive: Int? = null,
    @JvmField @TarsId(20) val vMarketFace: List<MarketFaceInfo>? = null,
    @JvmField @TarsId(21) val uSuperQQBubbleId: Long? = null
) : JceStruct

