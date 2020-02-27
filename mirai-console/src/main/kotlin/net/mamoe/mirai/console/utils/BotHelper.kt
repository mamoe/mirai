package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.ConfigSection
import net.mamoe.mirai.console.plugins.ConfigSectionImpl
import net.mamoe.mirai.console.plugins.loadAsConfig
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.console.utils.BotManagers.BOT_MANAGERS
import java.io.File

object BotManagers {
    val config = File("${MiraiConsole.path}/bot.yml").loadAsConfig()
    val BOT_MANAGERS: ConfigSection by config.withDefaultWriteSave { ConfigSectionImpl() }
}

fun Bot.addManager(long: Long) {
    BOT_MANAGERS.putIfAbsent(this.uin.toString(), mutableListOf<Long>())
    BOT_MANAGERS[this.uin.toString()] =
        (BOT_MANAGERS.getLongList(this.uin.toString()) as MutableList<Long>).apply { add(long) }
    BotManagers.config.save()
}

fun Bot.removeManager(long: Long) {
    BOT_MANAGERS.putIfAbsent(this.uin.toString(), mutableListOf<Long>())
    BOT_MANAGERS[this.uin.toString()] =
        (BOT_MANAGERS.getLongList(this.uin.toString()) as MutableList<Long>).apply { add(long) }
    BotManagers.config.save()
}

fun Bot.getManagers(): List<Long> {
    BOT_MANAGERS.putIfAbsent(this.uin.toString(), mutableListOf<Long>())
    return BOT_MANAGERS.getLongList(this.uin.toString())
}

fun Bot.checkManager(long: Long): Boolean {
    return this.getManagers().contains(long)
}


