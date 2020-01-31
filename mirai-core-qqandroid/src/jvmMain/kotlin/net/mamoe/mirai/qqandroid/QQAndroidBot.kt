package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context
import net.mamoe.mirai.utils.ContextImpl
import net.mamoe.mirai.utils.MiraiInternalAPI

@UseExperimental(MiraiInternalAPI::class)
@Suppress("FunctionName")
internal fun QQAndroidBot(account: BotAccount, configuration: BotConfiguration): QQAndroidBot = QQAndroidBot(ContextImpl(), account, configuration)

@UseExperimental(MiraiInternalAPI::class)
internal actual class QQAndroidBot actual constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration)

