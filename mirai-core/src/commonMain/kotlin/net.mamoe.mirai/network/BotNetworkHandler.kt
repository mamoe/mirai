package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.BotSocketAdapter
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.LoginHandler
import net.mamoe.mirai.network.protocol.tim.handler.*
import net.mamoe.mirai.network.protocol.tim.packet.HeartbeatPacket
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.BotNetworkConfiguration
import net.mamoe.mirai.utils.PlatformDatagramChannel
import kotlin.coroutines.ContinuationInterceptor

/**
 * Mirai 的网络处理器, 它承担所有数据包([Packet])的处理任务.
 * [BotNetworkHandler] 是全异步和线程安全的.
 *
 * [BotNetworkHandler] 由 2 个模块构成:
 * - [BotSocketAdapter]: 处理数据包底层的发送([ByteArray])
 * - [PacketHandler]: 制作 [OutgoingPacket] 并传递给 [BotSocketAdapter] 发送; 分析 [ServerPacket] 并处理
 *
 * 其中, [PacketHandler] 由 3 个子模块构成:
 * - [LoginHandler] 处理 sendTouch/login/verification code 相关
 * - [EventPacketHandler] 处理消息相关(群消息/好友消息)([ServerEventPacket])
 * - [ActionPacketHandler] 处理动作相关(踢人/加入群/好友列表等)
 *
 * A BotNetworkHandler is used to connect with Tencent servers.
 */
@Suppress("PropertyName")
interface BotNetworkHandler<Socket : DataPacketSocketAdapter> {
    /**
     * [BotNetworkHandler] 的协程作用域.
     * 所有 [BotNetworkHandler] 的协程均启动在此作用域下.
     *
     * [BotNetworkHandler] 的协程包含:
     * - UDP 包接收: [PlatformDatagramChannel.read]
     * - 心跳 Job [HeartbeatPacket]
     * - SKey 刷新 [RefreshSKeyRequestPacket]
     * - 所有数据包处理和发送
     *
     * [BotNetworkHandler.close] 时将会 [取消][kotlin.coroutines.CoroutineContext.cancelChildren] 所有此作用域下的协程
     */
    val NetworkScope: CoroutineScope

    val socket: Socket


    /**
     * 得到 [PacketHandler].
     * `get(EventPacketHandler)` 返回 [EventPacketHandler]
     * `get(ActionPacketHandler)` 返回 [ActionPacketHandler].
     *
     * 这个方法在 [PacketHandlerList] 中实现
     */
    operator fun <T : PacketHandler> get(key: PacketHandler.Key<T>): T

    /**
     * 依次尝试登录到可用的服务器. 在任一服务器登录完成后返回登录结果
     */
    suspend fun login(configuration: BotNetworkConfiguration): LoginResult

    /**
     * 添加一个临时包处理器, 并发送相应的包
     *
     * @see [BotSession.sendAndExpect] 发送并期待一个包
     * @see [TemporaryPacketHandler] 临时包处理器
     */
    suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*, *>)

    /**
     * 发送数据包
     */
    suspend fun sendPacket(packet: OutgoingPacket)

    fun close(cause: Throwable? = null) {
        //todo check??
        NetworkScope.coroutineContext[ContinuationInterceptor]!!.cancelChildren(CancellationException("handler closed", cause))
    }
}