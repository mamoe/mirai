/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0xeac
import net.mamoe.mirai.internal.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

/**
 * 群精华消息管理
 *
 * */

internal class TroopEssenceMsgManager {

    internal data class Response(val success: Boolean, val msg: String?) : Packet

    internal object SetEssence : OutgoingPacketFactory<Response>("OidbSvc.0xeac_1") {

        operator fun invoke(
            client: QQAndroidClient,
            troopUin: Long,
            msgRandom: Int,
            msgSeq: Int
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3756,
                    result = 0,
                    serviceType = 1,
                    bodybuffer = Oidb0xeac.ReqBody(
                        groupCode = troopUin,
                        msgSeq = msgSeq.and(-1),
                        msgRandom = msgRandom
                    ).toByteArray(Oidb0xeac.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            readProtoBuf(OidbSso.OIDBSSOPkg.serializer()).let { pkg ->
                pkg.bodybuffer.loadAs(Oidb0xeac.RspBody.serializer()).let { data ->
                    return Response(data.errorCode == 0, data.wording)
                }
            }

        }
    }

    internal object RemoveEssence : OutgoingPacketFactory<Response>("OidbSvc.0xeac_2") {

        operator fun invoke(
            client: QQAndroidClient,
            troopUin: Long,
            msgRandom: Int,
            msgSeq: Int
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                OidbSso.OIDBSSOPkg.serializer(), OidbSso.OIDBSSOPkg(
                    command = 3756,
                    result = 0,
                    serviceType = 1,
                    bodybuffer = Oidb0xeac.ReqBody(
                        groupCode = troopUin,
                        msgSeq = msgSeq.and(-1),
                        msgRandom = msgRandom
                    ).toByteArray(Oidb0xeac.ReqBody.serializer()),
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            readProtoBuf(OidbSso.OIDBSSOPkg.serializer()).let { pkg ->
                pkg.bodybuffer.loadAs(Oidb0xeac.RspBody.serializer()).let { data ->
                    return Response(data.errorCode == 0, data.wording)
                }
            }

        }
    }
}