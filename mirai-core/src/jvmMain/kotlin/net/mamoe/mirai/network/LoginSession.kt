package net.mamoe.mirai.network

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocket
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.getGTK

/**
 * 登录会话. 当登录完成后, 客户端会拿到 sessionKey.
 * 此时建立 session, 然后开始处理事务.
 *
 * @author Him188moe
 */
class LoginSession(
        val bot: Bot,
        val sessionKey: ByteArray,
        val socket: DataPacketSocket
) {

    /**
     * Web api 使用
     */
    lateinit var cookies: String

    /**
     * Web api 使用
     */
    var sKey: String = ""
        set(value) {
            field = value
            gtk = getGTK(value)
        }

    /**
     * Web api 使用
     */
    var gtk: Int = 0


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
    @JvmSynthetic
    suspend inline fun <reified P : ServerPacket> expectPacket(handlerTemporary: TemporaryPacketHandler<P>.() -> Unit): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        this.bot.network.addHandler(TemporaryPacketHandler(P::class, deferred, this).also(handlerTemporary))
        return deferred
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
    suspend inline fun <reified P : ServerPacket> expectPacket(toSend: ClientPacket, noinline handler: suspend (P) -> Unit): CompletableDeferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        this.bot.network.addHandler(TemporaryPacketHandler(P::class, deferred, this).also {
            it.toSend(toSend)
            it.onExpect(handler)
        })
        return deferred
    }
}