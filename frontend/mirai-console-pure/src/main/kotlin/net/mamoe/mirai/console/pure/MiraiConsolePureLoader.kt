/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.pure

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.DefaultCommands
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.MiraiConsoleFrontEnd
import kotlin.concurrent.thread

class MiraiConsolePureLoader {
    companion object {
        @JvmStatic
        fun load(
            coreVersion: String,
            consoleVersion: String
        ) {
            start(
                MiraiConsoleFrontEndPure(),
                coreVersion,
                consoleVersion
            )
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
            })
        }
    }
}

/**
 * 启动 Console
 */
@JvmOverloads
internal fun start(
    frontEnd: MiraiConsoleFrontEnd,
    coreVersion: String = "0.0.0",
    consoleVersion: String = "0.0.0",
    path: String = System.getProperty("user.dir")
) {
    if (MiraiConsole.started) {
        return
    }
    MiraiConsole.started = true
    this.path = path
    /* 初始化前端 */
    this.version = consoleVersion
    this.frontEnd = frontEnd
    this.frontEnd.pushVersion(consoleVersion, MiraiConsole.build, coreVersion)
    logger("Mirai-console now running under $path")
    logger("Get news in github: https://github.com/mamoe/mirai")
    logger("Mirai为开源项目，请自觉遵守开源项目协议")
    logger("Powered by Mamoe Technologies and contributors")

    /* 依次启用功能 */
    DefaultCommands()
    PluginManager.loadPlugins()
    CommandManager.start()

    /* 通知启动完成 */
    logger("Mirai-console 启动完成")
    logger("\"login qqnumber qqpassword \" to login a bot")
    logger("\"login qq号 qq密码 \" 来登录一个BOT")

    /* 尝试从系统配置自动登录 */
    DefaultCommands.tryLoginAuto()
}