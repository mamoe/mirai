package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.timpc.handler.DataPacketSocketAdapter
import net.mamoe.mirai.network.protocol.timpc.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.timpc.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.timpc.packet.Packet
import net.mamoe.mirai.network.protocol.timpc.packet.login.HeartbeatPacket
import net.mamoe.mirai.network.protocol.timpc.packet.login.RequestSKeyPacket
import net.mamoe.mirai.network.protocol.timpc.packet.login.LoginResult
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.PlatformDatagramChannel

/**
 * Mirai 的网络处理器, 它承担所有数据包([Packet])的处理任务.
 * [BotNetworkHandler] 是线程安全的.
 *
 * NetworkHandler 实现接口 [CoroutineScope]
 * 即 [BotNetworkHandler] 自己就是作用域.
 * 所有 [BotNetworkHandler] 的协程均启动在此作用域下.
 *
 * [BotNetworkHandler] 的协程包含:
 * - UDP 包接收: [PlatformDatagramChannel.read]
 * - 心跳 Job [HeartbeatPacket]
 * - SKey 刷新 [RequestSKeyPacket]
 * - 所有数据包处理和发送
 *
 * [BotNetworkHandler.close] 时将会 [取消][kotlin.coroutines.CoroutineContext.cancelChildren] 所有此作用域下的协程
 *
 * A BotNetworkHandler is used to connect with Tencent servers.
 */
@Suppress("PropertyName")
interface BotNetworkHandler<Socket : DataPacketSocketAdapter> : CoroutineScope {
    val socket: Socket
    val bot: Bot

    val supervisor: CompletableJob

    val session: BotSession

    /**
     * 依次尝试登录到可用的服务器. 在任一服务器登录完成后返回登录结果
     * 本函数将挂起直到登录成功.
     */
    suspend fun login(): LoginResult

    /**
     * 等待直到与服务器断开连接. 若未连接则立即返回
     */
    suspend fun awaitDisconnection()

    /**
     * 关闭网络接口, 停止所有有关协程和任务
     */
    fun close(cause: Throwable? = null) {
        supervisor.cancel(CancellationException("handler closed", cause))
    }
}