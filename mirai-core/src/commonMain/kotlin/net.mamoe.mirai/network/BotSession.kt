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

/**
 * 构造 [BotSession] 的捷径
 */
@Suppress("FunctionName", "NOTHING_TO_INLINE")
internal inline fun TIMBotNetworkHandler.BotSession(
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
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket][P].
     * 发送成功后, 该方法会等待收到 [ServerPacket][P] 直到超时.
     *
     * 实现方法:
     * ```kotlin
     * with(session){
     *  ClientPacketXXX(...).sendAndExpect<ServerPacketXXX> {
     *   //it: ServerPacketXXX
     *  }
     * }
     * ```
     * @sample net.mamoe.mirai.network.protocol.tim.packet.action.uploadImage
     *
     * @param P 期待的包
     * @param handler 处理期待的包
     *
     * @see Bot.withSession 转换接收器 (receiver, 即 `this` 的指向) 为 [BotSession]
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

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket][P].
     * 您将能从本函数的返回值 [CompletableDeferred] 接收到所期待的 [P]
     */
    suspend inline fun <reified P : ServerPacket> OutgoingPacket.sendAndExpect(): CompletableDeferred<P> = sendAndExpect<P, P> { it }

    suspend inline fun OutgoingPacket.send() = socket.sendPacket(this)
}


suspend inline fun BotSession.distributePacket(packet: ServerPacket) = this.socket.distributePacket(packet)
inline val BotSession.isOpen: Boolean get() = socket.isOpen
inline val BotSession.qqAccount: UInt get() = bot.account.id

/**
 * 取得 [T] 的 [BotSession].
 * 实际上是一个捷径.
 */
val <T : BotNetworkHandler<*>> T.session get() = this[ActionPacketHandler].session