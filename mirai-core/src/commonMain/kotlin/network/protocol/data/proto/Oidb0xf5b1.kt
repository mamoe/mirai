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
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val guildId: Long,
        @JvmField @ProtoNumber(2) val unKnown1: Short,
        @JvmField @ProtoNumber(3) val unKnown2: Short,
        @JvmField @ProtoNumber(4) val unKnown3: UnKnown3 = UnKnown3(),
        @JvmField @ProtoNumber(6) val startIndex: Short,
        @JvmField @ProtoNumber(8) val count: Short,
        @JvmField @ProtoNumber(12) val channelId: Long,
        @JvmField @ProtoNumber(13) val param: String? = null, //maybe not
        @JvmField @ProtoNumber(14) val roleIdIndex: Long,
    ) : ProtoBuf {
        override fun toString(): String {
            return "ReqBody(guildId=$guildId, unKnown1=$unKnown1, unKnown2=$unKnown2, unKnown3=$unKnown3, startIndex=$startIndex, count=$count, channelId=$channelId, roleIdIndex=$roleIdIndex)"
        }
    }

    @Serializable
    internal class UnKnown3(
        @JvmField @ProtoNumber(1) val unKnown31: Short = 1,
        @JvmField @ProtoNumber(3) val unKnown32: Short = 1,
        @JvmField @ProtoNumber(4) val unKnown33: Short = 1,
        @JvmField @ProtoNumber(5) val unKnown34: Short = 1,
        @JvmField @ProtoNumber(6) val unKnown35: Short = 1,
        @JvmField @ProtoNumber(7) val unKnown36: Short = 1,
        @JvmField @ProtoNumber(8) val unKnown37: Short = 1,
        @JvmField @ProtoNumber(9) val unKnown38: Short = 1,
        @JvmField @ProtoNumber(10) val unKnown310: Short = 1,
        @JvmField @ProtoNumber(20) val unKnown320: Short = 1,
        @JvmField @ProtoNumber(27) val unKnown327: Short = 1,
        @JvmField @ProtoNumber(35) val unKnown335: Short = 1,
    ) : ProtoBuf {
        override fun toString(): String {
            return "UnKnown3(unKnown31=$unKnown31, unKnown32=$unKnown32, unKnown33=$unKnown33, unKnown34=$unKnown34, unKnown35=$unKnown35, unKnown36=$unKnown36, unKnown37=$unKnown37, unKnown38=$unKnown38, unKnown310=$unKnown310, unKnown320=$unKnown320, unKnown327=$unKnown327, unKnown335=$unKnown335)"
        }
    }


    @Serializable
    internal class Rsp(
        @JvmField @ProtoNumber(4) val data: Data = Data(),
        @JvmField @ProtoNumber(5) val state: String = "",//ok
    ) : ProtoBuf {
        override fun toString(): String {
            return "Rsp(data=$data, state='$state')"
        }
    }

    @Serializable
    internal class Data(
        @JvmField @ProtoNumber(1) val guildId: Long = 0L,
        @JvmField @ProtoNumber(4) val bots: List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(5) val members: List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(10) val nextIndex: Short? = null,
        @JvmField @ProtoNumber(9) val finished: Short? = null,
        @JvmField @ProtoNumber(24) val nextQueryParam: String = "",
        @JvmField @ProtoNumber(25) val memberWithRoles: List<GuildGroupMembersInfo> = mutableListOf(),
        @JvmField @ProtoNumber(26) val nextRoleIdIndex: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "Data(guildId=$guildId, bots=$bots, members=$members, nextIndex=$nextIndex, finished=$finished, nextQueryParam='$nextQueryParam', memberWithRoles=$memberWithRoles, nextRoleIdIndex=$nextRoleIdIndex)"
        }
    }

    @Serializable
    internal class GuildMemberInfo(
        @JvmField @ProtoNumber(2) val title: String = "",
        @JvmField @ProtoNumber(3) val nickname: String = "",
        @JvmField @ProtoNumber(4) val lastSpeakTime: Long = 0L,
        @JvmField @ProtoNumber(5) val role: Short = 0,
        @JvmField @ProtoNumber(8) val tinyId: Long = 0L,
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildMemberInfo(title='$title', nickname='$nickname', lastSpeakTime=$lastSpeakTime, role=$role, tinyId=$tinyId)"
        }
    }

    @Serializable
    internal class GuildGroupMembersInfo(
        @JvmField @ProtoNumber(1) val roleId: Long = 0L,
        @JvmField @ProtoNumber(2) val members: List<GuildMemberInfo> = mutableListOf(),
        @JvmField @ProtoNumber(3) val roleName: String = "",
        @JvmField @ProtoNumber(4) val color: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "GuildGroupMembersInfo(roleId=$roleId, members=$members, roleName='$roleName', color=$color)"
        }
    }
}