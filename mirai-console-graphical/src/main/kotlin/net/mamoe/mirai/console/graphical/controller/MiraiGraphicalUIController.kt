package net.mamoe.mirai.console.graphical.controller

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.stage.Modality
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.graphical.model.BotModel
import net.mamoe.mirai.console.graphical.model.ConsoleInfo
import net.mamoe.mirai.console.graphical.model.PluginModel
import net.mamoe.mirai.console.graphical.model.VerificationCodeModel
import net.mamoe.mirai.console.graphical.view.VerificationCodeFragment
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.utils.LoginSolver
import tornadofx.*

class MiraiGraphicalUIController : Controller(), MiraiConsoleUI {

    private val loginSolver = GraphicalLoginSolver()
    private val cache = mutableMapOf<Long, BotModel>()
    val mainLog = observableListOf<String>()


    val botList = observableListOf<BotModel>()
    val pluginList: ObservableList<PluginModel> by lazy(::getPluginsFromConsole)

    val consoleInfo = ConsoleInfo()

    fun login(qq: String, psd: String) {
        MiraiConsole.CommandProcessor.runConsoleCommandBlocking("/login $qq $psd")
    }

    fun sendCommand(command: String) = MiraiConsole.CommandProcessor.runConsoleCommandBlocking(command)

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

    override suspend fun requestInput(): String {
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

    override fun createLoginSolver(): LoginSolver = loginSolver

    private fun getPluginsFromConsole(): ObservableList<PluginModel> =
        MiraiConsole.pluginManager.getAllPluginDescriptions().map(::PluginModel).toObservable()

}

class GraphicalLoginSolver : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: IoBuffer): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}