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
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.SessionKey
import net.mamoe.mirai.utils.getGTK
import kotlin.coroutines.coroutineContext

/**
 * 构造 [BotSession] 的捷径
 */
@Suppress("FunctionName", "NOTHING_TO_INLINE")
internal inline fun TIMBotNetworkHandler.BotSession(
    bot: Bot,
    sessionKey: SessionKey,
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
    val sessionKey: SessionKey,
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
     * 这个方法会立即发出这个数据包然后返回一个 [CompletableDeferred].
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
     * @param checkSequence 是否筛选 `sequenceId`, 即是否筛选发出的包对应的返回包.
     * @param P 期待的包
     * @param handler 处理期待的包. 将会在调用本函数的 [coroutineContext] 下执行.
     *
     * @see Bot.withSession 转换接收器 (receiver, 即 `this` 的指向) 为 [BotSession]
     */
    suspend inline fun <reified P : Packet, R> OutgoingPacket.sendAndExpect(
        checkSequence: Boolean = true,
        noinline handler: suspend (P) -> R
    ): CompletableDeferred<R> {
        val deferred: CompletableDeferred<R> = CompletableDeferred(coroutineContext[Job])
        bot.network.addHandler(TemporaryPacketHandler(P::class, deferred, this@BotSession, checkSequence, coroutineContext + deferred).also {
            it.toSend(this)
            it.onExpect(handler)
        })
        return deferred
    }

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket][P].
     * 您将能从本函数的返回值 [CompletableDeferred] 接收到所期待的 [P]
     */
    suspend inline fun <reified P : Packet> OutgoingPacket.sendAndExpect(checkSequence: Boolean = true): CompletableDeferred<P> =
        sendAndExpect<P, P>(checkSequence) { it }

    suspend inline fun OutgoingPacket.send() = socket.sendPacket(this)
}


inline val BotSession.isOpen: Boolean get() = socket.isOpen
inline val BotSession.qqAccount: UInt get() = bot.account.id

/**
 * 取得 [BotNetworkHandler] 的 [BotSession].
 * 实际上是一个捷径.
 */
val BotNetworkHandler<*>.session: BotSession get() = this[ActionPacketHandler].session

/**
 * 取得 [BotNetworkHandler] 的 sessionKey.
 * 实际上是一个捷径.
 */
inline val BotNetworkHandler<*>.sessionKey: SessionKey get() = this.session.sessionKey

/**
 * 取得 [Bot] 的 [BotSession].
 * 实际上是一个捷径.
 */
inline val Bot.session: BotSession get() = this.network.session

/**
 * 取得 [Bot] 的 `sessionKey`.
 * 实际上是一个捷径.
 */
inline val Bot.sessionKey: SessionKey get() = this.session.sessionKey