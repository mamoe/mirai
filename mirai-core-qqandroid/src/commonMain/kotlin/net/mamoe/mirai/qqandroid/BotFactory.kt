package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context

/**
 * QQ for Android
 */
expect object QQAndroid : BotFactory {

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    override fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration): Bot
}