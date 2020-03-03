/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("FunctionName")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context

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
    fun Bot(
        context: Context,
        qq: Long,
        password: String,
        configuration: BotConfiguration = BotConfiguration.Default
    ): Bot

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    fun Bot(
        context: Context,
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration = BotConfiguration.Default
    ): Bot
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun BotFactory.Bot(
    context: Context,
    qq: Long,
    password: String,
    configuration: (BotConfiguration.() -> Unit)
): Bot =
    this.Bot(context, qq, password, BotConfiguration().apply(configuration))

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun BotFactory.Bot(
    context: Context,
    qq: Long,
    passwordMd5: ByteArray,
    configuration: (BotConfiguration.() -> Unit)
): Bot =
    this.Bot(context, qq, passwordMd5, BotConfiguration().apply(configuration))