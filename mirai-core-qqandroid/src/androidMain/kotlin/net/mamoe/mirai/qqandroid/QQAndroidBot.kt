package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Context

internal actual class QQAndroidBot actual constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration)