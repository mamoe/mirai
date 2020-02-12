/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.api.http.generateSessionKey
import net.mamoe.mirai.plugin.*
import java.io.File
import kotlin.concurrent.thread

object MiraiConsole {
    val bots
        get() = Bot.instances

    val pluginManager: PluginManager
        get() = PluginManager

    var logger: MiraiConsoleLogger = DefaultLogger

    var path: String = System.getProperty("user.dir")

    val version = " 0.13"
    val build = "Beta"

    fun start() {
        logger("Mirai-console v${version} $build is still in testing stage, majority feature is available")
        logger("Mirai-console v${version} $build 还处于测试阶段, 大部分功能可用")
        logger()
        logger("Mirai-console now running under " + System.getProperty("user.dir"))
        logger("Mirai-console 正在 " + System.getProperty("user.dir") + "下运行")
        logger()
        logger("Get news in github: https://github.com/mamoe/mirai")
        logger("在Github中获取项目最新进展: https://github.com/mamoe/mirai")
        logger("Mirai为开源项目，请自觉遵守开源项目协议")
        logger("Powered by Mamoe Technology")
        logger()
        logger("\"/login qqnumber qqpassword \" to login a bot")
        logger("\"/login qq号 qq密码 \" 来登陆一个BOT")

        CommandManager.register(DefaultCommands.DefaultLoginCommand())
        pluginManager.loadPlugins()
        CommandListener.start()
    }

    fun stop() {
        PluginManager.disableAllPlugins()
    }

    /**
     * Defaults Commands are recommend to be replaced by plugin provided commands
     */
    object DefaultCommands {
        class DefaultLoginCommand : Command(
            "login"
        ) {
            override fun onCommand(args: List<String>): Boolean {
                if (args.size < 2) {
                    println("\"/login qqnumber qqpassword \" to login a bot")
                    println("\"/login qq号 qq密码 \" 来登录一个BOT")
                    return false
                }
                val qqNumber = args[0].toLong()
                val qqPassword = args[1]
                println("login...")
                try {
                    runBlocking {
                        Bot(qqNumber, qqPassword).alsoLogin()
                    }
                } catch (e: Exception) {
                    println("$qqNumber login failed")
                }
                return true
            }
        }
    }

    object CommandListener {
        fun start() {
            thread {
                processNextCommandLine()
            }
        }

        tailrec fun processNextCommandLine() {
            val fullCommand = readLine()
            if (fullCommand != null && fullCommand.startsWith("/")) {
                if (!CommandManager.runCommand(fullCommand)) {
                    logger("unknown command $fullCommand")
                    logger("未知指令 $fullCommand")
                }
            }
            processNextCommandLine();
        }
    }

    interface MiraiConsoleLogger {
        operator fun invoke(any: Any? = null)
    }

    object DefaultLogger : MiraiConsoleLogger {
        override fun invoke(any: Any?) {
            println("[Mirai${version} $build]: " + any?.toString())
        }
    }

    @UnstableDefault
    object MiraiProperties {
        var config = Config.load("$path/mirai.json")

        var HTTP_API_ENABLE: Boolean by config.withDefault { true }
        var HTTP_API_PORT: Int by config.withDefault { 8080 }
        var HTTP_API_AUTH_KEY: String by config.withDefault { "INITKEY" + generateSessionKey() }
    }
}

fun main() {
    MiraiConsole.start()
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        MiraiConsole.stop()
    })
}

