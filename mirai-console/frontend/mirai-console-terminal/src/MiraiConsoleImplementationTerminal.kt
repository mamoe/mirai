/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
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


import kotlinx.coroutines.*
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.fontend.DownloadingProgress
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.terminal.ConsoleInputImpl.requestInput
import net.mamoe.mirai.console.terminal.noconsole.AllEmptyLineReader
import net.mamoe.mirai.console.terminal.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.*
import org.fusesource.jansi.Ansi
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.LineReaderImpl
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.AbstractWindowsTerminal
import org.jline.utils.AttributedString
import org.jline.utils.Display
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.reflect.KProperty

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
) : MiraiConsoleImplementation, CoroutineScope by CoroutineScope(
    SupervisorJob() + CoroutineName("MiraiConsoleImplementationTerminal") +
            CoroutineExceptionHandler { coroutineContext, throwable ->
                if (throwable is CancellationException) {
                    return@CoroutineExceptionHandler
                }
                val coroutineName = coroutineContext[CoroutineName]?.name ?: "<unnamed>"
                MiraiConsole.mainLogger.error("Exception in coroutine $coroutineName", throwable)
            }) {
    override val jvmPluginLoader: JvmPluginLoader by lazy { backendAccess.createDefaultJvmPluginLoader(coroutineContext) }
    override val commandManager: CommandManager by lazy { backendAccess.createDefaultCommandManager(coroutineContext) }
    override val consoleInput: ConsoleInput get() = ConsoleInputImpl
    override val isAnsiSupported: Boolean get() = true
    override val consoleDataScope: MiraiConsoleImplementation.ConsoleDataScope by lazy {
        MiraiConsoleImplementation.ConsoleDataScope.createDefault(
            coroutineContext,
            dataStorageForBuiltIns,
            configStorageForBuiltIns
        )
    }

    // used in test
    internal val logService: LoggingService

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver {
        LoginSolver.Default?.takeIf { it !is StandardCharImageLoginSolver }?.let { return it }
        return StandardCharImageLoginSolver(input = { requestInput("LOGIN> ") })
    }

    override fun createLogger(identity: String?): MiraiLogger {
        return PlatformLogger(identity = identity, output = { line ->
            val text = line + ANSI_RESET
            prePrintNewLog()
            lineReader.printAbove(text)
            postPrintNewLog()
            logService.pushLine(text)
        })
    }

    init {
        with(rootPath.toFile()) {
            mkdir()
            require(isDirectory) { "rootDir $absolutePath is not a directory" }
            logService = if (ConsoleTerminalSettings.noLogging) {
                LoggingServiceNoop()
            } else {
                LoggingServiceI(childScope("Log Service")).also { service ->
                    service.startup(resolve("logs"))
                }
            }
        }
    }

    override val consoleLaunchOptions: MiraiConsoleImplementation.ConsoleLaunchOptions
        get() = ConsoleTerminalSettings.launchOptions

    override fun preStart() {
        registerSignalHandler()
        overrideSTD(this)
        launch(CoroutineName("Mirai Console Terminal Downloading Progress Bar Updater")) {
            while (isActive) {
                downloadingProgressDaemonStub()
            }
        }
    }

    override fun createNewDownloadingProgress(): DownloadingProgress {
        if (terminal is NoConsole) return super.createNewDownloadingProgress()

        containDownloadingProgress = true
        kotlin.runCatching {
            downloadingProgressCoroutine?.resumeWith(Result.success(Unit))
        }
        return TerminalDownloadingProgress(lineReader).also { terminalDownloadingProgresses.add(it) }
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

val terminalDisplay: Display by object : kotlin.properties.ReadOnlyProperty<Any?, Display> {
    val delegate: () -> Display by lazy {
        val terminal = terminal
        if (terminal is NoConsole) {
            val display = Display(terminal, false)
            return@lazy { display }
        }

        val lr = lineReader
        val field = LineReaderImpl::class.java.declaredFields.first { it.type == Display::class.java }
        field.isAccessible = true
        return@lazy { field[lr] as Display }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Display {
        return delegate()
    }
}

internal val terminalExecuteLock: java.util.concurrent.locks.Lock by lazy {
    val terminal = terminal
    if (terminal is NoConsole) return@lazy java.util.concurrent.locks.ReentrantLock()
    val lr = lineReader
    val field = LineReaderImpl::class.java.declaredFields.first {
        java.util.concurrent.locks.Lock::class.java.isAssignableFrom(it.type)
    }
    field.isAccessible = true
    field[lr].cast()
}
private val terminalDownloadingProgressesNoticer = Object()
private var containDownloadingProgress: Boolean = false

internal val terminalDownloadingProgresses = mutableListOf<TerminalDownloadingProgress>()
private var downloadingProgressCoroutine: Continuation<Unit>? = null
private suspend fun downloadingProgressDaemonStub() {
    delay(500L)
    if (containDownloadingProgress) {
        updateTerminalDownloadingProgresses()
    } else {
        suspendCancellableCoroutine<Unit> { cp ->
            downloadingProgressCoroutine = cp
        }
        downloadingProgressCoroutine = null
    }
}

internal fun updateTerminalDownloadingProgresses() {
    if (!containDownloadingProgress) return

    runCatching { downloadingProgressCoroutine?.resumeWith(Result.success(Unit)) }

    terminalExecuteLock.withLock {
        if (terminalDownloadingProgresses.isNotEmpty()) {
            val wid = terminal.width
            if (wid == 0) { // Run in idea
                if (terminalDownloadingProgresses.removeIf { it.pendingErase }) {
                    updateTerminalDownloadingProgresses()
                    return
                }
                terminalDisplay.update(listOf(AttributedString.EMPTY), 0, false)
                // Error in idea when more than one bar displaying
                terminalDisplay.update(listOf(terminalDownloadingProgresses[0].let {
                    it.updateTxt(0); it.ansiMsg
                }), 0)
            } else {
                if (terminalDownloadingProgresses.size > 4) {
                    // to mush. delete some completed status
                    var allowToDelete = terminalDownloadingProgresses.size - 4
                    terminalDownloadingProgresses.removeIf { pg ->
                        if (allowToDelete == 0) {
                            return@removeIf false
                        }
                        if (pg.pendingErase) {
                            allowToDelete--
                            return@removeIf true
                        }
                        return@removeIf false
                    }
                }
                terminalDisplay.update(terminalDownloadingProgresses.map {
                    it.updateTxt(wid); it.ansiMsg
                }, 0)
                cleanupErase()
            }
        } else {
            terminalDisplay.update(emptyList(), 0)
            (lineReader as LineReaderImpl).let { lr ->
                if (lr.isReading) {
                    lr.redisplay()
                }
            }
            containDownloadingProgress = false
            noticeDownloadingProgressEmpty()
        }
    }
}

internal fun prePrintNewLog() {
    if (!containDownloadingProgress) return
    if (terminalDownloadingProgresses.isNotEmpty()) {
        terminalExecuteLock.withLock {
            terminalDisplay.update(emptyList(), 0)
        }
    }
}

internal fun cleanupErase() {
    val now = currentTimeMillis()
    terminalDownloadingProgresses.removeIf { pg ->
        if (!pg.pendingErase) return@removeIf false
        if (now > pg.eraseTimestamp) {
            pg.ansiMsg = AttributedString.EMPTY
            return@removeIf true
        }
        return@removeIf false
    }
}

internal fun postPrintNewLog() {
    if (!containDownloadingProgress) return
    updateTerminalDownloadingProgresses()
    cleanupErase()
}

private fun noticeDownloadingProgressEmpty() {
    synchronized(terminalDownloadingProgressesNoticer) {
        terminalDownloadingProgressesNoticer.notifyAll()
    }
}

internal fun waitDownloadingProgressEmpty() {
    synchronized(terminalDownloadingProgressesNoticer) {
        if (containDownloadingProgress || terminalDownloadingProgresses.isNotEmpty()) {
            terminalDownloadingProgressesNoticer.wait()
        }
    }
}

val terminal: Terminal = run {
    if (ConsoleTerminalSettings.noConsole) return@run NoConsole

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
