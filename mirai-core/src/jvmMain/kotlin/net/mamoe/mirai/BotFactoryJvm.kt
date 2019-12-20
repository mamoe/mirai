@file:Suppress("FunctionName", "unused")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.utils.MiraiLogger

// Do not use ServiceLoader. Probably not working on MPP
private val factory = run {
    try {
        Class.forName("net.mamoe.mirai.timpc.TIMPC").kotlin.objectInstance as BotFactory
    } catch (ignored: Exception) {
        null
    }
} ?: error(
    """
    No BotFactory found. Please ensure that you've added dependency of protocol modules.
    Available modules:
    - net.mamoe:mirai-core-timpc
    You should have at lease one protocol module installed.
    """.trimIndent()
)

/**
 * 加载现有协议的 [BotFactory], 并调用其 [BotFactory.Bot]
 *
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
suspend fun Bot(account: BotAccount, logger: MiraiLogger? = null): Bot =
    factory.Bot(account, logger)

/**
 * 加载现有协议的 [BotFactory], 并调用其 [BotFactory.Bot]
 *
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
suspend fun Bot(qq: Long, password: String, logger: MiraiLogger? = null): Bot =
    factory.Bot(qq, password, logger)

/**
 * 加载现有协议的 [BotFactory], 并调用其 [BotFactory.Bot]
 *
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
fun CoroutineScope.Bot(qq: Long, password: String, logger: MiraiLogger? = null): Bot =
    factory.run { this@Bot.Bot(qq, password, logger) }

/**
 * 加载现有协议的 [BotFactory], 并调用其 [BotFactory.Bot]
 *
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
fun CoroutineScope.Bot(account: BotAccount, logger: MiraiLogger? = null): Bot =
    factory.run { this@Bot.Bot(account, logger) }