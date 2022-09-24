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
internal class GuildMsg : ProtoBuf {
    @Serializable
    internal class MsgOnlinePush(
        @ProtoNumber(1) @JvmField val msgs: List<Guild.ChannelMsgContent> = mutableListOf(),
        @ProtoNumber(2) @JvmField val generalFlag: Short = 0,
        @ProtoNumber(3) @JvmField val needResp: Short = 0,
        @ProtoNumber(4) @JvmField val serverBuf: ByteArray = EMPTY_BYTE_ARRAY,//EMPTY_BYTE_ARRAY
        @ProtoNumber(5) @JvmField val compressFlag: Short = 0,
        @ProtoNumber(6) @JvmField val compressMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val focusInfo: FocusInfo = FocusInfo(),
        @ProtoNumber(8) @JvmField val hugeFlag: Short = 0,
    ) : ProtoBuf {
        override fun toString(): String {
            return "MsgOnlinePush(msgs=$msgs, generalFlag=$generalFlag, needResp=$needResp, serverBuf=${serverBuf.contentToString()}, compressFlag=$compressFlag, compressMsg=${compressMsg.contentToString()}, focusInfo=$focusInfo, hugeFlag=$hugeFlag)"
        }
    }


    @Serializable
    internal class FocusInfo(
        @ProtoNumber(1) @JvmField val channelIdList: List<Long> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "FocusInfo(channelIdList=$channelIdList)"
        }
    }

    @Serializable
    internal class PressMsg(
        @ProtoNumber(1) @JvmField val msgs: List<Guild.ChannelMsgContent> = mutableListOf(),
    ) : ProtoBuf {
        override fun toString(): String {
            return "PressMsg(msgs=$msgs)"
        }
    }
}