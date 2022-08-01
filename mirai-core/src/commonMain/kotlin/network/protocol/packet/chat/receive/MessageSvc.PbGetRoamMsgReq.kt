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
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.Either
import net.mamoe.mirai.utils.Either.Companion.mapRight
import net.mamoe.mirai.utils.FailureResponse
import net.mamoe.mirai.utils.checked

internal class CheckedResponse<T>(
    val value: Either<FailureResponse, T>
) : Packet

internal object MessageSvcPbGetRoamMsgReq :
    OutgoingPacketFactory<CheckedResponse<MessageSvcPbGetRoamMsgReq.Response>>("MessageSvc.PbGetRoamMsg") {

    class Response(
        val messages: List<MsgComm.Msg>?,
        val lastMessageTime: Long,
        val random: Long,
        val sig: ByteArray, // 似乎没被用到, 服务器每次返回不同
    ) {

    }

    fun createForFriend(
        client: QQAndroidClient,
        uin: Long,
        timeStart: Long,
        lastMsgTime: Long, // 上次 resp 中的, 否则为期待的 time end
        random: Long = 0,
        maxCount: Int = 1000,
        sig: ByteArray = EMPTY_BYTE_ARRAY, // 客户端每次请求相同
        pwd: ByteArray = EMPTY_BYTE_ARRAY,
    ) = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            MsgSvc.PbGetRoamMsgReq.serializer(), MsgSvc.PbGetRoamMsgReq(
                peerUin = uin,
                beginMsgtime = timeStart,
                lastMsgtime = lastMsgTime,
                checkPwd = 1, // always
                readCnt = maxCount,
                subcmd = 1,
                reqType = 1,
                sig = sig,
                pwd = pwd,
                random = random,
            )
        )
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CheckedResponse<Response> {
        val resp = readProtoBuf(MsgSvc.PbGetRoamMsgResp.serializer())
        if (resp.result == 1) return CheckedResponse(
            Either.right(
                Response(
                    null,
                    resp.lastMsgtime,
                    resp.random,
                    resp.sig,
                )
            )
        ) // finished
        return CheckedResponse(resp.checked().mapRight {
            Response(
                messages = resp.msg.asReversed(),
                lastMessageTime = resp.lastMsgtime,
                random = resp.random,
                sig = resp.sig,
            )
        })
    }
}