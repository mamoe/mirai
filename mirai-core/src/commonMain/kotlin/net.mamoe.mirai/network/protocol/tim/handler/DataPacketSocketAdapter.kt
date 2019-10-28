package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.io.core.Closeable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.ServerPacketReceivedEvent
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.io.PlatformDatagramChannel

/**
 * 网络接口.
 * 发包 / 处理包.
 * 仅可通过 [TIMBotNetworkHandler.socket] 得到实例.
 *
 * @author Him188moe
 */
interface DataPacketSocketAdapter : Closeable {
    val owner: Bot

    /**
     * 连接的服务器的 IPv4 地址
     * 在整个过程中都不会变化. 若连接丢失, [DataPacketSocketAdapter] 将会被 [close]
     */
    val serverIp: String

    /**
     * UDP 通道
     */
    val channel: PlatformDatagramChannel

    /**
     * 是否开启
     */
    val isOpen: Boolean

    /**
     * 分发数据包给 [PacketHandler]
     */
    suspend fun distributePacket(packet: ServerPacket)

    /**
     * 发送一个数据包(非异步).
     *
     * 可通过 hook 事件 [ServerPacketReceivedEvent] 来获取服务器返回.
     *
     * @see [BotSession.sendAndExpect] kotlin DSL
     */
    suspend fun sendPacket(packet: OutgoingPacket)

    override fun close()
}