/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ConsoleExperimentalAPI::class)

package net.mamoe.mirai.console.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.IllegalMiraiConsoleImplementationError
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandManagerImpl
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.internal.plugin.CuiPluginCenter
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.internal.utils.ConsoleBuiltInSettingStorage
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * [MiraiConsole] 公开 API 与前端实现的连接桥.
 */
internal object MiraiConsoleImplementationBridge : CoroutineScope, MiraiConsoleImplementation,
    MiraiConsole {
    override val pluginCenter: PluginCenter get() = CuiPluginCenter

    private val instance: MiraiConsoleImplementation get() = MiraiConsoleImplementation.instance
    override val buildDate: Date get() = MiraiConsoleBuildConstants.buildDate
    override val version: String get() = MiraiConsoleBuildConstants.version
    override val rootDir: File get() = instance.rootDir
    override val frontEnd: MiraiConsoleFrontEnd get() = instance.frontEnd

    @ConsoleExperimentalAPI
    override val mainLogger: MiraiLogger
        get() = instance.mainLogger
    override val coroutineContext: CoroutineContext get() = instance.coroutineContext
    override val builtInPluginLoaders: List<PluginLoader<*, *>> get() = instance.builtInPluginLoaders
    override val consoleCommandSender: ConsoleCommandSender get() = instance.consoleCommandSender

    override val settingStorageForJarPluginLoader: SettingStorage get() = instance.settingStorageForJarPluginLoader
    override val settingStorageForBuiltIns: SettingStorage get() = instance.settingStorageForBuiltIns

    init {
        DefaultLogger = { identity -> this.newLogger(identity) }
    }

    @ConsoleExperimentalAPI
    override fun newLogger(identity: String?): MiraiLogger = frontEnd.loggerFor(identity)

    @OptIn(ConsoleExperimentalAPI::class)
    internal fun doStart() {
        val buildDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(buildDate)
        mainLogger.info { "Starting mirai-console..." }
        mainLogger.info { "Backend: version $version, built on $buildDateFormatted." }
        mainLogger.info { "Frontend ${frontEnd.name}: version $version." }

        if (coroutineContext[Job] == null) {
            throw IllegalMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
        }
        MiraiConsole.job.invokeOnCompletion {
            Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
        }

        BuiltInCommands.registerAll()
        mainLogger.info { "Preparing built-in commands: ${BuiltInCommands.all.joinToString { it.primaryName }}" }
        CommandManagerImpl.commandListener // start

        mainLogger.info { "Loading plugins..." }
        PluginManagerImpl.loadEnablePlugins()
        mainLogger.info { "${PluginManager.plugins.size} plugin(s) loaded." }
        mainLogger.info { "mirai-console started successfully." }

        ConsoleBuiltInSettingStorage // init
        // Only for initialize
    }
}