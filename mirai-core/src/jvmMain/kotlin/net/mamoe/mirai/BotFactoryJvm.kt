/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("BotFactoryJvm")
@file:Suppress("FunctionName", "unused")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.ContextImpl

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