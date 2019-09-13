package net.mamoe.mirai.network.handler

import net.mamoe.mirai.network.BotNetworkHandlerImpl
import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.ServerPacket
import java.io.Closeable

/**
 * 网络接口.
 * 发包 / 处理包.
 * 仅可通过 [BotNetworkHandlerImpl.socket] 得到实例.
 *
 * @author Him188moe
 */
interface DataPacketSocket : Closeable {

    fun distributePacket(packet: ServerPacket)


    fun sendPacket(packet: ClientPacket)

    fun isClosed(): Boolean

    override fun close()
}