package net.mamoe.mirai.network.handler

import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.ServerPacket
import java.io.Closeable

/**
 * @author Him188moe
 */
interface DataPacketSocket : Closeable {

    fun distributePacket(packet: ServerPacket)

    @ExperimentalUnsignedTypes
    fun sendPacket(packet: ClientPacket)

    fun isClosed(): Boolean

    override fun close()
}