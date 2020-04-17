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

class MsgRevokeUserDef : ProtoBuf {
    @Serializable
internal class MsgInfoUserDef(
        @ProtoId(1) @JvmField val longMessageFlag: Int = 0,
        @ProtoId(2) @JvmField val longMsgInfo: List<MsgInfoDef>? = null,
        @ProtoId(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf {
        @Serializable
internal class MsgInfoDef(
            @ProtoId(1) @JvmField val msgSeq: Int = 0,
            @ProtoId(2) @JvmField val longMsgId: Int = 0,
            @ProtoId(3) @JvmField val longMsgNum: Int = 0,
            @ProtoId(4) @JvmField val longMsgIndex: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class UinTypeUserDef(
        @ProtoId(1) @JvmField val fromUinType: Int = 0,
        @ProtoId(2) @JvmField val fromGroupCode: Long = 0L,
        @ProtoId(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf
}