package net.mamoe.mirai.qqandroid

import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.qqandroid.utils.Context
import net.mamoe.mirai.utils.BotConfiguration

internal actual class QQAndroidBot actual constructor(
    context: Context,
    account: BotAccount,
    configuration: BotConfiguration
) : QQAndroidBotBase(context, account, configuration)