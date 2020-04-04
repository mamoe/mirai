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
 * 只需要实现一个这个传入 MiraiConsole 就可以绑定 UI 层与 Console 层
 * 需要保证线程安全
 */
interface MiraiConsoleUI {
    /**
     * 让 UI 层展示一条 log
     *
     * identity：log 所属的 screen, Main=0; Bot=Bot.uin
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
     * 让 UI 层准备接受新增的一个BOT
     */
    fun prePushBot(
        identity: Long
    )

    /**
     * 让 UI 层接受一个新的bot
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
     * 让 UI 层提供一个输入, 相当于 [readLine]
     */
    suspend fun requestInput(hint: String): String


    /**
     * 让 UI 层更新 bot 管理员的数据
     */
    fun pushBotAdminStatus(
        identity: Long,
        admins: List<Long>
    )

    /**
     * 由 UI 层创建一个 [LoginSolver]
     */
    fun createLoginSolver(): LoginSolver

}