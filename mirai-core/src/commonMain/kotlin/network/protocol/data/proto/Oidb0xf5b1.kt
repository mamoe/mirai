/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class Oidb0xf5b1 : ProtoBuf {

    @Serializable
    internal class Req(
        @JvmField @ProtoNumber(1) val guildId: Long = 0L,
        @JvmField @ProtoNumber(2) val unKnown1 :Short = 3,
        @JvmField @ProtoNumber(3) val unKnown2 :Short = 0,
        @JvmField @ProtoNumber(4) val unKnown3 :UnKnown3 = UnKnown3(),
        @JvmField @ProtoNumber(6) val startIndex :Short = 0,
        @JvmField @ProtoNumber(8) val count :Short = 50,
        @JvmField @ProtoNumber(12) val channelId :Long = 0L,
        @JvmField @ProtoNumber(13) val param :String = "",
        @JvmField @ProtoNumber(14) val roleIdIndex :Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class UnKnown3(
        @JvmField @ProtoNumber(1) val unKnown1 :Short = 1,
        @JvmField @ProtoNumber(2) val unKnown2 :Short = 1,
        @JvmField @ProtoNumber(3) val unKnown3 :Short = 1,
        @JvmField @ProtoNumber(4) val unKnown4 :Short = 1,
        @JvmField @ProtoNumber(5) val unKnown5 :Short = 1,
        @JvmField @ProtoNumber(6) val unKnown6 :Short = 1,
        @JvmField @ProtoNumber(7) val unKnown7 :Short = 1,
        @JvmField @ProtoNumber(8) val unKnown8 :Short = 1,
        @JvmField @ProtoNumber(20) val unKnown20 :Short = 1,
    ) : ProtoBuf



    @Serializable
    internal class Rsp(
        @JvmField @ProtoNumber(1) val guildId: Long = 0L,
        @JvmField @ProtoNumber(4) val bots: List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(5) val members:  List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(10) val nextIndex:  Short = 0,
        @JvmField @ProtoNumber(9) val finished:  Short = 0,
        @JvmField @ProtoNumber(24) val nextQueryParam:  String = "",
        @JvmField @ProtoNumber(25) val memberWithRoles:  GuildGroupMembersInfo = GuildGroupMembersInfo(),
        @JvmField @ProtoNumber(26) val nextRoleIdIndex: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class GuildMemberInfo(
        @JvmField @ProtoNumber(2) val title: String = "",
        @JvmField @ProtoNumber(3) val nickname: String = "",
        @JvmField @ProtoNumber(4) val lastSpeakTime: Long = 0L,
        @JvmField @ProtoNumber(5) val role: Short = 0,
        @JvmField @ProtoNumber(8) val tinyId: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class GuildGroupMembersInfo(
        @JvmField @ProtoNumber(1) val roleId: Long = 0L,
        @JvmField @ProtoNumber(2) val members: List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(3) val roleName: String = "",
        @JvmField @ProtoNumber(4) val color: Short = 0,
    ) : ProtoBuf
}