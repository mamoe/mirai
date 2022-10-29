/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.Either

internal object MessageSvcPbGetGroupMsgReq :
    OutgoingPacketFactory<CheckedResponse<MsgSvc.PbGetGroupMsgResp>>("MessageSvc.PbGetRoamMsg") {
    fun createForGroup(
        client: QQAndroidClient,
        beginSeq: Long,
        endSeq: Long,
        group: Long
    ) = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            MsgSvc.PbGetGroupMsgReq.serializer(), MsgSvc.PbGetGroupMsgReq(
                groupCode = group,
                beginSeq = beginSeq,
                endSeq = endSeq
            )
        )
    }
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CheckedResponse<MsgSvc.PbGetGroupMsgResp> {
        var resp = readProtoBuf(MsgSvc.PbGetGroupMsgResp.serializer())
        if (resp.msg.isNotEmpty()) {
            // read messages
            return CheckedResponse(Either.right(resp))
        }
        TODO("不会了")
    }

}