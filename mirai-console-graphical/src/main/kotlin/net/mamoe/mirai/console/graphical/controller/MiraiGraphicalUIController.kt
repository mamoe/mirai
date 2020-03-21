package net.mamoe.mirai.console.graphical.controller

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.stage.Modality
import javafx.stage.StageStyle
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.graphical.model.*
import net.mamoe.mirai.console.graphical.view.VerificationCodeFragment
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import tornadofx.*

class MiraiGraphicalUIController : Controller(), MiraiConsoleUI {

    private val settingModel = find<GlobalSettingModel>()
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
        fun ObservableList<*>.trim() {
            if (size > settingModel.item.maxLongNum) {
                this.removeAt(0)
            }
        }

        when (identity) {
            0L -> mainLog.apply {
                add(message)
                mainLog.trim()
            }
            else -> cache[identity]?.logHistory?.apply {
                add(message)
                trim()
            }
        }
    }

    // 修改interface之后用来暂时占位
    override fun pushLog(priority: LogPriority, identityStr: String, identity: Long, message: String) {
        this.pushLog(identity, message)
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

    override suspend fun requestInput(hint: String): String {
        // TODO: 2020/3/21 HINT
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
        PluginManager.getAllPluginDescriptions().map(::PluginModel).toObservable()

}

class GraphicalLoginSolver : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        val code = VerificationCodeModel(VerificationCode(data))

        // 界面需要运行在主线程
        Platform.runLater {
            find<VerificationCodeFragment>(Scope(code)).openModal(
                stageStyle = StageStyle.UNDECORATED,
                escapeClosesWindow = false,
                modality = Modality.NONE,
                resizable = false,
                block = true
            )
        }

        // 阻塞协程直到验证码已经输入
        while (code.isDirty || code.code.value == null) {
            delay(1000)
            if (code.code.value === VerificationCodeFragment.MAGIC_KEY) {
                throw WrongPasswordException("取消登录")
            }
        }
        return code.code.value
    }


    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}