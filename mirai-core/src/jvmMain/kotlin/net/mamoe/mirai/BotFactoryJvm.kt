@file:Suppress("FunctionName", "unused")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context

// Do not use ServiceLoader. Probably not working on MPP
@PublishedApi
internal val factory: BotFactory = run {
    runCatching {
        Class.forName("net.mamoe.mirai.timpc.TIMPC").kotlin.objectInstance as BotFactory
    }.getOrElse {
        runCatching {
            Class.forName("net.mamoe.mirai.qqandroid.QQAndroid").kotlin.objectInstance as BotFactory
        }.getOrNull()
    }
} ?: error(
    """
    No BotFactory found. Please ensure that you've added dependency of protocol modules.
    Available modules:
    - net.mamoe:mirai-core-timpc
    - net.mamoe:mirai-core-qqandroid (recommended)
    You should have at lease one protocol module installed.
    -------------------------------------------------------
    找不到 BotFactory. 请确保你依赖了至少一个协议模块.
    可用的协议模块: 
    - net.mamoe:mirai-core-timpc
    - net.mamoe:mirai-core-qqandroid (推荐)
    请添加上述任一模块的依赖(与 mirai-core 版本相同)
    """.trimIndent()
)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(context, qq, password, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun Bot(context: Context, qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(context, qq, password, configuration)