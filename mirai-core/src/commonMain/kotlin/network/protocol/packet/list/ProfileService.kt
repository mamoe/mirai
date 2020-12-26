/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.GroupMngReqJce
import net.mamoe.mirai.internal.network.protocol.data.jce.GroupMngRes
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceStruct
import net.mamoe.mirai.utils.toByteArray

internal class ProfileService {
    object GroupMngReq : OutgoingPacketFactory<GroupMngReq.GroupMngReqResponse>("ProfileService.GroupMngReq") {
        data class GroupMngReqResponse(val errorCode: Int, val errorMessage: String) : Packet

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GroupMngReqResponse {
            val resp = readUniPacket(GroupMngRes.serializer())
            return GroupMngReqResponse(resp.errorCode.toInt(), resp.errorString)
        }

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long
        ): OutgoingPacket = buildOutgoingUniPacket(client) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    servantName = "KQQ.ProfileService.ProfileServantObj",
                    funcName = "GroupMngReq",
                    requestId = client.nextRequestPacketRequestId(),
                    sBuffer = jceRequestSBuffer(
                        "GroupMngReq",
                        GroupMngReqJce.serializer(),
                        GroupMngReqJce(
                            reqtype = 2,
                            uin = client.uin,
                            vecBody = client.uin.shl(32).or(groupCode).toByteArray()
                        )
                    )
                )
            )
        }


    }
}