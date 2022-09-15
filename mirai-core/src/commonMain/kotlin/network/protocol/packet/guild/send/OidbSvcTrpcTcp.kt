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
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf551
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf5d1
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeOidb

internal object OidbSvcTrpcTcp {
    //获取频道的子频道列表
    internal object FetchChannelList : OutgoingPacketFactory<FetchChannelList.Response>("OidbSvcTrpcTcp.0xf5d_1") {

        internal class Response(
            val origin: Oidb0xf5d.ChannelOidb0xf5dRsp
        ) : Packet {
            override fun toString(): String = "Response(OidbSvcTrpcTcp.0xf5d_1)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readProtoBuf(Oidb0xf5d.ChannelOidb0xf5dRsp.serializer())
            return Response(res)
        }

        operator fun invoke(
            client: QQAndroidClient,
            guildId: Long
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                3933,
                1,
                Oidb0xf5d1.Req.serializer(),
                Oidb0xf5d1.Req(
                    guildId
                )
            )
        }
    }

    //获取子频道信息
    internal object FetchChannelInfo : OutgoingPacketFactory<FetchChannelInfo.Response>("OidbSvcTrpcTcp.0xf55_1") {
        internal class Response(
            val origin: Oidb0xf551.Data
        ) : Packet {
            override fun toString(): String = "Response(OidbSvcTrpcTcp.0xf55_1)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readProtoBuf(Oidb0xf551.Data.serializer())
            return Response(res)
        }

        operator fun invoke(
            client: QQAndroidClient,
            guildId: Long,
            channelId: Long,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                3925,
                1,
                Oidb0xf551.Req.serializer(),
                Oidb0xf551.Req(
                    guildId,
                    channelId
                )
            )
        }
    }

    // FetchGuildMemberListWithRole 获取频道成员列表
    // 第一次请求: startIndex = 0 , roleIdIndex = 2 param = ""
    // 后续请求请根据上次请求的返回值进行设置
    internal object FetchGuildMemberListWithRole :
        OutgoingPacketFactory<FetchGuildMemberListWithRole.Response>("OidbSvcTrpcTcp.0xf5b_1") {
        internal class Response(
            val origin: Oidb0xf5b1.Rsp
        ) : Packet {
            override fun toString(): String = "Response(OidbSvcTrpcTcp.0xf5b_1)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readProtoBuf(Oidb0xf5b1.Rsp.serializer())
            return Response(res)
        }

        operator fun invoke(
            client: QQAndroidClient,
            guildId: Long,
            channelId: Long,
            startIndex: Short,
            roleIdIndex: Long,
            param: String
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                3931,
                1,
                Oidb0xf5b1.Req.serializer(),
                Oidb0xf5b1.Req(
                    guildId = guildId,
                    channelId = channelId,
                    startIndex = startIndex,
                    roleIdIndex = roleIdIndex,
                    param = param
                )
            )
        }
    }

    //获取频道信息
    internal object FetchGuestGuild : OutgoingPacketFactory<FetchGuestGuild.Response>("OidbSvcTrpcTcp.0xf57_9") {
        internal class Response(
            val origin: Oidb0xf57.Rsp
        ) : Packet {
            override fun toString(): String = "Response(OidbSvcTrpcTcp.0xf57_9)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readProtoBuf(Oidb0xf57.Rsp.serializer())
            return Response(res)
        }

        operator fun invoke(
            client: QQAndroidClient,
            guildId: Long,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                3927,
                9,
                Oidb0xf579.Req.serializer(),
                Oidb0xf579.Req(
                    param = Oidb0xf579.Param(
                        guildId
                    )
                )
            )
        }
    }
}