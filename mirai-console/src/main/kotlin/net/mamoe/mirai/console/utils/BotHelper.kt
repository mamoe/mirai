/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.console.plugins.ConfigSectionImpl
import net.mamoe.mirai.console.plugins.loadAsConfig
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.console.utils.BotManagers.BOT_MANAGERS
import java.io.File

internal object BotManagers {
    val config = File("${MiraiConsole.path}/bot.yml").loadAsConfig()
    val BOT_MANAGERS: ConfigSection by config.withDefaultWriteSave { ConfigSectionImpl() }
}

fun Bot.addManager(long: Long) {
    BOT_MANAGERS.putIfAbsent(this.id.toString(), mutableListOf<Long>())
    BOT_MANAGERS[this.id.toString()] =
        (BOT_MANAGERS.getLongList(this.id.toString()) as MutableList<Long>).apply { add(long) }
    BotManagers.config.save()
}

fun Bot.removeManager(long: Long) {
    BOT_MANAGERS.putIfAbsent(this.id.toString(), mutableListOf<Long>())
    BOT_MANAGERS[this.id.toString()] =
        (BOT_MANAGERS.getLongList(this.id.toString()) as MutableList<Long>).apply { add(long) }
    BotManagers.config.save()
}

val Bot.managers: List<Long>
    get() {
        BOT_MANAGERS.putIfAbsent(this.id.toString(), mutableListOf<Long>())
        return BOT_MANAGERS.getLongList(this.id.toString())
    }

fun Bot.checkManager(long: Long): Boolean {
    return this.managers.contains(long)
}


