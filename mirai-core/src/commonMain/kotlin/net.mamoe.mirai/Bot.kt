@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "FunctionName", "NOTHING_TO_INLINE")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.GroupNotFoundException
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmSynthetic


/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * @see Contact
 */
interface Bot : CoroutineScope {
    companion object {
        suspend inline operator fun invoke(account: BotAccount, logger: MiraiLogger): Bot = BotImpl(account, logger, coroutineContext)
        suspend inline operator fun invoke(account: BotAccount): Bot = BotImpl(account, context = coroutineContext)
        @JvmSynthetic
        suspend inline operator fun invoke(qq: UInt, password: String): Bot = BotImpl(BotAccount(qq, password), context = coroutineContext)

        suspend inline operator fun invoke(qq: Long, password: String): Bot = BotImpl(BotAccount(qq.toUInt(), password), context = coroutineContext)
        operator fun invoke(qq: Long, password: String, context: CoroutineContext): Bot = BotImpl(BotAccount(qq, password), context = context)
        @JvmSynthetic
        operator fun invoke(qq: UInt, password: String, context: CoroutineContext): Bot = BotImpl(BotAccount(qq, password), context = context)

        operator fun invoke(account: BotAccount, context: CoroutineContext): Bot = BotImpl(account, context = context)


        inline fun forEachInstance(block: (Bot) -> Unit) = BotImpl.forEachInstance(block)

        fun instanceWhose(qq: UInt): Bot = BotImpl.instanceWhose(qq = qq)
    }

    /**
     * 账号信息
     */
    val account: BotAccount

    /**
     * 日志记录器
     */
    val logger: MiraiLogger

    override val coroutineContext: CoroutineContext

    /**
     * 网络模块
     */
    val network: BotNetworkHandler<*>

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    fun tryReinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable? = null
    ): Job

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    suspend fun reinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable? = null
    ): LoginResult

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    fun reinitializeNetworkHandlerAsync(
        configuration: BotConfiguration,
        cause: Throwable? = null
    ): Deferred<LoginResult>

    /**
     * 与这个机器人相关的 QQ 列表. 机器人与 QQ 不一定是好友
     */
    val qqs: ContactList<QQ>

    /**
     * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会线程安全地创建一个.
     */
    fun getQQ(id: UInt): QQ

    /**
     * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会线程安全地创建一个.
     */
    fun getQQ(id: Long): QQ

    /**
     * 与这个机器人相关的群列表. 机器人不一定是群成员.
     */
    val groups: ContactList<Group>

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [id] 无效, 将会抛出 [GroupNotFoundException]
     */
    suspend fun getGroup(id: GroupId): Group

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [internalId] 无效, 将会抛出 [GroupNotFoundException]
     */
    suspend fun getGroup(internalId: GroupInternalId): Group

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [id] 无效, 将会抛出 [GroupNotFoundException]
     */
    suspend fun getGroup(id: Long): Group

    fun close()
}