@file:Suppress("FunctionName")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

/**
 * 构造 [Bot] 的工厂.
 *
 * 在协议模块中有各自的实现.
 * - `mirai-core-timpc`: `TIMPC`
 * - `mirai-core-qqandroid`: `QQAndroid`
 */
interface BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun BotFactory.Bot(context: Context, qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.Bot(context, qq, password, BotConfiguration().apply(configuration))