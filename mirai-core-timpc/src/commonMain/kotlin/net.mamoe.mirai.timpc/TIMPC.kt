@file:Suppress("FunctionName", "unused", "SpellCheckingInspection")

package net.mamoe.mirai.timpc

import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.timpc.TIMPC.Bot
import net.mamoe.mirai.utils.BotConfiguration

/**
 * TIM PC 协议的 [Bot] 构造器.
 */
object TIMPC : BotFactory {
    /**
     * 使用指定的 [配置][configuration] 构造 [Bot] 实例
     */
    override fun Bot(account: BotAccount, configuration: BotConfiguration): Bot = TIMPCBot(account, configuration)
}