@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.handler

import kotlinx.io.core.Closeable
import net.mamoe.mirai.Bot
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

    override fun close()
}