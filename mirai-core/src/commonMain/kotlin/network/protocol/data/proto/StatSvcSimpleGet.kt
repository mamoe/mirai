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


internal class StatSvcSimpleGet {
    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val errorCode: Int = 0,
        @JvmField @ProtoNumber(2) val errmsg: String = "",
        @JvmField @ProtoNumber(3) val helloInterval: Int = 0,
        @JvmField @ProtoNumber(4) val clientip: String = "",
        @JvmField @ProtoNumber(5) val clientBatteyGetInterval: Int = 0
    ) : ProtoBuf
}
