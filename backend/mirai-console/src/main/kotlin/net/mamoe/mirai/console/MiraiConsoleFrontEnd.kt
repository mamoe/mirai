/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger

/**
 * 只需要实现一个这个传入 MiraiConsole 就可以绑定 UI 层与 Console 层
 *
 * 需要保证线程安全
 */
@ConsoleExperimentalAPI
@ConsoleFrontEndImplementation
public interface MiraiConsoleFrontEnd {
    /**
     * 名称
     */
    public val name: String

    /**
     * 版本
     */
    public val version: String

    public fun loggerFor(identity: String?): MiraiLogger

    /**
     * 让 UI 层接受一个新的bot
     * */
    public fun pushBot(
        bot: Bot
    )

    /**
     * 让 UI 层提供一个输入, 相当于 [readLine]
     */
    public suspend fun requestInput(hint: String): String

    /**
     * 由 UI 层创建一个 [LoginSolver]
     */
    public fun createLoginSolver(): LoginSolver
}