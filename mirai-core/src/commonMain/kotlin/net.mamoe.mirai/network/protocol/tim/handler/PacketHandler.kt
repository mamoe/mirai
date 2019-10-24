package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import kotlin.reflect.KClass

/**
 * 数据包(接受/发送)处理器
 */
abstract class PacketHandler(
        val session: BotSession
) : CoroutineScope {
    abstract suspend fun onPacketReceived(packet: ServerPacket)

    interface Key<T : PacketHandler>

    open fun close() {

    }
}

internal class PacketHandlerNode<T : PacketHandler>(
        val clazz: KClass<T>,
        val instance: T,
        val key: PacketHandler.Key<T>
)

internal fun <T : PacketHandler> T.asNode(key: PacketHandler.Key<T>): PacketHandlerNode<T> {
    @Suppress("UNCHECKED_CAST")
    return PacketHandlerNode(this::class as KClass<T>, this, key)
}

internal open class PacketHandlerList : MutableList<PacketHandlerNode<*>> by mutableListOf() {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : PacketHandler> get(key: PacketHandler.Key<T>): T = this.first { it.key === key }.instance as T
}
