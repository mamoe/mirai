@file:Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "DeprecatedCallableAddReplaceWith")
@file:JvmName("BotFactoryJvm")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.ContextImpl

/**
 * 构造 [Bot] 的工厂. 这是 [Bot] 唯一的构造方式.
 *
 * 请添加模块依赖 `mirai-core-qqandroid` 以获取协议支持.
 *
 * mirai 通过 [Class.forName] 查找可用的协议实现, 如 `net.mamoe.mirai.qqandroid.QQAndroid`
 */
actual interface BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    actual fun Bot(
        context: Context,
        qq: Long,
        password: String,
        configuration: BotConfiguration
    ): Bot

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    actual fun Bot(
        context: Context,
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration
    ): Bot

}


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
    - net.mamoe:mirai-core-timpc (stays at 0.12.0)
    - net.mamoe:mirai-core-qqandroid (recommended)
    You should have at lease one protocol module installed.
    -------------------------------------------------------
    找不到 BotFactory. 请确保你依赖了至少一个协议模块.
    可用的协议模块: 
    - net.mamoe:mirai-core-timpc (0.12.0 后停止更新)
    - net.mamoe:mirai-core-qqandroid (推荐)
    请添加上述任一模块的依赖(与 mirai-core 版本相同)
    """.trimIndent()
)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmName("newBot")
@JvmOverloads
fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(context, qq, password, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
inline fun Bot(context: Context, qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(context, qq, password, configuration)


/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmName("newBot")
@JvmOverloads
fun Bot(qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(ContextImpl(), qq, password, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
inline fun Bot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(ContextImpl(), qq, password, configuration)


/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmName("newBot")
@JvmOverloads
fun Bot(
    context: Context,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: BotConfiguration = BotConfiguration.Default
): Bot =
    factory.Bot(context, qq, passwordMd5, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
inline fun Bot(context: Context, qq: Long, passwordMd5: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(context, qq, passwordMd5, BotConfiguration().apply(configuration))


/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmName("newBot")
@JvmOverloads
fun Bot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(ContextImpl(), qq, passwordMd5, configuration)

/**
 * 加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
inline fun Bot(qq: Long, passwordMd5: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(ContextImpl(), qq, passwordMd5, BotConfiguration().apply(configuration))