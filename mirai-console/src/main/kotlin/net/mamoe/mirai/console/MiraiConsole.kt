/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.MiraiHttpAPIServer
import net.mamoe.mirai.api.http.generateSessionKey
import net.mamoe.mirai.console.MiraiConsole.CommandProcessor.processNextCommandLine
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.plugins.loadAsConfig
import net.mamoe.mirai.console.plugins.withDefaultWrite
import net.mamoe.mirai.console.plugins.withDefaultWriteSave
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.console.utils.checkManager
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.SimpleLogger
import net.mamoe.mirai.utils.cryptor.ECDH
import java.io.File
import java.security.Security
import java.util.*


object MiraiConsole {
    /**
     * 发布的版本号 统一修改位置
     */
    val version = "v0.01"
    var coreVersion = "v0.18.0"
    val build = "Alpha"


    /**
     * 获取从Console登陆上的Bot, Bots
     * */
    val bots get() = Bot.instances

    fun getBotByUIN(uin: Long): Bot? {
        bots.forEach {
            if (it.get()?.uin == uin) {
                return it.get()
            }
        }
        return null
    }

    /**
     * PluginManager
     */
    val pluginManager: PluginManager get() = PluginManager

    /**
     * 与前端交互所使用的Logger
     */
    var logger = UIPushLogger

    /**
     * Console运行路径
     */
    var path: String = System.getProperty("user.dir")

    /**
     * Console前端接口
     */
    lateinit var frontEnd: MiraiConsoleUI


    /**
     * 启动Console
     */
    var start = false

    fun start(
        frontEnd: MiraiConsoleUI
    ) {
        if (start) {
            return
        }
        start = true

        /* 加载ECDH */
        try {
            ECDH()
        } catch (ignored: Exception) {
        }
        //Security.removeProvider("BC")


        /* 初始化前端 */
        this.frontEnd = frontEnd
        frontEnd.pushVersion(version, build, coreVersion)
        logger("Mirai-console [$version $build | core version $coreVersion] is still in testing stage, major features are available")
        logger("Mirai-console now running under $path")
        logger("Get news in github: https://github.com/mamoe/mirai")
        logger("Mirai为开源项目，请自觉遵守开源项目协议")
        logger("Powered by Mamoe Technologies and contributors")

        /* 依次启用功能 */
        DefaultCommands()
        HTTPAPIAdaptar()
        pluginManager.loadPlugins()
        CommandProcessor.start()

        /* 通知启动完成 */
        logger("Mirai-console 启动完成")
        logger("\"/login qqnumber qqpassword \" to login a bot")
        logger("\"/login qq号 qq密码 \" 来登录一个BOT")
    }

    fun stop() {
        PluginManager.disableAllPlugins()
        try {
            bots.forEach {
                it.get()?.close()
            }
        } catch (ignored: Exception) {

        }
    }


    object CommandProcessor : Job by {
        GlobalScope.launch(start = CoroutineStart.LAZY) {
            processNextCommandLine()
        }
    }() {

        internal class FullCommand(
            val sender: CommandSender,
            val commandStr: String
        )

        private val commandChannel: Channel<FullCommand> = Channel()

        suspend fun runConsoleCommand(command: String) {
            commandChannel.send(
                FullCommand(ConsoleCommandSender, command)
            )
        }

        suspend fun runCommand(sender: CommandSender, command: String) {
            commandChannel.send(
                FullCommand(sender, command)
            )
        }

        fun runConsoleCommandBlocking(command: String) = runBlocking { runConsoleCommand(command) }

        fun runCommandBlocking(sender: CommandSender, command: String) = runBlocking { runCommand(sender, command) }

        private suspend fun processNextCommandLine() {
            for (command in commandChannel) {
                var commandStr = command.commandStr
                if (!commandStr.startsWith("/")) {
                    commandStr = "/$commandStr"
                }
                if (!CommandManager.runCommand(command.sender, commandStr)) {
                    logger("未知指令 $commandStr")
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

}

object MiraiProperties {
    var config = File("${MiraiConsole.path}/mirai.properties").loadAsConfig()

    var HTTP_API_ENABLE: Boolean by config.withDefaultWrite { true }
    var HTTP_API_PORT: Int by config.withDefaultWrite { 8080 }
    var HTTP_API_AUTH_KEY: String by config.withDefaultWriteSave {
        "InitKey" + generateSessionKey()
    }
}

object HTTPAPIAdaptar {
    operator fun invoke() {
        if (MiraiProperties.HTTP_API_ENABLE) {
            if (MiraiProperties.HTTP_API_AUTH_KEY.startsWith("InitKey")) {
                MiraiConsole.logger("请尽快更改初始生成的HTTP API AUTHKEY")
            }
            MiraiConsole.logger("正在启动HTTPAPI; 端口=" + MiraiProperties.HTTP_API_PORT)
            MiraiHttpAPIServer.logger = SimpleLogger("HTTP API") { _, message, e ->
                MiraiConsole.logger("[Mirai HTTP API]", 0, message)
            }
            MiraiHttpAPIServer.start(
                MiraiProperties.HTTP_API_PORT,
                MiraiProperties.HTTP_API_AUTH_KEY
            )
            MiraiConsole.logger("HTTPAPI启动完成; 端口= " + MiraiProperties.HTTP_API_PORT)
        }
    }
}



