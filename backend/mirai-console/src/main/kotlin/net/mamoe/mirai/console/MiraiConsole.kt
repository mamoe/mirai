/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.charsets.Charset
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.center.CuiPluginCenter
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.coroutines.CoroutineContext


/**
 * mirai-console 实例
 *
 * @see INSTANCE
 */
interface MiraiConsole : CoroutineScope {
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

    val buildDate: Date

    val version: String

    val pluginCenter: PluginCenter

    @MiraiExperimentalAPI
    fun newLogger(identity: String?): MiraiLogger

    companion object INSTANCE : MiraiConsole by MiraiConsoleInternal
}


//// internal


internal object MiraiConsoleInitializer {
    internal lateinit var instance: IMiraiConsole

    /** 由前端调用 */
    internal fun init(instance: IMiraiConsole) {
        this.instance = instance
        MiraiConsoleInternal.initialize()
    }
}

internal object MiraiConsoleBuildConstants { // auto-filled on build (task :mirai-console:fillBuildConstants)
    @JvmStatic
    val buildDate: Date = Date(1592799753404L) // 2020-06-22 12:22:33
    const val version: String = "0.5.1"
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

    @MiraiExperimentalAPI
    override val mainLogger: MiraiLogger
        get() = instance.mainLogger
    override val coroutineContext: CoroutineContext get() = instance.coroutineContext
    override val builtInPluginLoaders: List<PluginLoader<*, *>> get() = instance.builtInPluginLoaders
    override val consoleCommandOwner: ConsoleCommandOwner get() = instance.consoleCommandOwner
    override val consoleCommandSender: ConsoleCommandSender get() = instance.consoleCommandSender

    init {
        DefaultLogger = { identity -> this.newLogger(identity) }
    }

    @MiraiExperimentalAPI
    override fun newLogger(identity: String?): MiraiLogger = frontEnd.loggerFor(identity)

    internal fun initialize() {
        if (coroutineContext[Job] == null) {
            throw IllegalMiraiConsoleImplementationError("The coroutineContext given to MiraiConsole must have a Job in it.")
        }
        this.coroutineContext[Job]!!.invokeOnCompletion {
            Bot.botInstances.forEach { kotlin.runCatching { it.close() }.exceptionOrNull()?.let(mainLogger::error) }
        }
        // Only for initialize
    }
}

class IllegalMiraiConsoleImplementationError(
    override val message: String?
) : Error()


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

    @Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
    internal val consoleCommandOwner: ConsoleCommandOwner

    @Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION")
    internal val consoleCommandSender: ConsoleCommandSender
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
