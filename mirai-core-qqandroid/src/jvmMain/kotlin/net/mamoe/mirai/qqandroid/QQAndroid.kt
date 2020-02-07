@file:Suppress("FunctionName")

package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * QQ for Android
 */
@UseExperimental(MiraiInternalAPI::class)
actual object QQAndroid : BotFactory {

    actual override fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration): Bot {
        return QQAndroidBot(context, BotAccount(qq, password), configuration)
    }

    fun Bot(qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot =
        QQAndroidBot(BotAccount(qq, password), configuration)
}

/**
 * 使用指定的 [配置][configuration] 构造 [Bot] 实例
 */
inline fun QQAndroid.Bot(qq: Long, password: String, configuration: (BotConfiguration.() -> Unit)): Bot =
    this.Bot(qq, password, BotConfiguration().apply(configuration))