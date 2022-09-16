/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_WARNING",
    "EXPOSED_SUPER_CLASS"
)
@file:OptIn(ConsoleInternalApi::class, ConsoleFrontEndImplementation::class, ConsoleTerminalExperimentalApi::class)

package net.mamoe.mirai.console.terminal


import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.fontend.ProcessProgress
import net.mamoe.mirai.console.frontendbase.AbstractMiraiConsoleFrontendImplementation
import net.mamoe.mirai.console.frontendbase.FrontendBase
import net.mamoe.mirai.console.frontendbase.logging.AllDroppedLogRecorder
import net.mamoe.mirai.console.frontendbase.logging.LogRecorder
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.terminal.ConsoleInputImpl.requestInput
import net.mamoe.mirai.console.terminal.noconsole.AllEmptyLineReader
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.terminal.noconsole.SystemOutputPrintStream
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.StandardCharImageLoginSolver
import org.fusesource.jansi.Ansi
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.AbstractWindowsTerminal
import java.nio.file.Path
import java.nio.file.Paths

/**
 * mirai-console-terminal 后端实现
 *
 * @see MiraiConsoleTerminalLoader CLI 入口点
 */
@ConsoleExperimentalApi
open class MiraiConsoleImplementationTerminal
@JvmOverloads constructor(
    final override val rootPath: Path = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath(),
    override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader }),
    override val frontEndDescription: MiraiConsoleFrontEndDescription = ConsoleFrontEndDescImpl,
    override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl = ConsoleCommandSenderImplTerminal,
    override val dataStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val dataStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val configStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
    override val configStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
) : AbstractMiraiConsoleFrontendImplementation("MiraiConsoleImplementationTerminal") {

    @Suppress("MemberVisibilityCanBePrivate")
    override val frontendBase = object : FrontendBase() {
        override val scope: CoroutineScope
            get() = this@MiraiConsoleImplementationTerminal

        override val workingDirectory: Path
            get() = this@MiraiConsoleImplementationTerminal.rootPath

        override fun initLogRecorder(): LogRecorder {
            if (ConsoleTerminalSettings.noLogging) {
                return AllDroppedLogRecorder
            }
            return super.initLogRecorder()
        }

        override fun initScreen_forwardStdToScreen() {
            lineReader
            super.initScreen_forwardStdToScreen()
        }

        override fun printToScreenDirectly(msg: String) {
            printToScreen(msg)

            @Suppress("DEPRECATION")
            logService.pushLine(msg)
        }
    }

    override val consoleInput: ConsoleInput get() = ConsoleInputImpl
    override val isAnsiSupported: Boolean get() = true

    @Deprecated("Used by iTXTech; for binary compatibility")
    internal val logService: LoggingService

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver {
        LoginSolver.Default?.takeIf { it !is StandardCharImageLoginSolver }?.let { return it }
        return StandardCharImageLoginSolver(input = { requestInput("LOGIN> ") })
    }


    init {
        with(rootPath.toFile()) {
            mkdir()
            require(isDirectory) { "rootDir $absolutePath is not a directory" }

            @Suppress("DEPRECATION")
            logService = LoggingServiceNoop()
        }
    }

    override val consoleLaunchOptions: MiraiConsoleImplementation.ConsoleLaunchOptions
        get() = ConsoleTerminalSettings.launchOptions

    override fun preStart() {
        registerSignalHandler()

        JLineInputDaemon.terminal0 = this
        if (terminal !is NoConsole) {
            frontendBase.newDaemon("JLine Input Daemon", JLineInputDaemon).start()
        }

        launch(CoroutineName("Mirai Console Terminal Downloading Progress Bar Updater")) {
            while (isActive) {
                downloadingProgressDaemonStub()
            }
        }
    }

    override fun createNewProcessProgress(): ProcessProgress {
        if (terminal is NoConsole) return super.createNewProcessProgress()

        containDownloadingProgress = true
        kotlin.runCatching {
            downloadingProgressCoroutine?.resumeWith(Result.success(Unit))
        }
        return TerminalProcessProgress(lineReader).also {
            terminalDownloadingProgresses.add(it)
            terminal.writer().print("\u001B[?25l") // hide cursor
        }
    }
}

val lineReader: LineReader by lazy {
    val terminal = terminal
    if (terminal is NoConsole) return@lazy AllEmptyLineReader

    LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(NullCompleter())
        .build()
}


val terminal: Terminal = run {
    if (ConsoleTerminalSettings.noConsole) {
        SystemOutputPrintStream // init value
        return@run NoConsole
    }

    TerminalBuilder.builder()
        .name("Mirai Console")
        .system(true)
        .jansi(true)
        .dumb(true)
        .paused(true)
        .signalHandler { signalHandler(it.name) }
        .build()
        .let { terminal ->
            if (terminal is AbstractWindowsTerminal) {
                val pumpField = runCatching {
                    AbstractWindowsTerminal::class.java.getDeclaredField("pump").also {
                        it.isAccessible = true
                    }
                }.onFailure { err ->
                    err.printStackTrace()
                    return@let terminal.also { it.resume() }
                }.getOrThrow()
                var response = terminal
                terminal.setOnClose {
                    response = NoConsole
                }
                terminal.resume()
                val pumpThread = pumpField[terminal] as? Thread ?: return@let NoConsole
                @Suppress("ControlFlowWithEmptyBody")
                while (pumpThread.state == Thread.State.NEW);
                Thread.sleep(1000)
                terminal.setOnClose(null)
                return@let response
            }
            terminal.resume()
            terminal
        }
}

private object ConsoleFrontEndDescImpl : MiraiConsoleFrontEndDescription {
    override val name: String get() = "Terminal"
    override val vendor: String get() = "Mamoe Technologies"

    // net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
    // is console's version not frontend's version
    override val version: SemVersion =
        SemVersion(net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.versionConst)
}

internal val ANSI_RESET = Ansi().reset().toString()
