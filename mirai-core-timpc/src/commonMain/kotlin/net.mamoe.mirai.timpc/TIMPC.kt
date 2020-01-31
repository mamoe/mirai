@file:Suppress("FunctionName", "unused", "SpellCheckingInspection")

package net.mamoe.mirai.timpc

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.timpc.TIMPC.Bot
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * TIM PC 协议的 [Bot] 构造器.
 */
@UseExperimental(MiraiInternalAPI::class)
object TIMPC : BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    override fun Bot(context: Context, qq: Long, password: String, configuration: BotConfiguration): Bot = TIMPCBot(BotAccount(qq, password), configuration)

    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    fun Bot(qq: Long, password: String, configuration: BotConfiguration = BotConfiguration.Default): Bot = TIMPCBot(BotAccount(qq, password), configuration)
}