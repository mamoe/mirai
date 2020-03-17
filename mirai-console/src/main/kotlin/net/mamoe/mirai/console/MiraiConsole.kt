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
import net.mamoe.mirai.console.MiraiConsole.CommandProcessor.processNextCommandLine
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.DefaultCommands
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import net.mamoe.mirai.utils.cryptor.ECDH


object MiraiConsole {
    /**
     * 发布的版本名
     */
    const val build = "Pkmon"
    lateinit var version: String

    /**
     * 获取从Console登陆上的Bot, Bots
     * */
    val bots get() = Bot.instances

    fun getBotOrNull(uin: Long): Bot? {
        return bots.asSequence().mapNotNull { it.get() }.firstOrNull { it.uin == uin }
    }

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
        frontEnd: MiraiConsoleUI,
        coreVersion: String = "0.0.0",
        consoleVersion: String = "0.0.0"
    ) {
        if (start) {
            return
        }
        start = true

        /* 初始化前端 */
        this.version = consoleVersion
        this.frontEnd = frontEnd
        this.frontEnd.pushVersion(consoleVersion, build, coreVersion)
        logger("Mirai-console now running under $path")
        logger("Get news in github: https://github.com/mamoe/mirai")
        logger("Mirai为开源项目，请自觉遵守开源项目协议")
        logger("Powered by Mamoe Technologies and contributors")

        /* 加载ECDH */
        try {
            ECDH()
        } catch (ignored: Exception) {
        }
        //Security.removeProvider("BC")


        /* 依次启用功能 */
        DefaultCommands()
        PluginManager.loadPlugins()
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

        @Suppress("unused")
        fun runCommandBlocking(sender: CommandSender, command: String) = runBlocking { runCommand(sender, command) }

        private suspend fun processNextCommandLine() {
            for (command in commandChannel) {
                var commandStr = command.commandStr
                if (!commandStr.startsWith("/")) {
                    commandStr = "/$commandStr"
                }
                if (!CommandManager.runCommand(command.sender, commandStr)) {
                    command.sender.sendMessage("未知指令 $commandStr")
                }
            }
        }
    }

    object UIPushLogger {
        operator fun invoke(any: Any? = null) {
            invoke(
                "[Mirai $version $build]",
                0L,
                any
            )
        }

        operator fun invoke(priority: LogPriority, identityStr: String, identity: Long, any: Any? = null) {
            if (any != null) {
                frontEnd.pushLog(priority, identityStr, identity, "$any")
            }
        }

        operator fun invoke(priority: LogPriority, identityStr: String, identity: Long, e: Exception? = null) {
            if (e != null) {
                frontEnd.pushLog(priority, identityStr, identity, "${e.stackTrace}")
            }
        }

        // 设置默认的pushLog输出为 INFO 类型
        operator fun invoke(identityStr: String, identity: Long, any: Any? = null) {
            if (any != null) {
                frontEnd.pushLog(LogPriority.INFO, identityStr, identity, "$any")
            }
        }

        operator fun invoke(identityStr: String, identity: Long, e: Exception? = null) {
            if (e != null) {
                frontEnd.pushLog(LogPriority.INFO, identityStr, identity, "${e.stackTrace}")
            }
        }
    }

}


