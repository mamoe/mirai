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
internal class Oidb0xf57 : ProtoBuf {
    @Serializable
    internal class Rsp(
        @JvmField @ProtoNumber(4) var rsp: Data = Data(),
    ) : ProtoBuf

    @Serializable
    internal class Data(
        @JvmField @ProtoNumber(1) var rsp: GuildMetaRsp = GuildMetaRsp(),
    ) : ProtoBuf

    @Serializable
    internal class GuildMetaRsp(
        @JvmField @ProtoNumber(3) var guildId: Long = 0L,
        @JvmField @ProtoNumber(4) var meta: GuildMeta = GuildMeta(),
    ) : ProtoBuf

    @Serializable
    internal class GuildMeta(
        @JvmField @ProtoNumber(2) var guildCode: Long = 0L,
        @JvmField @ProtoNumber(4) var createTime: Long = 0L,
        @JvmField @ProtoNumber(5) var maxMemberCount: Long = 0L,
        @JvmField @ProtoNumber(6) var memberCount: Long = 0L,
        @JvmField @ProtoNumber(8) var name: String = "",
        @JvmField @ProtoNumber(11) var robotMaxNum: Short = 0,
        @JvmField @ProtoNumber(12) var adminMaxNum: Short = 0,
        @JvmField @ProtoNumber(13) var profile: String = "",
        @JvmField @ProtoNumber(14) var avatarSeq: Long = 0L,
        @JvmField @ProtoNumber(18) var ownerId: Long = 0L,
        @JvmField @ProtoNumber(19) var coverSeq: Long = 0L,
        @JvmField @ProtoNumber(20) var cilentId: Long = 0L,
    ) : ProtoBuf
}