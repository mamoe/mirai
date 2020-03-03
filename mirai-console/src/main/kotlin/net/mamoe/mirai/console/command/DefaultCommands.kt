/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.addManager
import net.mamoe.mirai.console.utils.checkManager
import net.mamoe.mirai.console.utils.getManagers
import net.mamoe.mirai.console.utils.removeManager
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.utils.SimpleLogger
import java.util.*


/**
 * Some defaults commands are recommend to be replaced by plugin provided commands
 */

object DefaultCommands {
    operator fun invoke() {
        registerCommand {
            name = "manager"
            description = "Add a manager"
            onCommand { it ->
                if (this !is ConsoleCommandSender) {
                    sendMessage("请在后台使用该指令")
                    return@onCommand false
                }
                if (it.size < 2) {
                    MiraiConsole.logger("[Bot Manager]", 0, "/manager add [bot ID] [Manager ID]")
                    MiraiConsole.logger("[Bot Manager]", 0, "/manager remove [bot ID] [Manager ID]")
                    MiraiConsole.logger("[Bot Manager]", 0, "/manager list [bot ID]")
                    return@onCommand true
                }
                val botId = try {
                    it[1].toLong()
                } catch (e: Exception) {
                    MiraiConsole.logger("[Bot Manager]", 0, it[1] + " 不是一个Bot的ID")
                    return@onCommand false
                }
                val bot = MiraiConsole.getBotByUIN(botId)
                if (bot == null) {
                    MiraiConsole.logger("[Bot Manager]", 0, "$botId 没有在Console中登陆")
                    return@onCommand false
                }
                when (it[0]) {
                    "add" -> {
                        if (it.size < 3) {
                            MiraiConsole.logger("[Bot Manager]", 0, "/manager add [bot ID] [Manager ID]")
                            return@onCommand true
                        }
                        val adminID = try {
                            it[2].toLong()
                        } catch (e: Exception) {
                            MiraiConsole.logger("[Bot Manager]", 0, it[2] + " 不是一个ID")
                            return@onCommand false
                        }
                        bot.addManager(adminID)
                        MiraiConsole.logger("[Bot Manager]", 0, it[2] + "增加成功")
                    }
                    "remove" -> {
                        if (it.size < 3) {
                            MiraiConsole.logger("[Bot Manager]", 0, "/manager remove [bot ID] [Manager ID]")
                            return@onCommand true
                        }
                        val adminID = try {
                            it[2].toLong()
                        } catch (e: Exception) {
                            MiraiConsole.logger("[Bot Manager]", 0, it[1] + " 不是一个ID")
                            return@onCommand false
                        }
                        if (!bot.checkManager(adminID)) {
                            MiraiConsole.logger("[Bot Manager]", 0, it[2] + "本身不是一个Manager")
                            return@onCommand true
                        }
                        bot.removeManager(adminID)
                        MiraiConsole.logger("[Bot Manager]", 0, it[2] + "移除成功")
                    }
                    "list" -> {
                        bot.getManagers().forEach {
                            MiraiConsole.logger("[Bot Manager]", 0, " -> $it")
                        }
                    }
                }
                return@onCommand true
            }
        }

        registerCommand {
            name = "login"
            description = "机器人登陆"
            onCommand {
                if (this !is ConsoleCommandSender) {
                    sendMessage("请在后台使用该指令")
                    return@onCommand false
                }
                if (it.size < 2) {
                    MiraiConsole.logger("\"/login qqnumber qqpassword \" to login a bot")
                    MiraiConsole.logger("\"/login qq号 qq密码 \" 来登录一个BOT")
                    return@onCommand false
                }
                val qqNumber = it[0].toLong()
                val qqPassword = it[1]
                MiraiConsole.logger("[Bot Login]", 0, "login...")
                try {
                    MiraiConsole.frontEnd.prePushBot(qqNumber)
                    val bot = Bot(qqNumber, qqPassword) {
                        this.loginSolver = MiraiConsole.frontEnd.createLoginSolver()
                        this.botLoggerSupplier = {
                            SimpleLogger("BOT $qqNumber]") { _, message, e ->
                                MiraiConsole.logger("[BOT $qqNumber]", qqNumber, message)
                                if (e != null) {
                                    MiraiConsole.logger("[NETWORK ERROR]", qqNumber, e.toString())//因为在一页 所以可以不打QQ
                                    e.printStackTrace()
                                }
                            }
                        }
                        this.networkLoggerSupplier = {
                            SimpleLogger("BOT $qqNumber") { _, message, e ->
                                MiraiConsole.logger("[NETWORK]", qqNumber, message)//因为在一页 所以可以不打QQ
                                if (e != null) {
                                    MiraiConsole.logger("[NETWORK ERROR]", qqNumber, e.toString())//因为在一页 所以可以不打QQ
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                    bot.login()
                    bot.subscribeMessages {
                        this.startsWith("/") {
                            if (bot.checkManager(this.sender.id)) {
                                val sender = ContactCommandSender(this.subject)
                                MiraiConsole.CommandProcessor.runCommand(
                                    sender, it
                                )
                            }
                        }
                    }
                    sendMessage("$qqNumber login successes")
                    MiraiConsole.frontEnd.pushBot(bot)
                } catch (e: Exception) {
                    sendMessage("$qqNumber login failed -> " + e.message)
                }
                true
            }
        }

        registerCommand {
            name = "status"
            description = "获取状态"
            onCommand {
                when (it.size) {
                    0 -> {
                        sendMessage("当前有" + MiraiConsole.bots.size + "个BOT在线")
                    }
                    1 -> {
                        val bot = it[0]
                        var find = false
                        MiraiConsole.bots.forEach {
                            if (it.get()?.uin.toString().contains(bot)) {
                                find = true
                                appendMessage(
                                    "" + it.get()?.uin + ": 在线中; 好友数量:" + it.get()?.friends?.size + "; 群组数量:" + it.get()
                                        ?.groups?.size
                                )
                            }
                        }
                        if (!find) {
                            sendMessage("没有找到BOT$bot")
                        }
                    }
                }
                true
            }
        }


        registerCommand {
            name = "say"
            description = "聊天功能演示"
            onCommand {
                if (it.size < 2) {
                    MiraiConsole.logger("say [好友qq号或者群号] [文本消息]     //将默认使用第一个BOT")
                    MiraiConsole.logger("say [bot号] [好友qq号或者群号] [文本消息]")
                    return@onCommand false
                }
                val bot: Bot? = if (it.size == 2) {
                    if (MiraiConsole.bots.size == 0) {
                        MiraiConsole.logger("还没有BOT登录")
                        return@onCommand false
                    }
                    MiraiConsole.bots[0].get()
                } else {
                    MiraiConsole.getBotByUIN(it[0].toLong())
                }
                if (bot == null) {
                    MiraiConsole.logger("没有找到BOT")
                    return@onCommand false
                }
                val target = it[it.size - 2].toLong()
                val message = it[it.size - 1]
                try {
                    val contact = bot.getFriendOrNull(target) ?: bot.getGroup(target)
                    contact.sendMessage(message)
                    MiraiConsole.logger("消息已推送")
                } catch (e: NoSuchElementException) {
                    MiraiConsole.logger("没有找到群或好友 号码为${target}")
                    return@onCommand false
                }
                true
            }
        }


        registerCommand {
            name = "plugins"
            alias = listOf("plugin")
            description = "获取插件列表"
            onCommand {
                PluginManager.getAllPluginDescriptions().let {
                    it.forEach {
                        appendMessage("\t" + it.name + " v" + it.version + " by" + it.author + " " + it.info)
                    }
                    appendMessage("加载了" + it.size + "个插件")
                    true
                }
            }
        }

        registerCommand {
            name = "command"
            alias = listOf("commands", "help", "helps")
            description = "获取指令列表"
            onCommand {
                CommandManager.getCommands().let {
                    var size = 0
                    appendMessage("")//\n
                    it.toSet().forEach {
                        ++size
                        appendMessage("-> " + it.name + " :" + it.description)
                    }
                    appendMessage("""共有${size}条指令""")
                }
                true
            }
        }

        registerCommand {
            name = "about"
            description = "About Mirai-Console"
            onCommand {
                appendMessage("v${MiraiConsole.version} ${MiraiConsole.build} is still in testing stage, major features are available")
                appendMessage("now running under ${MiraiConsole.path}")
                appendMessage("在Github中获取项目最新进展: https://github.com/mamoe/mirai")
                appendMessage("Mirai为开源项目，请自觉遵守开源项目协议")
                appendMessage("Powered by Mamoe Technologies and contributors")
                true
            }
        }

    }
}
