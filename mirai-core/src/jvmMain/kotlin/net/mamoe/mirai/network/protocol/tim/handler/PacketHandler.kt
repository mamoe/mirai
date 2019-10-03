package net.mamoe.mirai.network.protocol.tim.handler

import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import java.io.Closeable

/**
 * 数据包(接受/发送)处理器
 */
abstract class PacketHandler(
        val session: LoginSession
) : Closeable {
    abstract suspend fun onPacketReceived(packet: ServerPacket)

    override fun close() {

    }
}

class PacketHandlerNode<T : PacketHandler>(
        val clazz: Class<T>,
        val instance: T
)

fun PacketHandler.asNode(): PacketHandlerNode<PacketHandler> {
    return PacketHandlerNode(this.javaClass, this)
}

class PacketHandlerList : MutableList<PacketHandlerNode<*>> by mutableListOf() {

    fun <T : PacketHandler> get(clazz: Class<T>): T {
        this.forEach {
            if (it.clazz == clazz) {
                @Suppress("UNCHECKED_CAST")
                return@get it.instance as T
            }
        }

        throw NoSuchElementException()
    }
}
