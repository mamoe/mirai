package net.mamoe.mirai.network.protocol.tim.handler

import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import kotlin.reflect.KClass

/**
 * 数据包(接受/发送)处理器
 */
abstract class PacketHandler(
        val session: LoginSession
) {
    abstract suspend fun onPacketReceived(packet: ServerPacket)

    open fun close() {

    }
}

class PacketHandlerNode<T : PacketHandler>(
        val clazz: KClass<T>,
        val instance: T
)

fun <T : PacketHandler> T.asNode(): PacketHandlerNode<T> {
    @Suppress("UNCHECKED_CAST")
    return PacketHandlerNode(this::class as KClass<T>, this)
}

class PacketHandlerList : MutableList<PacketHandlerNode<*>> by mutableListOf() {

    operator fun <T : PacketHandler> get(clazz: KClass<T>): T {
        this.forEach {
            if (it.clazz == clazz) {
                @Suppress("UNCHECKED_CAST")
                return@get it.instance as T
            }
        }

        throw NoSuchElementException()
    }
}
