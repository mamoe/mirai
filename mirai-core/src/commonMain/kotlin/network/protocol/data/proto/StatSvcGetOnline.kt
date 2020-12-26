/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf

internal class StatSvcGetOnline {
    @Serializable
    internal class Instance(
        @ProtoNumber(1) @JvmField val instanceId: Int = 0,
        @ProtoNumber(2) @JvmField val clientType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val errorCode: Int = 0,
        @ProtoNumber(2) @JvmField val errorMsg: String = "",
        @ProtoNumber(3) @JvmField val uin: Long = 0L,
        @ProtoNumber(4) @JvmField val appid: Int = 0,
        @ProtoNumber(5) @JvmField val timeInterval: Int = 0,
        @ProtoNumber(6) @JvmField val msgInstances: List<StatSvcGetOnline.Instance> = emptyList()
    ) : ProtoBuf
}