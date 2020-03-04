/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

class MsgRevokeUserDef : ProtoBuf {
    @Serializable
    class MsgInfoUserDef(
        @SerialId(1) val longMessageFlag: Int = 0,
        @SerialId(2) val longMsgInfo: List<MsgInfoDef>? = null,
        @SerialId(3) val fileUuid: List<String> = listOf()
    ) : ProtoBuf {
        @Serializable
        class MsgInfoDef(
            @SerialId(1) val msgSeq: Int = 0,
            @SerialId(2) val longMsgId: Int = 0,
            @SerialId(3) val longMsgNum: Int = 0,
            @SerialId(4) val longMsgIndex: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    class UinTypeUserDef(
        @SerialId(1) val fromUinType: Int = 0,
        @SerialId(2) val fromGroupCode: Long = 0L,
        @SerialId(3) val fileUuid: List<String> = listOf()
    ) : ProtoBuf
}