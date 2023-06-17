/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.sso

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory

internal object TRpcRawPacket : OutgoingPacketFactory<TRpcRawPacket.RawData>("trpc.o3.*") {
    const val COMMAND_PREFIX = "trpc.o3."


    internal class RawData(
        val command: String,
        val data: ByteArray,
    ) : Packet

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RawData {
        error("")
    }

    fun decode(raw: RawIncomingPacket, body: ByteReadPacket): RawData =
        RawData(raw.commandName, body.readBytes())
}
