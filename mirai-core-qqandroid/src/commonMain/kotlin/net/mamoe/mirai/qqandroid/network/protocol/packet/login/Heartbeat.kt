/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.login

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.*

internal class Heartbeat {

    object Alive : OutgoingPacketFactory<Alive.Response>("Heartbeat.Alive") {
        object Response : Packet {
            override fun toString(): String = "Heartbeat.Alive.Response"
        }

        operator fun invoke(
            client: QQAndroidClient
        ): OutgoingPacket = buildLoginOutgoingPacket(client, 0, key = NO_ENCRYPT) {
            writeSsoPacket(client, client.subAppId, commandName, sequenceId = it) {

            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response
        }
    }
}