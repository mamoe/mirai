/*
* Copyright 2019-2020 Mamoe Technologies and contributors.
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

internal class FrdSysMsg {
    @Serializable
    internal class AddFrdSNInfo(
        @JvmField @ProtoNumber(1) val notSeeDynamic: Int = 0,
        @JvmField @ProtoNumber(2) val setSn: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AddFriendVerifyInfo(
        @JvmField @ProtoNumber(1) val type: Int = 0,
        @JvmField @ProtoNumber(2) val url: String = "",
        @JvmField @ProtoNumber(3) val verifyInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class AddtionInfo(
        @JvmField @ProtoNumber(1) val poke: Int = 0,
        @JvmField @ProtoNumber(2) val format: Int = 0,
        @JvmField @ProtoNumber(3) val entityCategory: String = "",
        @JvmField @ProtoNumber(4) val entityName: String = "",
        @JvmField @ProtoNumber(5) val entityUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class DiscussInfo(
        @JvmField @ProtoNumber(1) val discussUin: Long = 0L,
        @JvmField @ProtoNumber(2) val discussName: String = "",
        @JvmField @ProtoNumber(3) val discussNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class EimInfo(
        @JvmField @ProtoNumber(1) val eimFuin: Long = 0L,
        @JvmField @ProtoNumber(2) val eimId: String = "",
        @JvmField @ProtoNumber(3) val eimTelno: String = "",
        @JvmField @ProtoNumber(4) val groupId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FriendHelloInfo(
        @JvmField @ProtoNumber(1) val sourceName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class FriendMiscInfo(
        @JvmField @ProtoNumber(1) val fromuinNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class FriendSysMsg(
        @JvmField @ProtoNumber(11) val msgGroupExt: GroupInfoExt? = null,
        @JvmField @ProtoNumber(12) val msgIntiteInfo: InviteInfo? = null,
        @JvmField @ProtoNumber(13) val msgSchoolInfo: SchoolInfo? = null,
        @JvmField @ProtoNumber(100) val doubtFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @JvmField @ProtoNumber(1) val groupUin: Long = 0L,
        @JvmField @ProtoNumber(2) val groupName: String = "",
        @JvmField @ProtoNumber(3) val groupNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GroupInfoExt(
        @JvmField @ProtoNumber(1) val notifyType: Int = 0,
        @JvmField @ProtoNumber(2) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(3) val fromGroupadmlist: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InviteInfo(
        @JvmField @ProtoNumber(1) val recommendUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MsgEncodeFlag(
        @JvmField @ProtoNumber(1) val isUtf8: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SchoolInfo(
        @JvmField @ProtoNumber(1) val schoolId: String = "",
        @JvmField @ProtoNumber(2) val schoolName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TongXunLuNickInfo(
        @JvmField @ProtoNumber(1) val fromuin: Long = 0L,
        @JvmField @ProtoNumber(2) val touin: Long = 0L,
        @JvmField @ProtoNumber(3) val tongxunluNickname: String = ""
    ) : ProtoBuf
}