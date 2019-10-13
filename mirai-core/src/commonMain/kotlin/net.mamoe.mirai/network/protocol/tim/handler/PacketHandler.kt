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

    interface Key<T : PacketHandler>

    open fun close() {

    }
}

class PacketHandlerNode<T : PacketHandler>(
        val clazz: KClass<T>,
        val instance: T,
        val key: PacketHandler.Key<T>
)

fun <T : PacketHandler> T.asNode(key: PacketHandler.Key<T>): PacketHandlerNode<T> {
    @Suppress("UNCHECKED_CAST")
    return PacketHandlerNode(this::class as KClass<T>, this, key)
}

open class PacketHandlerList : MutableList<PacketHandlerNode<*>> by mutableListOf() {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : PacketHandler> get(key: PacketHandler.Key<T>): T = this.first { it.key === key }.instance as T

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
