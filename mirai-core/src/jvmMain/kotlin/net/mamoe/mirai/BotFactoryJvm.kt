@file:Suppress("FunctionName", "unused")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration

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
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
fun Bot(account: BotAccount, configuration: (BotConfiguration.() -> Unit)? = null): Bot =
    factory.Bot(account, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
fun Bot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)? = null): Bot =
    factory.Bot(qq, password, configuration)