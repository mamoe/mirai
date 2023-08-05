/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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

@Serializable
internal class Oidb0xf8e : ProtoBuf {
    @Serializable
    internal class InfoValue(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0,
        @JvmField @ProtoNumber(2) val seq: Long = 0,
        @JvmField @ProtoNumber(3) val random: Int = 0,
        @JvmField @ProtoNumber(4) val uin: Long = 0,
        @JvmField @ProtoNumber(5) val nickname: String = "",
        @JvmField @ProtoNumber(6) val title: String = "",
        @JvmField @ProtoNumber(7) val jumpUrl: String = "",
        @JvmField @ProtoNumber(8) val iconUrl: String = "",
        @JvmField @ProtoNumber(9) val createTime: Int = 0,
        @JvmField @ProtoNumber(10) val appName: String = "",
        @JvmField @ProtoNumber(11) val appId: Long = 0,
        @JvmField @ProtoNumber(12) val msgType: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0,
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val info: InfoValue? = null,
        @JvmField @ProtoNumber(2) val rptGroupList: List<UserNode>? = null,
        @JvmField @ProtoNumber(3) val expTime: Int = 0,
    ) : ProtoBuf

    @Serializable
    internal class UserNode(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0,
        @JvmField @ProtoNumber(2) val seq: Long = 0,
        @JvmField @ProtoNumber(3) val status: Int = 0,
    ) : ProtoBuf

}