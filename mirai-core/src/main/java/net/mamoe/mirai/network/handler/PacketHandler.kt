package net.mamoe.mirai.network.handler

import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.ServerPacket
import java.io.Closeable
import java.util.*
import kotlin.NoSuchElementException

/**
 * 数据包(接受/发送)处理器
 */
abstract class PacketHandler(
        val session: LoginSession
) : Closeable {
    abstract fun onPacketReceived(packet: ServerPacket)

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

class PacketHandlerList : LinkedList<PacketHandlerNode<*>>() {

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
