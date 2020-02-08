/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event


abstract class BotEvent : Event {
    private lateinit var _bot: Bot
    open val bot: Bot get() = _bot

    constructor(bot: Bot) : super() {
        this._bot = bot
    }

    constructor() : super()
}

class BotLoginSucceedEvent(bot: Bot) : BotEvent(bot)

class BotOfflineEvent(bot: Bot) : BotEvent(bot)