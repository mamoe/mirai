/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.summarycard

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.ChangeFriendNameReq
import net.mamoe.mirai.internal.network.protocol.data.jce.ChangeFriendNameRes
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceRequestPacket

internal object ChangeFriendRemark :
    OutgoingPacketFactory<ChangeFriendRemark.Response>("ProfileService.ChangeFriendName") {
    class Response(val isSuccess: Boolean, val resultCode: Int) : Packet {
        override fun toString(): String {
            return "ProfileService.ChangeFriendName.Response(isSuccess=$isSuccess, resultCode=$resultCode)"
        }
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        val res = this.readUniPacket(ChangeFriendNameRes.serializer())
        return Response(res.result == 0x0.toByte(), res.result.toInt())
    }

    operator fun invoke(
        client: QQAndroidClient,
        id: Long,
        newRemark: String
    ): OutgoingPacketWithRespType<Response> {
        return buildOutgoingUniPacket(client) {
            writeJceRequestPacket(
                servantName = "KQQ.ProfileService.ProfileServantObj",
                funcName = "ChangeFriendName",
                name = "req",
                serializer = ChangeFriendNameReq.serializer(),
                body = ChangeFriendNameReq(id, newRemark)
            )
        }
    }

}