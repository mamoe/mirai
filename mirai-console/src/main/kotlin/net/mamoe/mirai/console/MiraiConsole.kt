package net.mamoe.mirai.console

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.MiraiHttpAPIServer
import net.mamoe.mirai.api.http.generateSessionKey
import net.mamoe.mirai.console.MiraiConsole.CommandListener.processNextCommandLine
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.plugins.loadAsConfig
import net.mamoe.mirai.console.plugins.withDefaultWrite
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File
import java.util.*


object MiraiConsole {
    val bots
        get() = Bot.instances

    fun getBotByUIN(uin: Long): Bot? {
        bots.forEach {
            if (it.get()?.uin == uin) {
                return it.get()
            }
        }
        return null
    }

    val pluginManager: PluginManager
        get() = PluginManager

    var logger = UIPushLogger

    var path: String = System.getProperty("user.dir")

    val version = "v0.01"
    var coreVersion = "v0.15.1"
    val build = "Beta"

    var allDown = false

    lateinit var frontEnd: MiraiConsoleUI
    fun start(
        frontEnd: MiraiConsoleUI
    ) {
        this.frontEnd = frontEnd
        frontEnd.pushVersion(
            version, build, coreVersion
        )
        logger("Mirai-console [$version $build | core version $coreVersion] is still in testing stage, majority feature is available")
        logger(
            "Mirai-console now running under " + System.getProperty(
                "user.dir"
            )
        )
        logger("Get news in github: https://github.com/mamoe/mirai")
        logger("Mirai为开源项目，请自觉遵守开源项目协议")
        logger("Powered by Mamoe Technologies and contributors")


        DefaultCommands()
        HTTPAPIAdaptar()
        pluginManager.loadPlugins()
        CommandListener.start()

        logger("Mirai-console 启动完成")
        logger("\"/login qqnumber qqpassword \" to login a bot")
        logger("\"/login qq号 qq密码 \" 来登陆一个BOT")
    }

    fun stop() {
        PluginManager.disableAllPlugins()
        allDown = true
        try {
            bots.forEach {
                it.get()?.close()
            }
        } catch (ignored: Exception) {

        }
    }

    object HTTPAPIAdaptar {
        operator fun invoke() {
            if (MiraiProperties.HTTP_API_ENABLE) {
                if (MiraiProperties.HTTP_API_AUTH_KEY.startsWith("InitKey")) {
                    logger("请尽快更改初始生成的HTTP API AUTHKEY")
                }
                logger("正在启动HTTPAPI; 端口=" + MiraiProperties.HTTP_API_PORT)
                MiraiHttpAPIServer.logger = SimpleLogger("HTTP API") { _, message, e ->
                    logger("[Mirai HTTP API]", 0, message)
                }
                MiraiHttpAPIServer.start(
                    MiraiProperties.HTTP_API_PORT,
                    MiraiProperties.HTTP_API_AUTH_KEY
                )
                logger("HTTPAPI启动完成; 端口= " + MiraiProperties.HTTP_API_PORT)

            }
        }
    }

    /**
     * Defaults Commands are recommend to be replaced by plugin provided commands
     */
    object DefaultCommands {
        operator fun invoke() {
            registerCommand {
                name = "login"
                description = "Mirai-Console default bot login command"
                onCommand {
                    if (it.size < 2) {
                        logger("\"/login qqnumber qqpassword \" to login a bot")
                        logger("\"/login qq号 qq密码 \" 来登录一个BOT")
                        return@onCommand false
                    }
                    val qqNumber = it[0].toLong()
                    val qqPassword = it[1]
                    logger("[Bot Login]", 0, "login...")
                    try {
                        frontEnd.prePushBot(qqNumber)
                        val bot = Bot(qqNumber, qqPassword) {
                            this.loginSolver = frontEnd.createLoginSolver()
                            this.botLoggerSupplier = {
                                SimpleLogger("BOT $qqNumber]") { _, message, e ->
                                    logger("[BOT $qqNumber]", qqNumber, message)
                                    if (e != null) {
                                        logger("[NETWORK ERROR]", qqNumber, e.toString())//因为在一页 所以可以不打QQ
                                        e.printStackTrace()
                                    }
                                }
                            }
                            this.networkLoggerSupplier = {
                                SimpleLogger("BOT $qqNumber") { _, message, e ->
                                    logger("[NETWORK]", qqNumber, message)//因为在一页 所以可以不打QQ
                                    if (e != null) {
                                        logger("[NETWORK ERROR]", qqNumber, e.toString())//因为在一页 所以可以不打QQ
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                        bot.login()
                        logger(
                            "[Bot Login]",
                            0,
                            "$qqNumber login successes"
                        )
                        frontEnd.pushBot(bot)
                    } catch (e: Exception) {
                        logger(
                            "[Bot Login]",
                            0,
                            "$qqNumber login failed -> " + e.message
                        )
                    }
                    true
                }
            }

            registerCommand {
                name = "status"
                description = "Mirai-Console default status command"
                onCommand {
                    when (it.size) {
                        0 -> {
                            logger("当前有" + bots.size + "个BOT在线")
                        }
                        1 -> {
                            val bot = it[0]
                            var find = false
                            bots.forEach {
                                if (it.get()?.uin.toString().contains(bot)) {
                                    find = true
                                    logger("" + it.get()?.uin + ": 在线中; 好友数量:" + it.get()?.qqs?.size + "; 群组数量:" + it.get()?.groups?.size)
                                }
                            }
                            if (!find) {
                                logger("没有找到BOT$bot")
                            }
                        }
                    }
                    true
                }
            }


            registerCommand {
                name = "say"
                description = "Mirai-Console default say command"
                onCommand {
                    if (it.size < 2) {
                        logger("say [好友qq号或者群号] [文本消息]     //将默认使用第一个BOT")
                        logger("say [bot号] [好友qq号或者群号] [文本消息]")
                        return@onCommand false
                    }
                    val bot: Bot? = if (it.size == 2) {
                        if (bots.size == 0) {
                            logger("还没有BOT登陆")
                            return@onCommand false
                        }
                        bots[0].get()
                    } else {
                        getBotByUIN(it[0].toLong())
                    }
                    if (bot == null) {
                        logger("没有找到BOT")
                        return@onCommand false
                    }
                    val target = it[it.size - 2].toLong()
                    val message = it[it.size - 1]
                    try {
                        val contact = bot[target]
                        runBlocking {
                            contact.sendMessage(message)
                            logger("消息已推送")
                        }
                    } catch (e: NoSuchElementException) {
                        logger("没有找到群或好友 号码为${target}")
                        return@onCommand false
                    }
                    true
                }
            }


            registerCommand {
                name = "plugins"
                alias = listOf("plugin")
                description = "show all plugins"
                onCommand {
                    PluginManager.getAllPluginDescriptions().let {
                        println("loaded " + it.size + " plugins")
                        it.forEach {
                            logger("\t" + it.name + " v" + it.version + " by" + it.author + " " + it.info)
                        }
                        true
                    }
                }
            }

            registerCommand {
                name = "command"
                alias = listOf("commands", "help", "helps")
                description = "show all commands"
                onCommand {
                    CommandManager.getCommands().let {
                        println("currently have " + it.size + " commands")
                        it.toSet().forEach {
                            logger("\t" + it.name + " :" + it.description)
                        }
                    }
                    true
                }
            }

            registerCommand {
                name = "about"
                description = "About Mirai-Console"
                onCommand {
                    logger("v$version $build is still in testing stage, majority feature is available")
                    logger(
                        "now running under " + System.getProperty(
                            "user.dir"
                        )
                    )
                    logger("在Github中获取项目最新进展: https://github.com/mamoe/mirai")
                    logger("Mirai为开源项目，请自觉遵守开源项目协议")
                    logger("Powered by Mamoe Technologies and contributors")
                    true
                }
            }

        }
    }

    object CommandListener : Job by {
        GlobalScope.launch(start = CoroutineStart.LAZY) {
            processNextCommandLine()
        }
    }() {
        val commandChannel: Channel<String> = Channel()

        suspend fun processNextCommandLine() {
            if (allDown) {
                return
            }
            for (command in commandChannel) {
                var fullCommand = command
                if (!fullCommand.startsWith("/")) {
                    fullCommand = "/$fullCommand"
                }
                if (!CommandManager.runCommand(fullCommand)) {
                    logger("未知指令 $fullCommand")
                }
            }
        }
    }

    object UIPushLogger {
        operator fun invoke(any: Any? = null) {
            invoke(
                "[Mirai$version $build]",
                0L,
                any
            )
        }

        operator fun invoke(identityStr: String, identity: Long, any: Any? = null) {
            if (any != null) {
                frontEnd.pushLog(identity, "$identityStr: $any")
            }
        }
    }

    object MiraiProperties {
        var config = File("$path/mirai.properties").loadAsConfig()

        var HTTP_API_ENABLE: Boolean by config.withDefaultWrite { true }
        var HTTP_API_PORT: Int by config.withDefaultWrite { 8080 }
        var HTTP_API_AUTH_KEY: String by config.withDefaultWriteSave {
            "InitKey" + generateSessionKey()
        }

    }

}



