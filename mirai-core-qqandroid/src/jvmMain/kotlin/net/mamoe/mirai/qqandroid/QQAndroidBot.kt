package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.utils.BotConfiguration

internal actual class QQAndroidBot actual constructor(
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(account, configuration)

suspend fun main() {
    val bot = QQAndroidBot(BotAccount(1, ""), BotConfiguration()).alsoLogin()
    bot.network.awaitDisconnection()
}