package net.mamoe.mirai.console

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DefaultLoginSolver
import net.mamoe.mirai.utils.LoginSolver
import kotlin.concurrent.thread

object MiraiConsoleUIPure : MiraiConsoleUI {
    override fun pushLog(identity: Long, message: String) {
        println(message)
    }

    override fun prePushBot(identity: Long) {

    }

    override fun pushBot(bot: Bot) {

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {

    }

    override suspend fun requestInput(question: String): String {
        return readLine() ?: ""
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) {

    }

    override fun createLoginSolver(): LoginSolver {
        return DefaultLoginSolver()
    }

}


class MiraiConsolePureLoader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MiraiConsole.start(MiraiConsoleUIPure)
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
            })
        }
    }
}