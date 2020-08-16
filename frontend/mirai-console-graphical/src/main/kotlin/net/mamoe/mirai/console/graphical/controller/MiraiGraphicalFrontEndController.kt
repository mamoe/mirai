/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.controller

import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.stage.Modality
import javafx.stage.StageStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandManager.runCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.graphical.event.ReloadEvent
import net.mamoe.mirai.console.graphical.model.*
import net.mamoe.mirai.console.graphical.view.dialog.InputDialog
import net.mamoe.mirai.console.graphical.view.dialog.VerificationCodeFragment
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.console.utils.MiraiConsoleFrontEnd
import net.mamoe.mirai.network.CustomLoginFailedException
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import tornadofx.Controller
import tornadofx.Scope
import tornadofx.find
import tornadofx.observableListOf
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.coroutines.resume

class MiraiGraphicalFrontEndController : Controller(), MiraiConsoleFrontEnd {

    private val settingModel = find<GlobalSettingModel>()
    private val loginSolver = GraphicalLoginSolver()
    private val cache = mutableMapOf<Long, BotModel>()
    val mainLog = observableListOf<Pair<String, String>>()


    val botList = observableListOf<BotModel>()
    val pluginList: ObservableList<PluginModel> by lazy(::getPluginsFromConsole)

    private val consoleInfo = ConsoleInfo()

    internal val sdf by lazy { SimpleDateFormat("HH:mm:ss") }

    init {
        // 监听插件重载事件，以重新从console获取插件列表
        subscribe<ReloadEvent> {
            pluginList.clear()

            // 不能直接赋值，pluginList已经被bind，不能更换对象
            pluginList.addAll(getPluginsFromConsole())
        }
    }

    fun login(qq: String, psd: String) {
        CommandManager.runCommand(ConsoleCommandSender, "login $qq $psd")
    }

    fun logout(qq: Long) {
        cache.remove(qq)?.apply {
            botList.remove(this)
            if (botProperty.value != null && bot.isActive) {
                bot.close()
            }
        }
    }

    fun sendCommand(command: String) = runCommand(ConsoleCommandSender, command)


    private val mainLogger = SimpleLogger(null) { priority: LogPriority, message: String?, e: Throwable? ->
        Platform.runLater {
            val time = sdf.format(Date())
            mainLog.apply {
                add("[$time] $message" to priority.name)
                trim()
            }
        }
    }

    override fun loggerFor(identity: Long): MiraiLogger {
        return if (identity == 0L) return mainLogger
        else cache[identity]?.logger ?: kotlin.error("bot not found: $identity")
    }


    override fun prePushBot(identity: Long) = Platform.runLater {
        if (!cache.containsKey(identity)) {
            BotModel(identity).also {
                cache[identity] = it
                botList.add(it)
            }
        }
    }

    override fun pushBot(bot: Bot) = Platform.runLater {
        cache[bot.id]?.bot = bot
    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        Platform.runLater {
            consoleInfo.consoleVersion = consoleVersion
            consoleInfo.consoleBuild = consoleBuild
            consoleInfo.coreVersion = coreVersion
        }
    }

    override suspend fun requestInput(hint: String): String =
        suspendCancellableCoroutine {
            Platform.runLater {
                it.resume(InputDialog(hint).open())
            }
        }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) = Platform.runLater {
        cache[identity]?.admins?.setAll(admins)
    }

    override fun createLoginSolver(): LoginSolver = loginSolver

    private fun getPluginsFromConsole(): ObservableList<PluginModel> =
        PluginManager.getAllPluginDescriptions().map(::PluginModel).toObservable()

    fun checkUpdate(plugin: PluginModel) {
        pluginList.forEach {
            if (it.name == plugin.name && it.author == plugin.author) {
                if (plugin.version > it.version) {
                    it.expired = true
                    return
                }
            }
        }
    }

    /**
     * return `true` when command is ambiguous
     */
    fun checkAmbiguous(plugin: PluginModel): Boolean {
        plugin.insight?.commands?.forEach { name ->
            CommandManager.commands.forEach {
                if (name == it.name) return true
            }
        } ?: return false
        return false
    }

    internal fun ObservableList<*>.trim() {
        while (size > settingModel.item.maxLongNum) {
            this.removeAt(0)
        }
    }

    fun reloadPlugins() {

        with(PluginManager) {
            reloadPlugins()
        }

        fire(ReloadEvent) // 广播插件重载事件
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
                throw LoginCancelledManuallyException()
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

class LoginCancelledManuallyException : CustomLoginFailedException(true, "取消登录")