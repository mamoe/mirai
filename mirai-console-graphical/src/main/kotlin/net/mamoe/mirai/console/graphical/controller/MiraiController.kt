package net.mamoe.mirai.console.graphical.controller

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleUI
import tornadofx.Controller

class MiraiController : Controller(), MiraiConsoleUI {
    override fun pushLog(identity: Long, message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun prePushBot(identity: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pushBot(bot: Bot) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun requestInput(question: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}