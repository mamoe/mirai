/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.utils

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger.LogPriority

/**
 * 只需要实现一个这个 传入MiraiConsole 就可以绑定UI层与Console层
 * 注意线程
 */

interface MiraiConsoleUI {
    /**
     * 让UI层展示一条log
     *
     * identity：log所属的screen, Main=0; Bot=Bot.uin
     */
    fun pushLog(
        identity: Long,
        message: String
    )

    fun pushLog(
        priority: LogPriority,
        identityStr: String,
        identity: Long,
        message: String
    )

    /**
     * 让UI层准备接受新增的一个BOT
     */
    fun prePushBot(
        identity: Long
    )

    /**
     * 让UI层接受一个新的bot
     * */
    fun pushBot(
        bot: Bot
    )


    fun pushVersion(
        consoleVersion: String,
        consoleBuild: String,
        coreVersion: String
    )

    /**
     * 让UI层提供一个Input
     * 这个Input 不 等于 Command
     *
     */
    suspend fun requestInput(): String


    /**
     * 让UI层更新BOT管理员的数据
     */
    fun pushBotAdminStatus(
        identity: Long,
        admins: List<Long>
    )

    /**
     * 由UI层创建一个LoginSolver
     */
    fun createLoginSolver(): LoginSolver

}