/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

/*
 * 获取群历史消息
 */
internal object MessageSvcPbGetGroupMsg : OutgoingPacketFactory<MessageSvcPbGetGroupMsg.Response>("MessageSvc.PbGetGroupMsg") {
    sealed class Response : Packet

    class Failed(
        val result: Int,
        val errorMsg: String
    ) : Response()

    class Success(
        val msgElem: List<MsgComm.Msg>,
        val beginSequence: Long,
        val endSequence: Long,
    ) : Response()

    operator fun invoke(
        client: QQAndroidClient,
        groupUin: Long,
        messageSequence: Long,
        count: Int,
    ) = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            MsgSvc.PbGetGroupMsgReq.serializer(),
            MsgSvc.PbGetGroupMsgReq(
                groupCode = groupUin,
                beginSeq = messageSequence - count + 1,
                endSeq = messageSequence,

            )
        )
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        val resp = readProtoBuf(MsgSvc.PbGetGroupMsgResp.serializer())
        return if (resp.result != 0) {
            Failed(resp.result, resp.errmsg)
        } else {
            Success(
                msgElem = resp.msg,
                beginSequence = resp.returnBeginSeq,
                endSequence = resp.returnEndSeq
            )
        }
    }
}