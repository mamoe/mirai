/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf90
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xf8e
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

/**
 * 群待办管理
 *
 * */
internal class TroopTodoManager {

    // region OidbSvcTrpcTcp.0xf8e

    class Fetch(
        val pkg: OidbSso.OIDBSSOPkg,
    ) : Packet {
        val body by lazy {
            pkg.bodybuffer.loadAs(Oidb0xf8e.RspBody.serializer())
        }

        override fun toString(): String =
            "TroopTodoManager.Fetch(success=${pkg.result == 0}, error=${pkg.errorMsg})"
    }

    internal object Current : OutgoingPacketFactory<Fetch>("OidbSvcTrpcTcp.0xf8e_1") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3982,
                    serviceType = 1,
                    bodybuffer = Oidb0xf8e.ReqBody(
                        groupCode = groupCode
                    ).toByteArray(Oidb0xf8e.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Fetch {
            return Fetch(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    internal object Status : OutgoingPacketFactory<Fetch>("OidbSvcTrpcTcp.0xf8e_2") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3982,
                    serviceType = 2,
                    bodybuffer = Oidb0xf8e.ReqBody(
                        groupCode = groupCode
                    ).toByteArray(Oidb0xf8e.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Fetch {
            return Fetch(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    // endregion

    // region OidbSvcTrpcTcp.0xf90

    class Response(
        val pkg: OidbSso.OIDBSSOPkg,
    ) : Packet {
        val info by lazy {
            pkg.bodybuffer.loadAs(Oidb0xf90.RspBody.serializer()).info
        }

        override fun toString(): String =
            "TroopTodoManager.Response(success=${pkg.result == 0}, error=${pkg.errorMsg})"
    }

    internal object SetTodo : OutgoingPacketFactory<Response>("OidbSvcTrpcTcp.0xf90_1") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            msgRandom: Long,
            msgSeq: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3984,
                    serviceType = 1,
                    bodybuffer = Oidb0xf90.ReqBody(
                        groupCode = groupCode,
                        seq = msgSeq,
                        random = msgRandom,
                    ).toByteArray(Oidb0xf90.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    internal object CompleteTodo : OutgoingPacketFactory<Response>("OidbSvcTrpcTcp.0xf90_2") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            msgRandom: Long,
            msgSeq: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3984,
                    serviceType = 2,
                    bodybuffer = Oidb0xf90.ReqBody(
                        groupCode = groupCode,
                        seq = msgSeq,
                        random = msgRandom,
                    ).toByteArray(Oidb0xf90.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    internal object RecallTodo : OutgoingPacketFactory<Response>("OidbSvcTrpcTcp.0xf90_3") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            msgRandom: Long,
            msgSeq: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3984,
                    serviceType = 3,
                    bodybuffer = Oidb0xf90.ReqBody(
                        groupCode = groupCode,
                        seq = msgSeq,
                        random = msgRandom,
                    ).toByteArray(Oidb0xf90.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    internal object CloseTodo : OutgoingPacketFactory<Response>("OidbSvcTrpcTcp.0xf90_4") {

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            msgRandom: Long,
            msgSeq: Long
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3984,
                    serviceType = 4,
                    bodybuffer = Oidb0xf90.ReqBody(
                        groupCode = groupCode,
                        seq = msgSeq,
                        random = msgRandom,
                    ).toByteArray(Oidb0xf90.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response(readProtoBuf(OidbSso.OIDBSSOPkg.serializer()))
        }
    }

    // endregion
}