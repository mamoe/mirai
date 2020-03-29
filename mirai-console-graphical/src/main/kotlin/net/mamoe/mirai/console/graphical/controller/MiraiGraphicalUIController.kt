package net.mamoe.mirai.console.graphical.controller

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.stage.Modality
import javafx.stage.StageStyle
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.graphical.model.*
import net.mamoe.mirai.console.graphical.view.dialog.InputDialog
import net.mamoe.mirai.console.graphical.view.dialog.VerificationCodeFragment
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import tornadofx.*
import java.text.SimpleDateFormat
import java.util.*

class MiraiGraphicalUIController : Controller(), MiraiConsoleUI {

    private val settingModel = find<GlobalSettingModel>()
    private val loginSolver = GraphicalLoginSolver()
    private val cache = mutableMapOf<Long, BotModel>()
    val mainLog = observableListOf<String>()


    val botList = observableListOf<BotModel>()
    val pluginList: ObservableList<PluginModel> by lazy(::getPluginsFromConsole)

    val consoleInfo = ConsoleInfo()

    val sdf by lazy {
        SimpleDateFormat("HH:mm:ss")
    }

    fun login(qq: String, psd: String) {
        CommandManager.runCommand(ConsoleCommandSender, "/login $qq $psd")
    }

    fun sendCommand(command: String) = CommandManager.runCommand(ConsoleCommandSender, command)

    override fun pushLog(identity: Long, message: String) = Platform.runLater {
        this.pushLog(LogPriority.INFO, "", identity, message)
    }

    // 修改interface之后用来暂时占位
    override fun pushLog(priority: LogPriority, identityStr: String, identity: Long, message: String) {
        Platform.runLater {

            val time = sdf.format(Date())

            if (identity == 0L) {
                mainLog
            } else {
                cache[identity]?.logHistory
            }?.apply {
                add("[$time] $identityStr $message")
                trim()
            }
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

    override suspend fun requestInput(hint: String): String {
        var ret: String? = null

        // UI必须在UI线程执行，requestInput在协程种被调用
        Platform.runLater {
            ret = InputDialog(hint).open()
        }
        while (ret == null) {
            delay(1000)
        }
        return ret!!
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) = Platform.runLater {
        cache[identity]?.admins?.setAll(admins)
    }

    override fun createLoginSolver(): LoginSolver = loginSolver

    private fun getPluginsFromConsole(): ObservableList<PluginModel> =
        PluginManager.getAllPluginDescriptions().map(::PluginModel).toObservable()


    private fun ObservableList<*>.trim() {
        while (size > settingModel.item.maxLongNum) {
            this.removeAt(0)
        }
    }

}

class GraphicalLoginSolver : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        val code = VerificationCodeModel(VerificationCode(data))

        // UI必须在UI线程执行，requestInput在协程种被调用
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