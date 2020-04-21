/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.center.PluginCenter
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.*
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.utils.SimpleLogger
import java.util.*


/**
 * Some defaults commands are recommend to be replaced by plugin provided commands
 */

object DefaultCommands {
    private suspend fun CommandSender.login(account: Long, password: String) {
        MiraiConsole.logger("[Bot Login]", 0, "login...")
        try {
            MiraiConsole.frontEnd.prePushBot(account)
            val bot = Bot(account, password) {
                fileBasedDeviceInfo()
                this.loginSolver = MiraiConsole.frontEnd.createLoginSolver()
                this.botLoggerSupplier = {
                    SimpleLogger("BOT $account]") { _, message, e ->
                        MiraiConsole.logger("[BOT $account]", account, message)
                        if (e != null) {
                            MiraiConsole.logger("[NETWORK ERROR]", account, e)//因为在一页 所以可以不打QQ
                        }
                    }
                }
                this.networkLoggerSupplier = {
                    SimpleLogger("BOT $account") { _, message, e ->
                        MiraiConsole.logger("[NETWORK]", account, message)//因为在一页 所以可以不打QQ
                        if (e != null) {
                            MiraiConsole.logger("[NETWORK ERROR]", account, e)//因为在一页 所以可以不打QQ
                        }
                    }
                }
            }
            bot.login()
            bot.subscribeMessages {
                startsWith("/") { message ->
                    if (bot.checkManager(this.sender.id)) {
                        val sender = if (this is GroupMessage) {
                            GroupContactCommandSender(this.sender, this.subject)
                        } else {
                            ContactCommandSender(this.subject)
                        }
                        CommandManager.runCommand(
                            sender, message
                        )
                    }
                }
            }
            sendMessage("$account login successes")
            MiraiConsole.frontEnd.pushBot(bot)
        } catch (e: Exception) {
            sendMessage("$account login failed -> " + e.message)
        }
    }

    private fun String.property(): String? = System.getProperty(this)

    @JvmSynthetic
    internal fun tryLoginAuto() {
        // For java -Dmirai.account=10086 -Dmirai.password=Password -jar mirai-console-wrapper-X.jar
        val account = ("mirai.account".property() ?: return).toLong()
        val password = "mirai.password".property() ?: "mirai.passphrase".property() ?: "mirai.passwd".property()
        if (password == null) {
            MiraiConsole.logger.invoke(SimpleLogger.LogPriority.ERROR, "[AUTO LOGIN]", account,
                "Find the account to be logged in, but no password specified")
            return
        }
        GlobalScope.launch {
            ConsoleCommandSender.login(account, password)
        }
    }

    operator fun invoke() {
        registerConsoleCommands {
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
                val bot = MiraiConsole.getBotOrNull(botId)
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
                        if (bot.addManager(adminID)) {
                            MiraiConsole.logger("[Bot Manager]", 0, it[2] + "增加成功")
                        } else {
                            MiraiConsole.logger("[Bot Manager]", 0, it[2] + "已经是一个manager了")
                        }
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
                        bot.managers.forEach {
                            MiraiConsole.logger("[Bot Manager]", 0, " -> $it")
                        }
                    }
                }
                return@onCommand true
            }
        }

        registerConsoleCommands {
            name = "login"
            description = "机器人登录"
            onCommand {
                if (this !is ConsoleCommandSender) {
                    sendMessage("请在后台使用该指令")
                    return@onCommand false
                }
                if (it.size < 2) {
                    MiraiConsole.logger("\"/login qq password \" to login a bot")
                    MiraiConsole.logger("\"/login qq号 qq密码 \" 来登录一个BOT")
                    return@onCommand false
                }
                val qqNumber = it[0].toLong()
                val qqPassword = it[1]
                login(qqNumber, qqPassword)
                true
            }
        }

        registerConsoleCommands {
            name = "status"
            description = "获取状态"
            onCommand { args ->
                when (args.size) {
                    0 -> {
                        sendMessage("当前有" + MiraiConsole.bots.size + "个BOT在线")
                    }
                    1 -> {
                        val bot = args[0]
                        var find = false
                        MiraiConsole.bots.forEach {
                            if (it.get()?.id.toString().contains(bot)) {
                                find = true
                                appendMessage(
                                    "" + it.get()?.id + ": 在线中; 好友数量:" + it.get()?.friends?.size + "; 群组数量:" + it.get()
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


        registerConsoleCommands {
            name = "say"
            description = "聊天功能演示"
            onCommand {
                if (it.size < 2) {
                    MiraiConsole.logger("say [好友qq号或者群号] [测试消息]     //将默认使用第一个BOT")
                    MiraiConsole.logger("say [bot号] [好友qq号或者群号] [测试消息]")
                    return@onCommand false
                }
                val bot: Bot? = if (it.size == 2) {
                    if (MiraiConsole.bots.isEmpty()) {
                        MiraiConsole.logger("还没有BOT登录")
                        return@onCommand false
                    }
                    MiraiConsole.bots[0].get()
                } else {
                    MiraiConsole.getBotOrNull(it[0].toLong())
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


        registerConsoleCommands {
            name = "plugins"
            alias = listOf("plugin")
            description = "获取插件列表"
            onCommand {
                PluginManager.getAllPluginDescriptions().let { descriptions ->
                    descriptions.forEach {
                        appendMessage("\t" + it.name + " v" + it.version + " by " + it.author + " " + it.info)
                    }
                    appendMessage("加载了" + descriptions.size + "个插件")
                    true
                }
            }
        }

        registerConsoleCommands {
            name = "command"
            alias = listOf("commands", "help", "helps")
            description = "获取指令列表"
            onCommand {
                CommandManager.commands.toSet().let { commands ->
                    var size = 0
                    appendMessage("")//\n
                    commands.forEach {
                        ++size
                        appendMessage("-> " + it.name + " :" + it.description)
                    }
                    appendMessage("""共有${size}条指令""")
                }
                true
            }
        }

        registerConsoleCommands {
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

        registerConsoleCommands {
            name = "reload"
            alias = listOf("reloadPlugins")
            description = "重新加载全部插件"
            onCommand {
                PluginManager.reloadPlugins()
                sendMessage("重新加载完成")
                true
            }
        }

        registerConsoleCommands {
            name = "install"
            description = "Install plugin from PluginCenter"
            usage = "/install [plugin-name] to install plugin or /install [page-num] to show list "
            onCommand {

                val center = PluginCenter.Default

                suspend fun showPage(num: Int) {
                    sendMessage("正在连接" + center.name)
                    val list = PluginCenter.Default.fetchPlugin(num)
                    appendMessage("\n")
                    list.values.forEach {
                        appendMessage("=>" + it.name + " ;作者: " + it.author + " ;介绍: " + it.description)
                    }
                }

                suspend fun installPlugin(name: String) {
                    sendMessage("正在连接" + center.name)
                    val plugin = center.findPlugin(name)
                    if (plugin == null) {
                        sendMessage("插件未找到, 请注意大小写")
                        return
                    }
                    sendMessage("正在安装" + plugin.name)
                    try {
                        center.downloadPlugin(name) {}
                        sendMessage("安装" + plugin.name + "成功")
                    } catch (e: Exception) {
                        sendMessage("安装" + plugin.name + "失败, " + (e.message ?: "未知原因"))
                    }
                }

                if (it.isEmpty()) {
                    showPage(1)
                } else {
                    val arg = it[0]

                    val id = try {
                        arg.toInt()
                    } catch (e: Exception) {
                        0
                    }

                    if (id > 0) {
                        showPage(id)
                    } else {
                        installPlugin(arg)
                    }
                }
                true
            }
        }
    }
}
