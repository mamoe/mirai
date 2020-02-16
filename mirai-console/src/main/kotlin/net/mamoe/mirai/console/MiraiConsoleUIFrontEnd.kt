package net.mamoe.mirai.console

import net.mamoe.mirai.Bot

/**
 * 只需要实现一个这个 传入MiraiConsole 就可以绑定UI层与Console层
 * 注意线程
 */

interface MiraiConsoleUIFrontEnd {
    /**
     * 让UI层展示一条log
     * identityString: log前面的prefix
     * identity：log所属的screen, Main=0; Bot=Bot.uin
     */
    fun pushLog(
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

    /**
     * 让UI层更新BOT管理员的数据
     */
    fun pushBotAdminStatus(
        identity: Long,
        admins: List<Long>
    )
}