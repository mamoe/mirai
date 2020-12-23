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

@Serializable
internal class MarketFaceExtPb : ProtoBuf {
    @Serializable
    internal class ResvAttr(
        @JvmField @ProtoNumber(1) val supportSize: List<SupportSize> = emptyList(),
        @JvmField @ProtoNumber(2) val sourceType: Int = 0,
        @JvmField @ProtoNumber(3) val sourceName: String = "",
        @JvmField @ProtoNumber(4) val sourceJumpUrl: String = "",
        @JvmField @ProtoNumber(5) val sourceTypeName: String = "",
        @JvmField @ProtoNumber(6) val startTime: Int = 0,
        @JvmField @ProtoNumber(7) val endTime: Int = 0,
        @JvmField @ProtoNumber(8) val emojiType: Int = 0,
        @JvmField @ProtoNumber(9) val apngSupportSize: List<SupportSize> = emptyList(),
        @JvmField @ProtoNumber(10) val hasIpProduct: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SupportSize(
        @JvmField @ProtoNumber(1) val width: Int = 0,
        @JvmField @ProtoNumber(2) val height: Int = 0
    ) : ProtoBuf
}
        