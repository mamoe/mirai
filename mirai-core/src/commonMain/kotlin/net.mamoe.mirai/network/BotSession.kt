@file:Suppress("MemberVisibilityCanBePrivate", "unused", "EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import kotlinx.coroutines.*
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.message.Image
import net.mamoe.mirai.message.ImageId0x03
import net.mamoe.mirai.message.ImageId0x06
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.handler.ActionPacketHandler
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocketAdapter
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.network.protocol.tim.packet.SessionKey
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.assertUnreachable
import net.mamoe.mirai.utils.getGTK
import net.mamoe.mirai.utils.internal.PositiveNumbers
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
import kotlin.coroutines.coroutineContext

/**
 * 构造 [BotSession] 的捷径
 */
@Suppress("FunctionName", "NOTHING_TO_INLINE")
internal inline fun TIMBotNetworkHandler.BotSession(
    sessionKey: SessionKey,
    socket: DataPacketSocketAdapter
): BotSession = BotSession(bot, sessionKey, socket, this)

/**
 * 登录会话. 当登录完成后, 客户端会拿到 sessionKey.
 * 此时建立 session, 然后开始处理事务.
 *
 * 本类中含有各平台相关扩展函数等.
 *
 * @author Him188moe
 */
@UseExperimental(MiraiInternalAPI::class)
expect class BotSession(
    bot: Bot,
    sessionKey: SessionKey,
    socket: DataPacketSocketAdapter,
    NetworkScope: CoroutineScope
) : BotSessionBase

/**
 * [BotSession] 平台通用基础
 */
@MiraiInternalAPI
abstract class BotSessionBase(
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
    val sKey: String get() = _sKey

    @Suppress("PropertyName")
    internal var _sKey: String = ""
        set(value) {
            field = value
            _gtk = getGTK(value)
        }

    /**
     * Web api 使用
     */
    val gtk: Int get() = _gtk

    private var _gtk: Int = 0

    /**
     * 发送一个数据包, 并期待接受一个特定的 [ServerPacket][P].
     * 这个方法会立即发出这个数据包然后返回一个 [CompletableDeferred].
     *
     * 实现方法:
     * ```kotlin
     * with(session){
     *  ClientPacketXXX(...).sendAndExpectAsync<ServerPacketXXX> {
     *   //it: ServerPacketXXX
     *  }
     * }
     * ```
     * @sample net.mamoe.mirai.network.protocol.tim.packet.action.uploadImage
     *
     * @param checkSequence 是否筛选 `sequenceId`, 即是否筛选发出的包对应的返回包.
     * @param P 期待的包
     * @param handler 处理期待的包. **将会在调用本函数的 [coroutineContext] 下执行.**
     *
     * @see Bot.withSession 转换 receiver, 即 `this` 的指向, 为 [BotSession]
     */
    suspend inline fun <reified P : Packet, R> OutgoingPacket.sendAndExpectAsync(
        checkSequence: Boolean = true,
        noinline handler: suspend (P) -> R
    ): Deferred<R> {
        val deferred: CompletableDeferred<R> = CompletableDeferred(coroutineContext[Job])
        bot.network.addHandler(TemporaryPacketHandler(P::class, deferred, this@BotSessionBase as BotSession, checkSequence, coroutineContext + deferred).also {
            it.toSend(this)
            it.onExpect(handler)
        })
        return deferred
    }

    suspend inline fun <reified P : Packet> OutgoingPacket.sendAndExpectAsync(checkSequence: Boolean = true): Deferred<P> =
        sendAndExpectAsync<P, P>(checkSequence) { it }

    suspend inline fun <reified P : Packet, R> OutgoingPacket.sendAndExpect(
        checkSequence: Boolean = true,
        timeout: TimeSpan = 5.seconds,
        crossinline mapper: (P) -> R
    ): R = withTimeout(timeout.millisecondsLong) { sendAndExpectAsync<P, R>(checkSequence) { mapper(it) }.await() }

    suspend inline fun <reified P : Packet> OutgoingPacket.sendAndExpect(
        checkSequence: Boolean = true,
        timeout: TimeSpan = 5.seconds
    ): P = withTimeout(timeout.millisecondsLong) { sendAndExpectAsync<P, P>(checkSequence) { it }.await() }

    suspend inline fun OutgoingPacket.send() = socket.sendPacket(this)


    suspend inline fun Int.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0).toUInt())
    suspend inline fun Long.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0))
    suspend inline fun UInt.qq(): QQ = bot.getQQ(this)

    suspend inline fun Int.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0).toUInt())
    suspend inline fun Long.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0))
    suspend inline fun UInt.group(): Group = bot.getGroup(GroupId(this))
    suspend inline fun GroupId.group(): Group = bot.getGroup(this)
    suspend inline fun GroupInternalId.group(): Group = bot.getGroup(this)

    suspend fun Image.getLink(): ImageLink = when (this.id) {
        is ImageId0x06 -> FriendImagePacket.RequestImageLink(bot.qqAccount, bot.sessionKey, id).sendAndExpect<FriendImageLink>()
        is ImageId0x03 -> GroupImagePacket.RequestImageLink(bot.qqAccount, bot.sessionKey, id).sendAndExpect<ImageDownloadInfo>().requireSuccess()
        else -> assertUnreachable()
    }

    suspend inline fun Image.downloadAsByteArray(): ByteArray = getLink().downloadAsByteArray()
    suspend inline fun Image.download(): ByteReadPacket = getLink().download()
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


/**
 * 发送数据包
 * @throws IllegalStateException 当 [BotNetworkHandler.socket] 未开启时
 */
suspend inline fun BotSession.sendPacket(packet: OutgoingPacket) = this.bot.sendPacket(packet)


suspend inline fun BotSession.getQQ(@PositiveNumbers number: Long): QQ = this.bot.getQQ(number)
suspend inline fun BotSession.getQQ(number: UInt): QQ = this.bot.getQQ(number)

suspend inline fun BotSession.getGroup(id: UInt): Group = this.bot.getGroup(id)
suspend inline fun BotSession.getGroup(@PositiveNumbers id: Long): Group = this.bot.getGroup(id)
suspend inline fun BotSession.getGroup(id: GroupId): Group = this.bot.getGroup(id)
suspend inline fun BotSession.getGroup(internalId: GroupInternalId): Group = this.bot.getGroup(internalId)