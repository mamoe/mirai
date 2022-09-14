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
internal class Oidb0xf579 : ProtoBuf {
    @Serializable
    internal class Req(
        @JvmField @ProtoNumber(1) var unKnown: UnKnown = UnKnown(),
        @JvmField @ProtoNumber(2) var param: Param = Param(),
    ) : ProtoBuf

    @Serializable
    internal class Param(
        @JvmField @ProtoNumber(1) var guildId: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class UnKnown(
        @JvmField @ProtoNumber(1) var unKnown1: UnKnown1 = UnKnown1(),
        @JvmField @ProtoNumber(2) var unKnown2: UnKnown2 = UnKnown2(),
    ) : ProtoBuf

    @Serializable
    internal class UnKnown1(
        @JvmField @ProtoNumber(2) var unKnown12: UnKnown12 = UnKnown12(),
        @JvmField @ProtoNumber(18) var unKnown118: UnKnown118 = UnKnown118(),
    ) : ProtoBuf

    @Serializable
    internal class UnKnown12(
        @JvmField @ProtoNumber(2) var unKnown2: Short = 1,
        @JvmField @ProtoNumber(4) var unKnown4: Short = 1,
        @JvmField @ProtoNumber(5) var unKnown5: Short = 1,
        @JvmField @ProtoNumber(6) var unKnown6: Short = 1,
        @JvmField @ProtoNumber(7) var unKnown7: Short = 1,
        @JvmField @ProtoNumber(8) var unKnown8: Short = 1,
        @JvmField @ProtoNumber(11) var unKnown11: Short = 1,
        @JvmField @ProtoNumber(12) var unKnown12: Short = 1,
        @JvmField @ProtoNumber(13) var unKnown13: Short = 1,
        @JvmField @ProtoNumber(14) var unKnown14: Short = 1,
        @JvmField @ProtoNumber(45) var unKnown45: Short = 1,
    ) : ProtoBuf

    @Serializable
    internal class UnKnown118(
        @JvmField @ProtoNumber(18) var unKnown18: Short = 1,
        @JvmField @ProtoNumber(19) var unKnown19: Short = 1,
        @JvmField @ProtoNumber(20) var unKnown20: Short = 1,
        @JvmField @ProtoNumber(22) var unKnown22: Short = 1,
        @JvmField @ProtoNumber(23) var unKnown23: Short = 1,
        @JvmField @ProtoNumber(5002) var unKnown5002: Short = 1,
        @JvmField @ProtoNumber(5003) var unKnown5003: Short = 1,
        @JvmField @ProtoNumber(5004) var unKnown5004: Short = 1,
        @JvmField @ProtoNumber(5005) var unKnown5005: Short = 1,
        @JvmField @ProtoNumber(10007) var unKnown10007: Short = 1,
    ) : ProtoBuf

    @Serializable
    internal class UnKnown2(
        @JvmField @ProtoNumber(3) var unKnown3: Short = 1,
        @JvmField @ProtoNumber(4) var unKnown4: Short = 1,
        @JvmField @ProtoNumber(6) var unKnown6: Short = 1,
        @JvmField @ProtoNumber(11) var unKnown11: Short = 1,
        @JvmField @ProtoNumber(14) var unKnown14: Short = 1,
        @JvmField @ProtoNumber(15) var unKnown15: Short = 1,
        @JvmField @ProtoNumber(16) var unKnown16: Short = 1,
        @JvmField @ProtoNumber(17) var unKnown17: Short = 1,
    ) : ProtoBuf

}