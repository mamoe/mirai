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
internal class Oidb0xf62 : ProtoBuf {
    @Serializable
    internal class RsqBody(
        @JvmField @ProtoNumber(1) var msg: Guild.ChannelMsgContent,
    ) : ProtoBuf {
        override fun toString(): String {
            return "RsqBody(msg=$msg)"
        }
    }

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) var result: Short? = null,
        @JvmField @ProtoNumber(2) var errmsg: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) var sendTime: Short? = null,
        @JvmField @ProtoNumber(4) var head: Guild.ChannelMsgHead? = null,
        @JvmField @ProtoNumber(5) var errType: Short? = null,
        @JvmField @ProtoNumber(6) var transSvrInfo: MsgSvc.TransSvrInfo? = null,
        @JvmField @ProtoNumber(7) var freqLimitInfo: Guild.ChannelFreqLimitInfo? = null,
        @JvmField @ProtoNumber(8) var body: Guild.MessageBody? = null,
    ) : ProtoBuf {
        override fun toString(): String {
            return "RspBody(result=$result, errmsg=${errmsg.decodeToString()}, sendTime=$sendTime, head=$head, errType=$errType, transSvrInfo=$transSvrInfo, freqLimitInfo=$freqLimitInfo, body=$body)"
        }
    }

}