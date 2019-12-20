@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.toUHexString

internal data class UnknownEventPacket(
    val id: UShort,
    val identity: EventPacketIdentity,
    val body: ByteReadPacket
) : EventPacket {
    override fun toString(): String = "UnknownEventPacket(id=${id.toUHexString()}, identity=$identity)\n = ${body.readBytes().toUHexString()}"
}

/*
被好友拉入群 (已经进入)
Mirai 21:54:15 : Packet received: UnknownEventPacket(id=00 57, identity=(920503456->1994701021))
 = 00 00 00 08 00 0A 00 04 01 00 00 00 36 DD C4 A0 01 04 00 00 00 00 3E 03 3F A2 00 00 20 E5 96 01 BC 23 AE 03 C7 B8 9F BE B3 E5 E4 77 A9 0E FD 2B 7C 64 8B C0 5F 29 8B D7 DC 85 7E 44 7B 00 30 33 65 62 61 62 31 31 66 63 63 61 34 63 38 39 31 36 31 33 37 37 65 65 62 36 63 32 39 37 31 33 34 32 35 62 64 30 34 66 62 31 61 31 65 37 31 63 33
 */

//TODO This class should be declared with `inline`, but a CompilationException will be thrown
internal class UnknownEventParserAndHandler(override val id: UShort) : EventParserAndHandler<UnknownEventPacket> {

    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): UnknownEventPacket {
        // MiraiLogger.debug("UnknownEventPacket(${id.toHexString()}) = ${readBytes().toHexString()}")
        return UnknownEventPacket(id, identity, this) //TODO the cause is that `this` reference.
    }

    override suspend fun BotNetworkHandler.handlePacket(packet: UnknownEventPacket) {
        ByteArrayPool.useInstance {
            packet.body.readAvailable(it)
            bot.logger.debug("Unknown packet(${packet.id}) data = " + it.toUHexString())
        }
    }
}
