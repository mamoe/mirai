/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0xed3
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal object NudgePacket : OutgoingPacketFactory<NudgePacket.Response>("OidbSvc.0xed3") {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        with(readBytes().loadAs(OidbSso.OIDBSSOPkg.serializer())) {
            return Response(result == 0, result)
        }
    }

    class Response(val success: Boolean, val code: Int) : Packet {
        override fun toString(): String = "NudgeResponse(success=$success,code=$code)"
    }

    fun friendInvoke(
        client: QQAndroidClient,
        nudgeTargetId: Long,
        messageReceiverUin: Long,
    ): OutgoingPacket {
        return buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(),
                OidbSso.OIDBSSOPkg(
                    command = 3795,
                    serviceType = 1,
                    result = 0,
                    bodybuffer = Cmd0xed3.ReqBody(
                        toUin = nudgeTargetId,
                        aioUin = messageReceiverUin
                    ).toByteArray(Cmd0xed3.ReqBody.serializer())
                )
            )
        }
    }

    fun troopInvoke(
        client: QQAndroidClient,
        messageReceiverGroupCode: Long,
        nudgeTargetId: Long,
    ): OutgoingPacket {
        return buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(),
                OidbSso.OIDBSSOPkg(
                    command = 3795,
                    serviceType = 1,
                    result = 0,
                    bodybuffer = Cmd0xed3.ReqBody(
                        toUin = nudgeTargetId,
                        groupCode = messageReceiverGroupCode
                    ).toByteArray(Cmd0xed3.ReqBody.serializer())
                )
            )
        }
    }

}