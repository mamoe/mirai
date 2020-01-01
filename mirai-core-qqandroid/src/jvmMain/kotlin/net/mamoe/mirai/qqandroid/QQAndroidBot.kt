package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.qqandroid.utils.Context
import net.mamoe.mirai.qqandroid.utils.ContextImpl
import net.mamoe.mirai.utils.BotConfiguration

@Suppress("FunctionName")
internal fun QQAndroidBot(account: BotAccount, configuration: BotConfiguration) = QQAndroidBot(ContextImpl(), account, configuration)

internal actual class QQAndroidBot actual constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration)