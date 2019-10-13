package net.mamoe.mirai.network.protocol.tim.handler

import kotlinx.io.core.Closeable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.ServerPacketReceivedEvent
import net.mamoe.mirai.network.LoginSession
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.MiraiDatagramChannel

/**
 * 网络接口.
 * 发包 / 处理包.
 * 仅可通过 [TIMBotNetworkHandler.socket] 得到实例.
 *
 * @author Him188moe
 */
interface DataPacketSocket : Closeable {
    val owner: Bot

    val serverIp: String

    /**
     * UDP 通道
     */
    val channel: MiraiDatagramChannel

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
     * @see [LoginSession.expectPacket] kotlin DSL
     */
    suspend fun sendPacket(packet: ClientPacket)

    override fun close()
}