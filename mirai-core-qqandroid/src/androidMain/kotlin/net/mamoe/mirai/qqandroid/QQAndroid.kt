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
actual object QQAndroid : BotFactory {
    @UseExperimental(MiraiInternalAPI::class)
    actual override fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration): Bot {
        return QQAndroidBot(context, BotAccount(qq, password), configuration)
    }
}