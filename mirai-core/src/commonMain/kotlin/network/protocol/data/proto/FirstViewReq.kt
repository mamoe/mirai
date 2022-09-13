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
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import kotlin.jvm.JvmField

@Serializable
internal class FirstViewReq(
    @JvmField @ProtoNumber(0) val lastMsgTime: Long = 0L,
    @JvmField @ProtoNumber(1) val seq: Short = 0,
    @JvmField @ProtoNumber(2) val directMessageFlag: Short = 1,
) : ProtoBuf

@Serializable
internal class FirstViewResp(
    @JvmField @ProtoNumber(1) val result: Short = 0,
    @JvmField @ProtoNumber(2) val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
    @JvmField @ProtoNumber(3) val seq: Short = 0,
    @JvmField @ProtoNumber(4) val udcFlag: Short = 0,
    @JvmField @ProtoNumber(5) val guildCount: Short = 0,
    @JvmField @ProtoNumber(6) val selfTinyId: Long = 0L,
    @JvmField @ProtoNumber(7) val directMessageSwitch: Short = 0,
    @JvmField @ProtoNumber(8) val directMessageGuildCount: Short = 0,
) : ProtoBuf