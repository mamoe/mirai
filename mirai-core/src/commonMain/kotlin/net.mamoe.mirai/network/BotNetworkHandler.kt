package net.mamoe.mirai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.io.core.Closeable
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.BotSocket
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.LoginHandler
import net.mamoe.mirai.network.protocol.tim.handler.*
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.ClientSKeyRefreshmentRequestPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.LoginConfiguration
import net.mamoe.mirai.utils.MiraiDatagramChannel

/**
 * Mirai 的网络处理器, 它承担所有数据包([Packet])的处理任务.
 * [BotNetworkHandler] 是全异步和线程安全的.
 *
 * [BotNetworkHandler] 由 2 个模块构成:
 * - [BotSocket]: 处理数据包底层的发送([ByteArray])
 * - [PacketHandler]: 制作 [ClientPacket] 并传递给 [BotSocket] 发送; 分析 [ServerPacket] 并处理
 *
 * 其中, [PacketHandler] 由 3 个子模块构成:
 * - [LoginHandler] 处理 sendTouch/login/verification code 相关
 * - [EventPacketHandler] 处理消息相关(群消息/好友消息)([ServerEventPacket])
 * - [ActionPacketHandler] 处理动作相关(踢人/加入群/好友列表等)
 *
 * A BotNetworkHandler is used to connect with Tencent servers.
 */
interface BotNetworkHandler<Socket : DataPacketSocket> : Closeable {
    /**
     * [BotNetworkHandler] 的协程作用域.
     * 所有 [BotNetworkHandler] 的协程均启动在此作用域下.
     *
     * [BotNetworkHandler] 的协程包含:
     * - UDP 包接收: [MiraiDatagramChannel.read]
     * - 心跳 Job [ClientHeartbeatPacket]
     * - SKey 刷新 [ClientSKeyRefreshmentRequestPacket]
     * - 所有数据包处理和发送
     *
     * [BotNetworkHandler.close] 时将会 [取消][CoroutineScope.cancel] 所有此作用域下的协程
     */
    val NetworkScope: CoroutineScope

    var socket: Socket

    /**
     * 事件处理. 如发送好友消息, 接受群消息并触发事件
     */
    val event: EventPacketHandler

    /**
     * 动作处理. 如发送好友请求, 处理别人发来的好友请求等
     */
    val action: ActionPacketHandler

    /**
     * [PacketHandler] 列表
     */
    val packetHandlers: PacketHandlerList

    /**
     * 尝试登录. 将会依次尝试登录到可用的服务器. 在任一服务器登录完成后返回登录结果
     */
    suspend fun login(configuration: LoginConfiguration): LoginResult

    /**
     * 添加一个临时包处理器
     *
     * @see [TemporaryPacketHandler]
     */
    suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*>)

    /**
     * 发送数据包
     */
    suspend fun sendPacket(packet: ClientPacket)

    override fun close() {
        NetworkScope.cancel("handler closed", HandlerClosedException())
    }
}