/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("FunctionName", "OverridingDeprecatedMember")

package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.qqandroid.QQAndroid.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context

/**
 * QQ for Android
 */
@Suppress("INAPPLICABLE_JVM_NAME")
actual object QQAndroid : BotFactory {

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    actual override fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration): Bot {
        return QQAndroidBot(context, BotAccount(qq, password), configuration)
    }

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    fun Bot(qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
        QQAndroidBot(BotAccount(qq, password), configuration)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    actual override fun Bot(
        context: Context,
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration
    ): Bot = QQAndroidBot(context, BotAccount(qq, passwordMd5), configuration)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    @JvmName("newBot")
    fun Bot(
        qq: Long,
        passwordMd5: ByteArray,
        configuration: BotConfiguration
    ): Bot = QQAndroidBot(BotAccount(qq, passwordMd5), configuration)
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun QQAndroid.Bot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.Bot(qq, password, BotConfiguration().apply(configuration))