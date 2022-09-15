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
internal class Oidb0xf551 : ProtoBuf{
    @Serializable
    internal class Req(
        @JvmField @ProtoNumber(1) var guildId: Long,
        @JvmField @ProtoNumber(2) var channelId: Long,
    ) : ProtoBuf

    @Serializable
    internal class Data(
        @JvmField @ProtoNumber(1) val info: Guild.GuildChannelInfo = Guild.GuildChannelInfo(),
    ) : ProtoBuf{
        override fun toString(): String {
            return "Data(info=$info)"
        }
    }
}



