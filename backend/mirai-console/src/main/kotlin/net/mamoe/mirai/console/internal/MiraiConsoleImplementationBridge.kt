/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ConsoleExperimentalAPI::class)

package net.mamoe.mirai.console.internal

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MalformedMiraiConsoleImplementationError
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.ConsoleDataScope
import net.mamoe.mirai.console.internal.plugin.CuiPluginCenter
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.utils.*
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

/**
 * [MiraiConsole] 公开 API 与前端实现的连接桥.
 */
internal object MiraiConsoleImplementationBridge : CoroutineScope, MiraiConsoleImplementation,
    MiraiConsole {
    override val pluginCenter: PluginCenter get() = CuiPluginCenter

    private val instance: MiraiConsoleImplementation by MiraiConsoleImplementation.Companion::instance
    override val buildDate: Instant by MiraiConsoleBuildConstants::buildDate
    override val version: Semver by MiraiConsoleBuildConstants::version
    override val rootPath: Path by instance::rootPath
    override val frontEndDescription: MiraiConsoleFrontEndDescription by instance::frontEndDescription

    override val mainLogger: MiraiLogger by lazy {
        createLogger("main")
    }
    override val coroutineContext: CoroutineContext by instance::coroutineContext
    override val builtInPluginLoaders: List<PluginLoader<*, *>> by instance::builtInPluginLoaders
    override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl by instance::consoleCommandSender

    override val dataStorageForJarPluginLoader: PluginDataStorage by instance::dataStorageForJarPluginLoader
    override val configStorageForJarPluginLoader: PluginDataStorage by instance::configStorageForJarPluginLoader
    override val dataStorageForBuiltIns: PluginDataStorage by instance::dataStorageForBuiltIns
    override val configStorageForBuiltIns: PluginDataStorage by instance::configStorageForBuiltIns
    override val consoleInput: ConsoleInput by instance::consoleInput

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver =
        instance.createLoginSolver(requesterBot, configuration)

    init {
        DefaultLogger = this::createLogger
    }

    override fun createLogger(identity: String?): MiraiLogger = instance.createLogger(identity)

    @OptIn(ConsoleExperimentalAPI::class)
    internal fun doStart() {
        val buildDateFormatted =
            buildDate.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        mainLogger.info { "Starting mirai-console..." }
        mainLogger.info { "Backend: version $version, built on $buildDateFormatted." }
        mainLogger.info { frontEndDescription.render() }

        if (coroutineContext[Job] == null) {
            throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
        }
        if (coroutineContext[CoroutineExceptionHandler] == null) {
            throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a CoroutineExceptionHandler in it.")
        }

        MiraiConsole.job.invokeOnCompletion {
            Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
        }

        mainLogger.info { "Reloading configurations..." }
        ConsoleDataScope.reloadAll()

        BuiltInCommands.registerAll()
        mainLogger.info { "Prepared built-in commands: ${BuiltInCommands.all.joinToString { it.primaryName }}" }
        CommandManagerImpl.commandListener // start

        mainLogger.info { "Loading plugins..." }
        PluginManagerImpl.loadEnablePlugins()
        mainLogger.info { "${PluginManager.plugins.size} plugin(s) loaded." }
        mainLogger.info { "mirai-console started successfully." }

        for ((id, password) in AutoLoginConfig.plainPasswords) {
            mainLogger.info { "Auto-login $id" }
            MiraiConsole.addBot(id, password)
        }

        for ((id, password) in AutoLoginConfig.md5Passwords) {
            mainLogger.info { "Auto-login $id" }
            MiraiConsole.addBot(id, password)
        }

        // Only for initialize
    }
}