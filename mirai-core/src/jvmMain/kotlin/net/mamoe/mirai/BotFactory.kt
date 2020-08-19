/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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
 * ### 自动选择协议模块并构造 [Bot]
 * 在 Kotlin 使用包级函数 [Bot], 在 Java 使用 `BotFactoryJvm.newBot`
 *
 * mirai 通过 [Class.forName] 查找可用的协议实现, 如 `net.mamoe.mirai.qqandroid.QQAndroid`
 *
 * ### 手动选择协议模块并构造 [Bot]
 * 引用 `net.mamoe.mirai.qqandroid.QQAndroid` 并使用其成员函数 [Bot]
 */
public actual interface BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    public actual fun Bot(
        context: Context,
        qq: Long,
        password: String,
        configuration: BotConfiguration
    ): Bot

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    public actual fun Bot(
        context: Context,
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration
    ): Bot

}

/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 *
 * Java 调用方式: `BotFactoryJvm.newBot(...)`
 */
@JvmName("newBot")
@JvmOverloads
public fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(context, qq, password, configuration)

/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun Bot(context: Context, qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(context, qq, password, configuration)


/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 *
 * Java 调用方式: `BotFactoryJvm.newBot(...)`
 */
@JvmName("newBot")
@JvmOverloads
public fun Bot(qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(ContextImpl(), qq, password, configuration)

/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun Bot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(ContextImpl(), qq, password, configuration)


/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 *
 * Java 调用方式: `BotFactoryJvm.newBot(...)`
 */
@JvmName("newBot")
@JvmOverloads
public fun Bot(
    context: Context,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: BotConfiguration = BotConfiguration.Default
): Bot =
    factory.Bot(context, qq, passwordMd5, configuration)

/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun Bot(context: Context, qq: Long, passwordMd5: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(context, qq, passwordMd5, BotConfiguration().apply(configuration))


/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 *
 * Java 调用方式: `BotFactoryJvm.newBot(...)`
 */
@JvmName("newBot")
@JvmOverloads
public fun Bot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration = BotConfiguration.Default): Bot =
    factory.Bot(ContextImpl(), qq, passwordMd5, configuration)

/**
 * 自动加载现有协议的 [BotFactory], 并使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
@JvmSynthetic
public inline fun Bot(qq: Long, passwordMd5: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    factory.Bot(ContextImpl(), qq, passwordMd5, BotConfiguration().apply(configuration))


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