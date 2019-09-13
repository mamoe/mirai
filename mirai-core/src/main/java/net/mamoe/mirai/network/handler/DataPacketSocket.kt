package net.mamoe.mirai.network.handler

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.network.ServerPacketReceivedEvent
import net.mamoe.mirai.network.BotNetworkHandlerImpl
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.packet.ClientPacket
import net.mamoe.mirai.network.packet.ServerPacket
import net.mamoe.mirai.task.MiraiThreadPool
import java.io.Closeable
import java.util.concurrent.Future

/**
 * 网络接口.
 * 发包 / 处理包.
 * 仅可通过 [BotNetworkHandlerImpl.socket] 得到实例.
 *
 * @author Him188moe
 */
interface DataPacketSocket : Closeable {
    fun getOwner(): Bot

    /**
     * 分发数据包给 [PacketHandler]
     */
    fun distributePacket(packet: ServerPacket)

    /**
     * 发送一个数据包(非异步).
     *
     * 可通过 hook 事件 [ServerPacketReceivedEvent] 来获取服务器返回.
     *
     * @see [LoginSession.expectPacket] kotlin DSL
     */
    fun sendPacket(packet: ClientPacket)

    /**
     * 发送一个数据包(异步).
     *
     * 可通过 hook 事件 [ServerPacketReceivedEvent] 来获取服务器返回.
     *
     * @see [LoginSession.expectPacket] kotlin DSL
     */
    fun sendPacketAsync(packet: ClientPacket): Future<*> {
        return MiraiThreadPool.getInstance().submit {
            sendPacket(packet)
        }
    }

    fun isClosed(): Boolean

    override fun close()
}