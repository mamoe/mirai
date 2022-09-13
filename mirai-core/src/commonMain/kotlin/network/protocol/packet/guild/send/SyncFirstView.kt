/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.guild.send

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.FirstViewReq
import net.mamoe.mirai.internal.network.protocol.data.proto.FirstViewResp
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal object SyncFirstView : OutgoingPacketFactory<SyncFirstView.Response>("trpc.group_pro.synclogic.SyncLogic.SyncFirstView") {

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
        val res = this.readProtoBuf(FirstViewResp.serializer())
        //TODO GuildService.GetUserProfile
        return Response(
            res
        )
    }

    internal class Response(
        val origin: FirstViewResp
    ) : Packet {
        override fun toString(): String = "Response(trpc.group_pro.synclogic.SyncLogic.SyncFirstView)"
    }

    operator fun invoke(
        client: QQAndroidClient
    ) = buildOutgoingUniPacket(client) {
        writeProtoBuf(
            FirstViewReq.serializer(),
            FirstViewReq(
                0,0,1
            )
        )
    }
}