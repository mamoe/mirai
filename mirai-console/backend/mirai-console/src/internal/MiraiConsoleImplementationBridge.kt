/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import me.him188.kotlin.dynamic.delegation.dynamicDelegation
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MalformedMiraiConsoleImplementationError
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.SpaceSeparatedCommandCallParser
import net.mamoe.mirai.console.command.resolve.BuiltInCommandCallResolver
import net.mamoe.mirai.console.extensions.CommandCallParserProvider
import net.mamoe.mirai.console.extensions.CommandCallResolverProvider
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.extensions.PostStartupExtension
import net.mamoe.mirai.console.internal.command.CommandConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.ConfigurationKey
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind.MD5
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig.Account.PasswordKind.PLAIN
import net.mamoe.mirai.console.internal.data.builtins.LoggerConfig
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorageImpl
import net.mamoe.mirai.console.internal.logging.LoggerControllerImpl
import net.mamoe.mirai.console.internal.logging.MiraiConsoleLogger
import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.internal.util.autoHexToBytes
import net.mamoe.mirai.console.internal.util.runIgnoreException
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.permission.RootPermission
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.utils.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

internal val MiraiConsole.pluginManagerImpl: PluginManagerImpl get() = this.pluginManager.cast()

/**
 * [MiraiConsole] 公开 API 与前端实现的连接桥.
 */
@Suppress("SpellCheckingInspection")
internal class MiraiConsoleImplementationBridge(
    private val externalImplementation: MiraiConsoleImplementation,
) : MiraiConsole,
    MiraiConsoleImplementation by (dynamicDelegation(MiraiConsoleImplementationBridge::externalImplementation)) {
    override val origin: MiraiConsoleImplementation
        get() = externalImplementation

    // FIXME: 12/12/2021 Workaround for compiler regression, should remove when using Kotlin compiller 1.6.20
    private operator fun <V> KProperty0<V>.getValue(thisRef: Any?, property: KProperty<*>): V = this.get()

    override val buildDate: Instant by MiraiConsoleBuildConstants::buildDate
    override val version: SemVersion by MiraiConsoleBuildConstants::version
    override val pluginManager: PluginManagerImpl by lazy { PluginManagerImpl(coroutineContext) }

    // used internally
    val globalComponentStorage: GlobalComponentStorageImpl by lazy { GlobalComponentStorageImpl() }

    override val mainLogger: MiraiLogger by lazy { createLogger("main") }

    init {
        // TODO: Replace to standard api
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        DefaultFactoryOverrides.override { requester, identity ->
            return@override createLogger(
                identity ?: requester.kotlin.simpleName ?: requester.simpleName
            )
        }
    }


    override fun createLogger(identity: String?): MiraiLogger {
        val controller = loggerController
        return MiraiConsoleLogger(controller, externalImplementation.createLogger(identity))
    }

    @Suppress("RemoveRedundantBackticks")
    internal fun doStart() {
        externalImplementation.preStart()

        @OptIn(ExperimentalCommandDescriptors::class)
        phase("register builtin componenets") {
            GlobalComponentStorage.run {
                contributeConsole(CommandCallParserProvider, SpaceSeparatedCommandCallParser.Provider)
                contributeConsole(CommandCallResolverProvider, BuiltInCommandCallResolver.Provider)
                contributeConsole(PermissionServiceProvider, BuiltInPermissionService.Provider())
            }
        }

        phase("setup logger controller") {
            if (loggerController === LoggerControllerImpl) {
                // Reload LoggerConfig.
                consoleDataScope.addAndReloadConfig(LoggerConfig)
                LoggerControllerImpl.initialized = true
            }
        }

        phase("greeting") {
            val buildDateFormatted =
                buildDate.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            mainLogger.info { "Starting mirai-console..." }
            mainLogger.info { "Backend: version $version, built on $buildDateFormatted." }
            mainLogger.info { frontEndDescription.render() }
            mainLogger.info { "Welcome to visit https://mirai.mamoe.net/" }
        }

        phase("check coroutineContext") {
            if (coroutineContext[Job] == null) {
                throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
            }
            if (coroutineContext[CoroutineExceptionHandler] == null) {
                throw MalformedMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a CoroutineExceptionHandler in it.")
            }

            MiraiConsole.job.invokeOnCompletion {
                Bot.instances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
            }
        }

        ConsoleInput

        // start

        phase("load configurations") {
            mainLogger.verbose { "Loading configurations..." }
            consoleDataScope.addAndReloadConfig(CommandConfig)
            consoleDataScope.reloadAll()
        }

        phase("initialize all plugins") {
            pluginManager // init

            mainLogger.verbose { "Loading JVM plugins..." }
            pluginManager.loadAllPluginsUsingBuiltInLoaders()
            pluginManager.initExternalPluginLoaders().let { count ->
                mainLogger.verbose { "$count external PluginLoader(s) found. " }
                if (count != 0) {
                    mainLogger.verbose { "Loading external plugins..." }
                }
            }
        }

        phase("load all plugins") {
            pluginManager.loadPlugins(pluginManager.scanPluginsUsingPluginLoadersIncludingThoseFromPluginLoaderProvider())

            mainLogger.verbose { "${PluginManager.plugins.size} plugin(s) loaded." }
        }

//        phase("load SingletonExtensionSelector") {
//            SingletonExtensionSelector.init()
//            val instance = SingletonExtensionSelector.instance
//            if (instance is SingletonExtensionSelectorImpl) {
//                consoleDataScope.addAndReloadConfig(instance.config)
//            }
//        }


        phase("load PermissionService") {
            mainLogger.verbose { "Loading PermissionService..." }

            PermissionService.INSTANCE.let { ps ->
                if (ps is BuiltInPermissionService) {
                    consoleDataScope.addAndReloadConfig(ps.config)
                    mainLogger.verbose { "Reloaded PermissionService settings." }
                } else {
                    mainLogger.info {
                        "Loaded PermissionService from plugin ${
                            GlobalComponentStorage.getPreferredExtension(
                                PermissionServiceProvider
                            ).plugin?.name
                        }"
                    }
                }
            }

            runIgnoreException<UnsupportedOperationException> { ConsoleCommandSender.permit(RootPermission) }
        }

        phase("prepare commands") {
            mainLogger.verbose { "Loading built-in commands..." }
            BuiltInCommands.registerAll()
            mainLogger.info { "Prepared built-in commands: ${BuiltInCommands.all.joinToString { it.primaryName }}" }
            CommandManager
            // CommandManagerImpl.commandListener // start
        }

        phase("enable plugins") {
            mainLogger.verbose { "Enabling plugins..." }

            pluginManager.enableAllLoadedPlugins()

            for (registeredCommand in CommandManager.allRegisteredCommands) {
                registeredCommand.permission // init
            }

            mainLogger.info { "${pluginManager.plugins.size} plugin(s) enabled." }
        }

        phase("auto-login bots") {
            runBlocking {
                val accounts = AutoLoginConfig.accounts.toList()
                for (account in accounts.filter {
                    it.configuration[ConfigurationKey.enable]?.toString()?.equals("true", true) ?: true
                }) {
                    val id = kotlin.runCatching {
                        account.account.toLong()
                    }.getOrElse {
                        error("Bad auto-login account: '${account.account}'")
                    }
                    if (id == 123456L) continue
                    fun BotConfiguration.configBot() {
                        mainLogger.info { "Auto-login ${account.account}" }

                        account.configuration[ConfigurationKey.protocol]?.let { protocol ->
                            this.protocol = runCatching {
                                BotConfiguration.MiraiProtocol.valueOf(protocol.toString())
                            }.getOrElse {
                                throw IllegalArgumentException(
                                    "Bad auto-login config value for `protocol` for account $id",
                                    it
                                )
                            }
                        }
                        account.configuration[ConfigurationKey.device]?.let { device ->
                            fileBasedDeviceInfo(device.toString())
                        }
                    }

                    val bot = when (account.password.kind) {
                        PLAIN -> {
                            MiraiConsole.addBot(id, account.password.value, BotConfiguration::configBot)
                        }
                        MD5 -> {
                            val md5 = kotlin.runCatching {
                                account.password.value.autoHexToBytes()
                            }.getOrElse {
                                error("Bad auto-login md5: '${account.password.value}' for account $id")
                            }
                            MiraiConsole.addBot(id, md5, BotConfiguration::configBot)
                        }
                    }

                    runCatching { bot.login() }.getOrElse {
                        mainLogger.error(it)
                        bot.close()
                    }
                }

            }
        }

        phase("finally post") {
            globalComponentStorage.useEachExtensions(PostStartupExtension) { it.invoke() }
        }

        externalImplementation.postStart()

        mainLogger.info { "mirai-console started successfully." }
    }

    @Suppress("SpellCheckingInspection")
    @Retention(AnnotationRetention.BINARY)
    @DslMarker
    internal annotation class ILoveOmaeKumikoForever

    /**
     * 表示一个初始化阶段, 无实际作用.
     */
    @ILoveOmaeKumikoForever
    private inline fun phase(phase: String, block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        prePhase(phase)
        block()
        postPhase(phase)
    }

    override fun prePhase(phase: String) {
        externalImplementation.prePhase(phase)
    }

    override fun postPhase(phase: String) {
        externalImplementation.postPhase(phase)
    }
}