/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.recording

import net.mamoe.mirai.internal.network.components.PacketHandler
import net.mamoe.mirai.internal.network.components.RawIncomingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.TimeSource

internal class RecordingPacketHandler : PacketHandler {
    private val startTime = TimeSource.Monotonic.markNow()
    private val logger = MiraiLogger.create("recorder")
    val records = ConcurrentLinkedQueue<PacketRecord>()

    override suspend fun handlePacket(rawIncomingPacket: RawIncomingPacket) {
        logger.info { "Record recv: ${rawIncomingPacket.commandName} ${rawIncomingPacket.sequenceId}" }
        records.add(
            PacketRecord(
                timeFromStart = startTime.elapsedNow(),
                data = rawIncomingPacket,
            )
        )
    }

    override suspend fun handlePacket(outgoingPacket: OutgoingPacket) {
        logger.info { "Ignore send: ${outgoingPacket.commandName} ${outgoingPacket.sequenceId}" }
    }

}

//
//internal object OutgoingPacketSerializer : KSerializer<OutgoingPacket> by Surrogate.serializer().map(
//    Surrogate.serializer().descriptor,
//    deserialize = { OutgoingPacket(name, commandName) }
//) {
//    @SerialName("OutgoingPacket")
//    @Serializable
//    private class Surrogate(
//        val commandName: String,
//        val name: String,
//        val sequenceId: Long,
//        val delegate: ByteArray
//    )
//
//}

//internal enum class PacketDirection {
//    SERVER_TO_CLIENT,
//    CLIENT_TO_SERVER,
//}

