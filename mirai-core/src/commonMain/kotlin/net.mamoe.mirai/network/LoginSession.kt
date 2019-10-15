package net.mamoe.mirai.network

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocket
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.getGTK
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmSynthetic

/**
 * 登录会话. 当登录完成后, 客户端会拿到 sessionKey.
 * 此时建立 session, 然后开始处理事务.
 *
 * @author Him188moe
 */
class LoginSession(
        val bot: Bot,
        val sessionKey: ByteArray,//TODO 协议抽象? 可能并不是所有协议均需要 sessionKey
        val socket: DataPacketSocket,
        val scope: CoroutineScope
) {

    /**
     * Web api 使用
     */
    lateinit var cookies: String

    /**
     * Web api 使用
     */
    @ExperimentalStdlibApi
    var sKey: String = ""
        set(value) {
            field = value
            gtk = getGTK(value)
        }

    /**
     * Web api 使用
     */
    var gtk: Int = 0

    val isOpen: Boolean get() = socket.isOpen

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket].
     *
     * 实现方法:
     * ```kotlin
     * session.expectPacket<ServerPacketXXX> {
     *  toSend { ClientPacketXXX(...) }
     *  onExpect {//it: ServerPacketXXX
     *
     *  }
     * }
     * ```
     *
     * @param P 期待的包
     * @param handlerTemporary 处理器.
     */
    //@JvmSynthetic
    suspend inline fun <reified P : ServerPacket> expectPacket(handlerTemporary: TemporaryPacketHandler<P>.() -> Unit): CompletableJob {
        val job = coroutineContext[Job].takeIf { it != null }?.let { Job(it) } ?: Job()
        this.bot.network.addHandler(TemporaryPacketHandler(P::class, job, this).also(handlerTemporary))
        return job
    }

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket].
     * 发送成功后, 该方法会等待收到 [ServerPacket] 直到超时.
     * 由于包名可能过长, 可使用 `DataPacketSocket.expectPacket(PacketProcessor)` 替代.
     *
     * 实现方法:
     * ```kotlin
     * session.expectPacket<ServerPacketXXX>(ClientPacketXXX(...)) {//it: ServerPacketXXX
     *
     * }
     * ```
     *
     * @param P 期待的包
     * @param toSend 将要发送的包
     * @param handler 处理期待的包
     */
    @JvmSynthetic
    suspend inline fun <reified P : ServerPacket> expectPacket(toSend: ClientPacket, noinline handler: suspend (P) -> Unit): CompletableJob {
        val job = coroutineContext[Job].takeIf { it != null }?.let { Job(it) } ?: Job()
        this.bot.network.addHandler(TemporaryPacketHandler(P::class, job, this).also {
            it.toSend(toSend)
            it.onExpect(handler)
        })
        return job
    }
}


suspend fun LoginSession.distributePacket(packet: ServerPacket) = this.socket.distributePacket(packet)