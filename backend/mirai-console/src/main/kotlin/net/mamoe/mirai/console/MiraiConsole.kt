/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.charsets.Charset
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.command.BuiltInCommands
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.internal.InternalCommandManager
import net.mamoe.mirai.console.command.primaryName
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManagerImpl
import net.mamoe.mirai.console.plugin.center.CuiPluginCenter
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.utils.ConsoleBuiltInSettingStorage
import net.mamoe.mirai.console.utils.ConsoleExperimentalAPI
import net.mamoe.mirai.console.utils.ConsoleInternalAPI
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


/**
 * mirai-console 实例
 *
 * @see INSTANCE
 */
public interface MiraiConsole : CoroutineScope {
    /**
     * Console 运行路径
     */
    public val rootDir: File

    /**
     * Console 前端接口
     */
    public val frontEnd: MiraiConsoleFrontEnd

    /**
     * 与前端交互所使用的 Logger
     */
    public val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JarPluginLoader]
     */
    public val builtInPluginLoaders: List<PluginLoader<*, *>>

    public val buildDate: Date

    public val version: String

    public val pluginCenter: PluginCenter

    @ConsoleExperimentalAPI
    public fun newLogger(identity: String?): MiraiLogger

    public companion object INSTANCE : MiraiConsole by MiraiConsoleInternal
}

public class IllegalMiraiConsoleImplementationError(
    override val message: String?
) : Error()

/**
 * 获取 [MiraiConsole] 的 [Job]
 */
public val MiraiConsole.job: Job
    get() = this.coroutineContext[Job] ?: error("Internal error: Job not found in MiraiConsole.coroutineContext")

//// internal


internal object MiraiConsoleInitializer {
    internal lateinit var instance: IMiraiConsole

    /** 由前端调用 */
    internal fun init(instance: IMiraiConsole) {
        this.instance = instance
        MiraiConsoleInternal.doStart()
    }
}

internal object MiraiConsoleBuildConstants { // auto-filled on build (task :mirai-console:fillBuildConstants)
    @JvmStatic
    val buildDate: Date = Date(1595136353901L) // 2020-07-19 13:25:53
    const val version: String = "1.0-dev-4"
}

/**
 * mirai 控制台实例.
 */
internal object MiraiConsoleInternal : CoroutineScope, IMiraiConsole, MiraiConsole {
    override val pluginCenter: PluginCenter get() = CuiPluginCenter

    private val instance: IMiraiConsole get() = MiraiConsoleInitializer.instance
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

    internal fun doStart() {
        val buildDateFormatted = SimpleDateFormat("yyyy-MM-dd").format(buildDate)
        mainLogger.info { "Starting mirai-console..." }
        mainLogger.info { "Backend: version $version, built on $buildDateFormatted." }
        mainLogger.info { "Frontend ${frontEnd.name}: version $version." }

        if (coroutineContext[Job] == null) {
            throw IllegalMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
        }
        job.invokeOnCompletion {
            Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
        }

        BuiltInCommands.registerAll()
        mainLogger.info { "Preparing built-in commands: ${BuiltInCommands.all.joinToString { it.primaryName }}" }
        InternalCommandManager.commandListener // start

        mainLogger.info { "Loading plugins..." }
        PluginManagerImpl.loadEnablePlugins()
        mainLogger.info { "${PluginManager.plugins.size} plugin(s) loaded." }
        mainLogger.info { "mirai-console started successfully." }

        ConsoleBuiltInSettingStorage // init
        // Only for initialize
    }
}


// 前端使用
internal interface IMiraiConsole : CoroutineScope {
    /**
     * Console 运行路径
     */
    val rootDir: File

    /**
     * Console 前端接口
     */
    val frontEnd: MiraiConsoleFrontEnd

    /**
     * 与前端交互所使用的 Logger
     */
    val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JarPluginLoader]
     */
    val builtInPluginLoaders: List<PluginLoader<*, *>>

    val consoleCommandSender: ConsoleCommandSender

    val settingStorageForJarPluginLoader: SettingStorage
    val settingStorageForBuiltIns: SettingStorage
}

/**
 * Included in kotlin stdlib 1.4
 */
internal val Throwable.stacktraceString: String
    get() =
        ByteArrayOutputStream().apply {
            printStackTrace(PrintStream(this))
        }.use { it.toByteArray().encodeToString() }


@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.encodeToString(charset: Charset = Charsets.UTF_8): String =
    kotlinx.io.core.String(this, charset = charset)
