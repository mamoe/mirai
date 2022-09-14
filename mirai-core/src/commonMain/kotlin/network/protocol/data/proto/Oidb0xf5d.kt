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
internal class Oidb0xf5d : ProtoBuf {

    @Serializable
    internal class ChannelOidb0xf5dRsp(
        @JvmField @ProtoNumber(4) val rsp: ChannelOidb0xf5dData= ChannelOidb0xf5dData()
    ) : ProtoBuf

    @Serializable
    internal class ChannelOidb0xf5dData(
        @JvmField @ProtoNumber(1) val rsp: ChannelListRsp= ChannelListRsp()
    ) : ProtoBuf

    @Serializable
    internal class ChannelListRsp(
        @JvmField @ProtoNumber(1) val guildId: Long= 0L,
        @JvmField @ProtoNumber(2) val channels: List<Guild.GuildChannelInfo> = mutableListOf()
        // 5: Category infos
    ) : ProtoBuf


}