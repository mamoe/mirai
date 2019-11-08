@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.toUHexString

data class UnknownEventPacket(
    val id: UShort,
    val identity: EventPacketIdentity,
    val body: ByteReadPacket
) : EventPacket {
    override fun toString(): String = "UnknownEventPacket(id=${id.toUHexString()}, identity=$identity)"
}

//TODO This class should be declared with `inline`, but a CompilationException will be thrown
class UnknownEventParserAndHandler(override val id: UShort) : EventParserAndHandler<UnknownEventPacket> {

    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): UnknownEventPacket {
        MiraiLogger.debug("UnknownEventPacket(${id.toUHexString()}) = ${readBytes().toUHexString()}")
        return UnknownEventPacket(id, identity, this) //TODO the cause is that `this` reference.
    }

    override suspend fun BotNetworkHandler<*>.handlePacket(packet: UnknownEventPacket) {
        ByteArrayPool.useInstance {
            packet.body.readAvailable(it)
            bot.logger.debug("Unknown packet(${packet.id}) data = " + it.toUHexString())
        }
    }
}
