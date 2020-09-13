/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:OptIn(ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MalformedMiraiConsoleImplementationError
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.Command.Companion.primaryName
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.extensions.PostStartupExtension
import net.mamoe.mirai.console.extensions.SingletonExtensionSelector
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.ConsoleDataScope
import net.mamoe.mirai.console.internal.data.castOrNull
import net.mamoe.mirai.console.internal.extension.BuiltInSingletonExtensionSelector
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.internal.util.autoHexToBytes
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.grantPermission
import net.mamoe.mirai.console.permission.RootPermission
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.AbstractJvmPlugin
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.utils.*
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

/**
 * [MiraiConsole] 公开 API 与前端实现的连接桥.
 */
@Suppress("SpellCheckingInspection")
internal object MiraiConsoleImplementationBridge : CoroutineScope, MiraiConsoleImplementation,
    MiraiConsole {
    override val pluginCenter: PluginCenter get() = throw UnsupportedOperationException("PluginCenter is not supported yet")

    private val instance: MiraiConsoleImplementation by MiraiConsoleImplementation.Companion::instance
    override val buildDate: Instant by MiraiConsoleBuildConstants::buildDate
    override val version: Semver by MiraiConsoleBuildConstants::version
    override val rootPath: Path by instance::rootPath
    override val frontEndDescription: MiraiConsoleFrontEndDescription by instance::frontEndDescription

    override val mainLogger: MiraiLogger by lazy {
        createLogger("main")
    }
    override val coroutineContext: CoroutineContext by instance::coroutineContext
    override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> by instance::builtInPluginLoaders
    override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl by instance::consoleCommandSender

    override val dataStorageForJvmPluginLoader: PluginDataStorage by instance::dataStorageForJvmPluginLoader
    override val configStorageForJvmPluginLoader: PluginDataStorage by instance::configStorageForJvmPluginLoader
    override val dataStorageForBuiltIns: PluginDataStorage by instance::dataStorageForBuiltIns
    override val configStorageForBuiltIns: PluginDataStorage by instance::configStorageForBuiltIns
    override val consoleInput: ConsoleInput by instance::consoleInput

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver =
        instance.createLoginSolver(requesterBot, configuration)

    init {
        DefaultLogger = this::createLogger
    }

    override fun createLogger(identity: String?): MiraiLogger = instance.createLogger(identity)

    @Suppress("RemoveRedundantBackticks")
    internal fun doStart() {
        phase `greeting`@{
            val buildDateFormatted =
                buildDate.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            mainLogger.info { "Starting mirai-console..." }
            mainLogger.info { "Backend: version $version, built on $buildDateFormatted." }
            mainLogger.info { frontEndDescription.render() }
        }

        phase `check coroutineContext`@{
            if (coroutineContext[Job] == null) {
                throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
            }
            if (coroutineContext[CoroutineExceptionHandler] == null) {
                throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a CoroutineExceptionHandler in it.")
            }

            MiraiConsole.job.invokeOnCompletion {
                Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
            }
        }

        ConsoleInput

        // start

        phase `load configurations`@{
            mainLogger.verbose { "Loading configurations..." }
            ConsoleDataScope.reloadAll()
        }

        phase `initialize all plugins`@{
            PluginManager // init

            mainLogger.verbose { "Loading JVM plugins..." }
            PluginManagerImpl.loadAllPluginsUsingBuiltInLoaders()
            PluginManagerImpl.initExternalPluginLoaders().let { count ->
                mainLogger.verbose { "$count external PluginLoader(s) found. " }
                if (count != 0) {
                    mainLogger.verbose { "Loading external plugins..." }
                }
            }
        }

        phase `load all plugins`@{
            PluginManagerImpl.loadPlugins(PluginManagerImpl.scanPluginsUsingPluginLoadersIncludingThoseFromPluginLoaderProvider())

            mainLogger.verbose { "${PluginManager.plugins.size} plugin(s) loaded." }
        }

        phase `collect extensions`@{
            for (resolvedPlugin in PluginManagerImpl.resolvedPlugins) {
                resolvedPlugin.castOrNull<AbstractJvmPlugin>()?.let {
                    GlobalComponentStorage.mergeWith(it.componentStorage)
                }
            }
        }

        phase `load SingletonExtensionSelector`@{
            SingletonExtensionSelector.init()
            val instance = SingletonExtensionSelector.instance
            if (instance is BuiltInSingletonExtensionSelector) {
                ConsoleDataScope.addAndReloadConfig(instance.config)
            }
        }

        phase `load PermissionService`@{
            mainLogger.verbose { "Loading PermissionService..." }

            PermissionService.instanceField = GlobalComponentStorage.run {
                PermissionServiceProvider.findSingletonInstance(BuiltInPermissionService)
            }

            PermissionService.INSTANCE.let { ps ->
                if (ps is BuiltInPermissionService) {
                    ConsoleDataScope.addAndReloadConfig(ps.config)
                    mainLogger.verbose { "Reloaded PermissionService settings." }
                }
            }

            ConsoleCommandSender.grantPermission(RootPermission)
        }

        phase `prepare commands`@{
            mainLogger.verbose { "Loading built-in commands..." }
            BuiltInCommands.registerAll()
            mainLogger.verbose { "Prepared built-in commands: ${BuiltInCommands.all.joinToString { it.primaryName }}" }
            CommandManager
            CommandManagerImpl.commandListener // start
        }

        phase `enable plugins`@{
            mainLogger.verbose { "Enabling plugins..." }

            PluginManagerImpl.enableAllLoadedPlugins()

            mainLogger.info { "${PluginManagerImpl.plugins.size} plugin(s) enabled." }
        }

        phase `auto-login bots`@{
            runBlocking {
                for ((id, password) in AutoLoginConfig.plainPasswords.filterNot { it.key == 123456654321L }) {
                    mainLogger.info { "Auto-login $id" }
                    MiraiConsole.addBot(id, password).alsoLogin()
                }

                for ((id, password) in AutoLoginConfig.md5Passwords.filterNot { it.key == 123456654321L }) {
                    mainLogger.info { "Auto-login $id" }
                    val x = runCatching {
                        password.autoHexToBytes()
                    }.getOrElse {
                        error("Bad auto-login md5: '$password'")
                    }
                    MiraiConsole.addBot(id, x).alsoLogin()
                }
            }
        }

        GlobalComponentStorage.run {
            PostStartupExtension.useExtensions { it() }
        }

        mainLogger.info { "mirai-console started successfully." }
    }

    @Suppress("SpellCheckingInspection")
    @Retention(AnnotationRetention.SOURCE)
    @DslMarker
    private annotation class ILoveOmaeKumikoForever

    @ILoveOmaeKumikoForever
    private inline fun phase(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        block()
    }
}