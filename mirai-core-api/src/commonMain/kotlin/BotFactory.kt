/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration

/**
 * 构造 [Bot] 的工厂. 这是 [Bot] 唯一的构造方式.
 *
 * @see IMirai.BotFactory
 */
public interface BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, password: String): Bot = newBot(qq, password, BotConfiguration.Default)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot

    /**
     * 使用 [默认配置][BotConfiguration.Default] 构造 [Bot] 实例
     */
    public fun newBot(qq: Long, passwordMd5: ByteArray): Bot = newBot(qq, passwordMd5, BotConfiguration.Default)

    public companion object INSTANCE : BotFactory {
        override fun newBot(qq: Long, password: String, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, password, configuration)
        }

        override fun newBot(qq: Long, passwordMd5: ByteArray, configuration: BotConfiguration): Bot {
            return Mirai.BotFactory.newBot(qq, passwordMd5, configuration)
        }
    }
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
public inline fun BotFactory.newBot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.newBot(qq, password, BotConfiguration().apply(configuration))

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
public inline fun BotFactory.newBot(qq: Long, password: ByteArray, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.newBot(qq, password, BotConfiguration().apply(configuration))