@file:Suppress("FunctionName", "unused", "SpellCheckingInspection")

package net.mamoe.mirai.timpc

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.timpc.TIMPC.Bot
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.coroutineContext

/**
 * TIM PC 协议的 [Bot] 构造器.
 */
object TIMPC : BotFactory {
    /**
     * 在当前 CoroutineScope 下构造 Bot 实例
     * 该 Bot 实例的生命周期将会跟随这个 CoroutineScope.
     * 这个 CoroutineScope 也会等待 Bot 的结束才会结束.
     *
     * ```kotlin
     * suspend fun myProcess(){
     *   TIMPC.Bot(account, logger)
     * }
     * ```
     */
    override suspend fun Bot(account: BotAccount, logger: MiraiLogger?): Bot =
        TIMPCBot(account, logger, coroutineContext)

    /**
     * 在当前 CoroutineScope 下构造 Bot 实例
     * 该 Bot 实例的生命周期将会跟随这个 CoroutineScope.
     * 这个 CoroutineScope 也会等待 Bot 的结束才会结束.
     *
     * ```kotlin
     * suspend fun myProcess(){
     *   TIMPC.Bot(account, logger)
     * }
     * ```
     */
    override suspend fun Bot(qq: Long, password: String, logger: MiraiLogger?): Bot =
        TIMPCBot(BotAccount(qq, password), logger, coroutineContext)

    /**
     * 在特定的 CoroutineScope 下构造 Bot 实例
     * 该 Bot 实例的生命周期将会跟随这个 CoroutineScope.
     * 这个 CoroutineScope 也会等待 Bot 的结束才会结束.
     *
     * ```kotlin
     * fun myProcess(){
     *   TIMPC.run {
     *     GlobalScope.Bot(account, logger)
     *   }
     * }
     * ```
     */
    override fun CoroutineScope.Bot(qq: Long, password: String, logger: MiraiLogger?): Bot =
        TIMPCBot(BotAccount(qq, password), logger, coroutineContext)

    /**
     * 在特定的 CoroutineScope 下构造 Bot 实例
     * 该 Bot 实例的生命周期将会跟随这个 CoroutineScope.
     * 这个 CoroutineScope 也会等待 Bot 的结束才会结束.
     *
     * ```kotlin
     * fun myProcess(){
     *   TIMPC.run {
     *     GlobalScope.Bot(account, logger)
     *   }
     * }
     * ```
     */
    override fun CoroutineScope.Bot(account: BotAccount, logger: MiraiLogger?): Bot =
        TIMPCBot(account, logger, coroutineContext)
}