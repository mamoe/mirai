package net.mamoe.mirai.console.graphical.controller

import javafx.application.Platform
import javafx.stage.Modality
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleUI
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.model.ConsoleInfo
import net.mamoe.mirai.console.graphical.model.VerificationCodeModel
import net.mamoe.mirai.console.graphical.view.VerificationCodeFragment
import tornadofx.Controller
import tornadofx.Scope
import tornadofx.find
import tornadofx.observableListOf

class MiraiGraphicalUIController : Controller(), MiraiConsoleUI {

    private val cache = mutableMapOf<Long, BotModel>()
    val mainLog = observableListOf<String>()
    val botList = observableListOf<BotModel>()
    val consoleInfo = ConsoleInfo()

    fun login(qq: String, psd: String) {
        MiraiConsole.CommandListener.commandChannel.offer("/login $qq $psd")
    }

    override fun pushLog(identity: Long, message: String) = Platform.runLater {
        when (identity) {
            0L -> mainLog.add(message)
            else -> cache[identity]?.logHistory?.add(message)
        }
    }

    override fun prePushBot(identity: Long) = Platform.runLater {
        BotModel(identity).also {
            cache[identity] = it
            botList.add(it)
        }
    }

    override fun pushBot(bot: Bot) = Platform.runLater {
        cache[bot.uin]?.bot = bot
    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        Platform.runLater {
            consoleInfo.consoleVersion = consoleVersion
            consoleInfo.consoleBuild = consoleBuild
            consoleInfo.coreVersion = coreVersion
        }
    }

    override suspend fun requestInput(question: String): String {
        val model = VerificationCodeModel()
        find<VerificationCodeFragment>(Scope(model)).openModal(
            modality = Modality.APPLICATION_MODAL,
            resizable = false
        )
        return model.code.value
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) = Platform.runLater {
        cache[identity]?.admins?.setAll(admins)
    }
}