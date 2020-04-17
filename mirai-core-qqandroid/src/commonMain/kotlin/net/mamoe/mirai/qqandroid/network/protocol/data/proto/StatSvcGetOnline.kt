/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

class StatSvcGetOnline {
    @Serializable
internal class Instance(
        @ProtoId(1) @JvmField val instanceId: Int = 0,
        @ProtoId(2) @JvmField val clientType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val appid: Int = 0
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoId(1) @JvmField val errorCode: Int = 0,
        @ProtoId(2) @JvmField val errorMsg: String = "",
        @ProtoId(3) @JvmField val uin: Long = 0L,
        @ProtoId(4) @JvmField val appid: Int = 0,
        @ProtoId(5) @JvmField val timeInterval: Int = 0,
        @ProtoId(6) @JvmField val msgInstances: List<StatSvcGetOnline.Instance>? = null
    ) : ProtoBuf
}