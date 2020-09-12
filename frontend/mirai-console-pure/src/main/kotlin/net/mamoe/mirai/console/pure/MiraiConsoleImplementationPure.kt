/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
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
@file:OptIn(ConsoleInternalApi::class, ConsoleFrontEndImplementation::class, ConsolePureExperimentalApi::class)

package net.mamoe.mirai.console.pure


import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.*
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.pure.ConsoleInputImpl.requestInput
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.pure.noconsole.AllEmptyLineReader
import net.mamoe.mirai.console.pure.noconsole.NoConsole
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.NamedSupervisorJob
import net.mamoe.mirai.utils.*
import org.fusesource.jansi.Ansi
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * mirai-console-pure 后端实现
 *
 * @see MiraiConsolePureLoader CLI 入口点
 */
@ConsoleExperimentalApi
class MiraiConsoleImplementationPure
@JvmOverloads constructor(
    override val rootPath: Path = Paths.get(".").toAbsolutePath(),
    override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader }),
    override val frontEndDescription: MiraiConsoleFrontEndDescription = ConsoleFrontEndDescImpl,
    override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl = ConsoleCommandSenderImplPure,
    override val dataStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val dataStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val configStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
    override val configStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
) : MiraiConsoleImplementation, CoroutineScope by CoroutineScope(
    NamedSupervisorJob("MiraiConsoleImplementationPure") +
            CoroutineExceptionHandler { coroutineContext, throwable ->
                if (throwable is CancellationException) {
                    return@CoroutineExceptionHandler
                }
                val coroutineName = coroutineContext[CoroutineName]?.name ?: "<unnamed>"
                MiraiConsole.mainLogger.error("Exception in coroutine $coroutineName", throwable)
            }) {
    override val consoleInput: ConsoleInput get() = ConsoleInputImpl

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver {
        return DefaultLoginSolver(input = { requestInput("LOGIN> ") })
    }

    override fun createLogger(identity: String?): MiraiLogger = LoggerCreator(identity)

    init {
        with(rootPath.toFile()) {
            mkdir()
            require(isDirectory) { "rootDir $absolutePath is not a directory" }
        }
    }
}

private object ConsoleInputImpl : ConsoleInput {
    private val format = DateTimeFormatter.ofPattern("HH:mm:ss")

    override suspend fun requestInput(hint: String): String {
        return withContext(Dispatchers.IO) {
            lineReader.readLine(
                if (hint.isNotEmpty()) {
                    lineReader.printAbove(
                        Ansi.ansi()
                            .fgCyan().a(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(format))
                            .a(" ")
                            .fgMagenta().a(hint)
                            .reset()
                            .toString()
                    )
                    "$hint > "
                } else "> "
            )
        }
    }
}

val lineReader: LineReader by lazy {
    if (ConsolePureSettings.noConsole) return@lazy AllEmptyLineReader

    LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(NullCompleter())
        .build()
}

val terminal: Terminal = run {
    if (ConsolePureSettings.noConsole) return@run NoConsole

    val dumb = System.getProperty("java.class.path")
        .contains("idea_rt.jar") || System.getProperty("mirai.idea") !== null || System.getenv("mirai.idea") !== null

    runCatching {
        TerminalBuilder.builder()
            .dumb(dumb)
            .build()
    }.recoverCatching {
        TerminalBuilder.builder()
            .jansi(true)
            .build()
    }.recoverCatching {
        TerminalBuilder.builder()
            .system(true)
            .build()
    }.getOrThrow()
}

private object ConsoleFrontEndDescImpl : MiraiConsoleFrontEndDescription {
    override val name: String get() = "Pure"
    override val vendor: String get() = "Mamoe Technologies"
    override val version: Semver = net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
}

private val ANSI_RESET = Ansi().reset().toString()

internal val LoggerCreator: (identity: String?) -> MiraiLogger = {
    PlatformLogger(identity = it, output = { line ->
        lineReader.printAbove(line + ANSI_RESET)
    })
}
