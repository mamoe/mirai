package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

internal actual class QQAndroidBot actual constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : QQAndroidBotBase(account, logger, context) {

}