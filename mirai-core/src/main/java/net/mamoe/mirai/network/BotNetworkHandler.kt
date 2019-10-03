package net.mamoe.mirai.network

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.BotSocket
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler.Login
import net.mamoe.mirai.network.protocol.tim.handler.ActionPacketHandler
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocket
import net.mamoe.mirai.network.protocol.tim.handler.MessagePacketHandler
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginState
import java.io.Closeable

/**
 * Mirai 的网络处理器, 它承担所有数据包([Packet])的处理任务.
 * [BotNetworkHandler] 是全异步和线程安全的.
 *
 * [BotNetworkHandler] 由 2 个模块构成:
 * - [BotSocket]: 处理数据包底层的发送([ByteArray])
 * - [PacketHandler]: 制作 [ClientPacket] 并传递给 [BotSocket] 发送; 分析 [ServerPacket] 并处理
 *
 * 其中, [PacketHandler] 由 4 个子模块构成:
 * - [DebugPacketHandler] 输出 [Packet.toString]
 * - [Login] 处理 touch/login/verification code 相关
 * - [MessagePacketHandler] 处理消息相关(群消息/好友消息)([ServerEventPacket])
 * - [ActionPacketHandler] 处理动作相关(踢人/加入群/好友列表等)
 *
 * A BotNetworkHandler is used to connect with Tencent servers.
 *
 * @author Him188moe
 */
interface BotNetworkHandler : Closeable {
    /**
     * 网络层处理器. 用于编码/解码 [Packet], 发送/接受 [ByteArray]
     *
     * java 调用方式: `botNetWorkHandler.getSocket()`
     */
    val socket: DataPacketSocket

    /**
     * 消息处理. 如发送好友消息, 接受群消息并触发事件
     *
     * java 调用方式: `botNetWorkHandler.getMessage()`
     */
    val message: MessagePacketHandler

    /**
     * 动作处理. 如发送好友请求, 处理别人发来的好友请求等
     *
     * java 调用方式: `botNetWorkHandler.getAction()`
     */
    val action: ActionPacketHandler

    /**
     * 尝试登录
     *
     * @param touchingTimeoutMillis 连接每个服务器的 timeout
     */
    fun tryLogin(touchingTimeoutMillis: Long = 200): CompletableDeferred<LoginState>

    /**
     * 添加一个临时包处理器
     *
     * @see [TemporaryPacketHandler]
     */
    fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*>)

    override fun close()
}