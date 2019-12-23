package net.mamoe.mirai.qqandroid

import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

internal actual class QQAndroidBot actual constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : QQAndroidBotBase(account, logger, context)

suspend fun main() {
    val bot = QQAndroidBot(BotAccount(1, ""), null, GlobalScope.coroutineContext).alsoLogin()
    bot.network.awaitDisconnection()
}