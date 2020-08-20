/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.command

import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandManagerImpl.allRegisteredCommands
import net.mamoe.mirai.console.command.CommandManagerImpl.register
import net.mamoe.mirai.event.selectMessagesUnit
import net.mamoe.mirai.utils.DirectoryLogger
import net.mamoe.mirai.utils.weeksToMillis
import java.io.File
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * 添加一个 [Bot] 实例到全局 Bot 列表, 但不登录.
 */
public fun MiraiConsole.addBot(id: Long, password: String): Bot {
    return Bot(id, password) {

        /**
         * 重定向 [网络日志][networkLoggerSupplier] 到指定目录. 若目录不存在将会自动创建 ([File.mkdirs])
         * @see DirectoryLogger
         * @see redirectNetworkLogToDirectory
         */
        fun redirectNetworkLogToDirectory(
            dir: File = File("logs"),
            retain: Long = 1.weeksToMillis,
            identity: (bot: Bot) -> String = { "Net ${it.id}" }
        ) {
            require(!dir.isFile) { "dir must not be a file" }
            dir.mkdirs()
            networkLoggerSupplier = { DirectoryLogger(identity(it), dir, retain) }
        }

        fun redirectBotLogToDirectory(
            dir: File = File("logs"),
            retain: Long = 1.weeksToMillis,
            identity: (bot: Bot) -> String = { "Net ${it.id}" }
        ) {
            require(!dir.isFile) { "dir must not be a file" }
            dir.mkdirs()
            botLoggerSupplier = { DirectoryLogger(identity(it), dir, retain) }
        }

        fileBasedDeviceInfo()
        this.loginSolver = this@addBot.frontEnd.createLoginSolver()
        redirectNetworkLogToDirectory()
        //   redirectBotLogToDirectory()
    }
}

@Suppress("EXPOSED_SUPER_INTERFACE")
public interface BuiltInCommand : Command, BuiltInCommandInternal

// for identification
internal interface BuiltInCommandInternal : Command

@Suppress("unused")
public object BuiltInCommands {

    public val all: Array<out Command> by lazy {
        this::class.nestedClasses.mapNotNull { it.objectInstance as? Command }.toTypedArray()
    }

    internal fun registerAll() {
        BuiltInCommands::class.nestedClasses.forEach {
            (it.objectInstance as? Command)?.register()
        }
    }

    public object Help : SimpleCommand(
        ConsoleCommandOwner, "help",
        description = "Gets help about the console."
    ), BuiltInCommand {
        @Handler
        public suspend fun CommandSender.handle() {
            sendMessage("现在有指令: ${allRegisteredCommands.joinToString { it.primaryName }}")
            sendMessage("帮助还没写, 将就一下")
        }
    }

    public object Stop : SimpleCommand(
        ConsoleCommandOwner, "stop", "shutdown", "exit",
        description = "Stop the whole world."
    ), BuiltInCommand {
        init {
            Runtime.getRuntime().addShutdownHook(thread(false) {
                MiraiConsole.cancel()
            })
        }

        private val closingLock = Mutex()

        @Handler
        public suspend fun CommandSender.handle(): Unit {
            closingLock.withLock {
                sendMessage("Stopping mirai-console")
                kotlin.runCatching {
                    MiraiConsole.job.cancelAndJoin()
                }.fold(
                    onSuccess = { sendMessage("mirai-console stopped successfully.") },
                    onFailure = {
                        MiraiConsole.mainLogger.error(it)
                        sendMessage(it.localizedMessage ?: it.message ?: it.toString())
                    }
                )
            }
            exitProcess(0)
        }
    }

    public object Login : SimpleCommand(
        ConsoleCommandOwner, "login",
        description = "Log in a bot account."
    ), BuiltInCommand {
        @Handler
        public suspend fun CommandSender.handle(id: Long, password: String) {

            kotlin.runCatching {
                MiraiConsole.addBot(id, password).alsoLogin()
            }.fold(
                onSuccess = { sendMessage("${it.nick} ($id) Login succeed") },
                onFailure = { throwable ->
                    sendMessage(
                        "Login failed: ${throwable.localizedMessage ?: throwable.message ?: throwable.toString()}" +
                                if (this is MessageEventContextAware<*>) {
                                    this.fromEvent.selectMessagesUnit {
                                        "stacktrace" reply {
                                            throwable.stackTraceToString()
                                        }
                                    }
                                    "test"
                                } else "")

                    throw throwable
                }
            )
        }
    }
}

/*

/**
 * Some defaults commands are recommend to be replaced by plugin provided commands
 */
internal object DefaultCommands {
    internal val commandPrefix = "mirai.command.prefix".property() ?: "/"
    private suspend fun CommandSender.login(account: Long, password: String) {
        MiraiConsole.logger("[Bot Login]", 0, "login...")
        try {
            MiraiConsole.frontEnd.prePushBot(account)
            val bot = Bot(account, password) {
                fileBasedDeviceInfo(MiraiConsole.path + "/device.json")
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
            MiraiConsole.subscribeMessages {
                startsWith(commandPrefix) { message ->
                    if (this.bot != bot) return@startsWith

                    if (bot.checkManager(this.sender.id)) {
                        val sender = if (this is GroupMessageEvent) {
                            GroupContactCommandSender(bot,this.sender, this.subject)
                        } else {
                            ContactCommandSender(bot,this.subject)
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
            MiraiConsole.logger.invoke(
                SimpleLogger.LogPriority.ERROR, "[AUTO LOGIN]", account,
                "Find the account to be logged in, but no password specified"
            )
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
                        sendMessage("当前有" + botInstances.size + "个BOT在线")
                    }
                    1 -> {
                        val bot = args[0]
                        var find = false
                        botInstances.forEach {
                            if (it.id.toString().contains(bot)) {
                                find = true
                                appendMessage(
                                    "" + it.id + ": 在线中; 好友数量:" + it.friends.size + "; 群组数量:" + it.groups.size
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
                    if (botInstances.isEmpty()) {
                        MiraiConsole.logger("还没有BOT登录")
                        return@onCommand false
                    }
                    botInstances[0]
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
            onCommand { args ->

                val center = MiraiConsole.frontEnd.pluginCenter

                suspend fun showPage(num: Int) {
                    sendMessage("正在连接 " + center.name)
                    val list = center.fetchPlugin(num)
                    if (list.isEmpty()) {
                        sendMessage("页码过大")
                        return
                    }
                    sendMessage("显示插件列表第 $num 页")
                    appendMessage("\n")
                    list.values.forEach {
                        appendMessage("=> " + it.name + " ;作者: " + it.author + " ;介绍: " + it.description)
                    }
                    sendMessage("使用 /install ${num + 1} 查看下一页")
                }

                suspend fun installPlugin(name: String) {
                    sendMessage("正在连接 " + center.name)
                    val plugin = center.findPlugin(name)
                    if (plugin == null) {
                        sendMessage("插件未找到, 请注意大小写")
                        return
                    }
                    sendMessage("正在安装 " + plugin.name)
                    try {
                        center.downloadPlugin(name) {}
                        sendMessage("安装 " + plugin.name + " 成功, 请重启服务器以更新")
                    } catch (e: Exception) {
                        sendMessage("安装 " + plugin.name + " 失败, " + (e.message ?: "未知原因"))
                    }
                }

                if (args.isEmpty()) {
                    showPage(1)
                } else {
                    val arg = args[0]

                    val id = arg.toIntOrNull() ?: 0
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

 */