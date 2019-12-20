@file:Suppress("FunctionName")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 构造 [Bot] 的工厂.
 *
 * 在协议模块中有各自的实现.
 * - `mirai-core-timpc`: `TIMPC`
 * - `mirai-core-qqandroid`: `QQAndroid`
 */
interface BotFactory {
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
    suspend fun Bot(account: BotAccount, logger: MiraiLogger? = null): Bot

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
    suspend fun Bot(qq: Long, password: String, logger: MiraiLogger? = null): Bot

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
    fun CoroutineScope.Bot(qq: Long, password: String, logger: MiraiLogger? = null): Bot

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
    fun CoroutineScope.Bot(account: BotAccount, logger: MiraiLogger? = null): Bot
}