@file:Suppress("MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.handler.ActionPacketHandler
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocketAdapter
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.getGTK
import kotlin.coroutines.coroutineContext

internal fun TIMBotNetworkHandler.BotSession(
    bot: Bot,
    sessionKey: ByteArray,
    socket: DataPacketSocketAdapter
) = BotSession(bot, sessionKey, socket, this)

/**
 * 登录会话. 当登录完成后, 客户端会拿到 sessionKey.
 * 此时建立 session, 然后开始处理事务.
 *
 * @author Him188moe
 */
class BotSession(
    val bot: Bot,
    val sessionKey: ByteArray,//TODO 协议抽象? 可能并不是所有协议均需要 sessionKey
    val socket: DataPacketSocketAdapter,
    val NetworkScope: CoroutineScope
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
        internal set(value) {
            field = value
            gtk = getGTK(value)
        }

    /**
     * Web api 使用
     */
    var gtk: Int = 0
        private set

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket].
     * 发送成功后, 该方法会等待收到 [ServerPacket] 直到超时.
     * 由于包名可能过长, 可使用 `DataPacketSocketAdapter.sendAndExpect(PacketProcessor)` 替代.
     *
     * 实现方法:
     * ```kotlin
     * with(session){
     *  ClientPacketXXX(...).sendAndExpect<ServerPacketXXX> {
     *   //it: ServerPacketXXX
     *  }
     * }
     * ```
     *
     * @param P 期待的包
     * @param handler 处理期待的包
     */
    suspend inline fun <reified P : ServerPacket, R> OutgoingPacket.sendAndExpect(noinline handler: suspend (P) -> R): CompletableDeferred<R> {
        val deferred: CompletableDeferred<R> =
            coroutineContext[Job].takeIf { it != null }?.let { CompletableDeferred<R>(it) } ?: CompletableDeferred()
        bot.network.addHandler(TemporaryPacketHandler(P::class, deferred, this@BotSession).also {
            it.toSend(this)
            it.onExpect(handler)
        })
        return deferred
    }

    suspend inline fun <reified P : ServerPacket> OutgoingPacket.sendAndExpect(): CompletableDeferred<Unit> =
        sendAndExpect<P, Unit> {}

    suspend inline fun OutgoingPacket.send() = socket.sendPacket(this)
}


suspend fun BotSession.distributePacket(packet: ServerPacket) = this.socket.distributePacket(packet)
val BotSession.isOpen: Boolean get() = socket.isOpen
val BotSession.qqAccount: UInt get() = bot.account.account

val <T : BotNetworkHandler<*>> T.session get() = this[ActionPacketHandler].session