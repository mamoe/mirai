/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
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
)
@file:OptIn(ConsoleInternalApi::class, ConsoleTerminalExperimentalApi::class, ConsoleFrontEndImplementation::class)

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.*
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.terminal.noconsole.SystemOutputPrintStream
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.childScope
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.verbose
import org.jline.utils.Signals
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.exitProcess

/**
 * mirai-console-terminal CLI 入口点
 */
object MiraiConsoleTerminalLoader {

    // Note: Do not run this in IDEA, as you will get invalid classpath and `java.lang.NoClassDefFoundError`.
    // Run `RunTerminal.kt` under `test` source set instead.
    @OptIn(ConsoleExperimentalApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
        parse(args, exitProcess = true)
        startAsDaemon()
        try {
            runBlocking {
                MiraiConsole.job.invokeOnCompletion { err ->
                    if (err != null) {
                        Thread.sleep(1000) // 保证错误信息打印完全
                    }
                }
                MiraiConsole.job.join()
            }
        } catch (e: CancellationException) {
            // ignored
        }
        // Avoid plugin started some non-daemon threads
        exitProcessAndForceHalt(0)
    }

    @ConsoleTerminalExperimentalApi
    fun printHelpMessage() {
        val help = listOf(
            "" to "Mirai-Console[Terminal FrontEnd] v" + net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.versionConst,
            "" to "             [          BackEnd] v" + kotlin.runCatching {
                net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
            }.getOrElse { "<unknown>" },
            "" to "",
            "--help" to "显示此帮助",
            "" to "",
            "--no-console" to "使用无终端操作环境",
            "--no-logging" to "禁用 console 日志文件",
            "--dont-setup-terminal-ansi" to
                    "[NoConsole] [Windows Only] 不进行ansi console初始化工作",
            "--no-ansi" to "[NoConsole] 禁用 ansi",
            "--safe-reading" to
                    "[NoConsole] 如果启动此选项, console在获取用户输入的时候会获得一个安全的替换符\n" +
                    "            如果不启动, 将会直接 error",
            "--reading-replacement <string>" to
                    "[NoConsole] Console尝试读取命令的替换符, 默认是空字符串\n" +
                    "            使用此选项会自动开启 --safe-reading",
        )
        val prefixPlaceholder = String(CharArray(
            help.maxOfOrNull { it.first.length }!! + 3
        ) { ' ' })

        fun printOption(optionName: String, value: String) {
            if (optionName == "") {
                println(value)
                return
            }
            print(optionName)
            print(prefixPlaceholder.substring(optionName.length))
            val lines = value.split('\n').iterator()
            if (lines.hasNext()) println(lines.next())
            lines.forEach { line ->
                print(prefixPlaceholder)
                println(line)
            }
        }
        help.forEach { (optionName, value) ->
            printOption(optionName, value)
        }
    }

    @ConsoleTerminalExperimentalApi
    fun parse(args: Array<String>, exitProcess: Boolean = false) {
        val iterator = args.iterator()
        while (iterator.hasNext()) {
            when (val option = iterator.next()) {
                "--help" -> {
                    printHelpMessage()
                    if (exitProcess) exitProcess(0)
                    return
                }

                "--no-console" -> {
                    ConsoleTerminalSettings.noConsole = true
                }

                "--dont-setup-terminal-ansi" -> {
                    ConsoleTerminalSettings.setupAnsi = false
                }

                "--no-logging" -> {
                    ConsoleTerminalSettings.noLogging = true
                }

                "--no-ansi" -> {
                    ConsoleTerminalSettings.noAnsi = true
                    ConsoleTerminalSettings.setupAnsi = false
                }

                "--reading-replacement" -> {
                    ConsoleTerminalSettings.noConsoleSafeReading = true
                    if (iterator.hasNext()) {
                        ConsoleTerminalSettings.noConsoleReadingReplacement = iterator.next()
                    } else {
                        println("Bad option `--reading-replacement`")
                        println("Usage: --reading-replacement <string>")
                        if (exitProcess)
                            exitProcess(1)
                        return
                    }
                }

                "--safe-reading" -> {
                    ConsoleTerminalSettings.noConsoleSafeReading = true
                }

                else -> {
                    println("Unknown option `$option`")
                    printHelpMessage()
                    if (exitProcess)
                        exitProcess(1)
                    return
                }
            }
        }
        if (ConsoleTerminalSettings.noConsole)
            SystemOutputPrintStream // Setup Output Channel
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @ConsoleExperimentalApi
    fun startAsDaemon(instance: MiraiConsoleImplementationTerminal = MiraiConsoleImplementationTerminal()) {
        instance.start()
        startupConsoleThread()
    }
}

@OptIn(ConsoleExperimentalApi::class)
internal object ConsoleDataHolder : AutoSavePluginDataHolder,
    CoroutineScope by MiraiConsole.childScope("ConsoleDataHolder") {
    @ConsoleExperimentalApi
    override val autoSaveIntervalMillis: LongRange = 60_000L..10.times(60_000)

    @ConsoleExperimentalApi
    override val dataHolderName: String
        get() = "Terminal"
}

private val shutdownSignals = arrayOf(
    "INT", "TERM", "QUIT"
)

internal val signalHandler: (String) -> Unit = initSignalHandler()

@OptIn(ConsoleExperimentalApi::class)
private fun initSignalHandler(): (String) -> Unit {
    val shutdownMonitorLock = AtomicBoolean(false)
    val lastSignalTimestamp = AtomicLong(0)
    return handler@{ signalName ->
        if (signalName == "WINCH") {
            // Windows CMD.exe resized
            return@handler
        }
        runCatching {
            MiraiConsole.mainLogger
        }.onFailure { // mirai-console not yet initialized
            System.err.println("[TERMINAL] [WARNING] Received signal $signalName")
            System.err.println("[TERMINAL] [WARNING] This signal will be processed later because mirai-console not yet initialized.")

            // Try later
            if (signalName in shutdownSignals) {
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    delay(500L)
                    signalHandler(signalName)
                }
            }
            return@handler
        }
        // JLine may process other signals
        MiraiConsole.mainLogger.verbose { "Received signal $signalName" }
        if (signalName !in shutdownSignals) return@handler

        MiraiConsole.mainLogger.debug { "Handled  signal $signalName" }
        run multiSignalHandler@{
            val crtTime = System.currentTimeMillis()
            val last = lastSignalTimestamp.getAndSet(crtTime)
            if (crtTime - last < 1000L) {
                MiraiConsole.mainLogger.debug { "Multi    signal $signalName" }
                MiraiConsole.mainLogger.info { "Process will be killed after 0.5s" }

                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    delay(500L)
                    exitProcessAndForceHalt(-5)
                }
            }
        }
        MiraiConsole.shutdown()

        // Shutdown by signal requires process be killed
        if (shutdownMonitorLock.compareAndSet(false, true)) {
            val pool = Executors.newFixedThreadPool(4, object : ThreadFactory {
                private val counter = AtomicInteger()
                override fun newThread(r: Runnable): Thread {
                    return Thread(r, "Mirai Console Signal-Shutdown Daemon #" + counter.getAndIncrement()).also {
                        it.isDaemon = true
                    }
                }
            })
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(pool.asCoroutineDispatcher()) {
                MiraiConsole.job.join()

                delay(15000)
                // Force kill process if plugins started non-daemon threads
                exitProcessAndForceHalt(-5)
            }
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch(pool.asCoroutineDispatcher()) {
                delay(1000L * 60) // timed out
                // Force kill process if plugins started non-daemon threads
                exitProcessAndForceHalt(-5)
            }
        }
    }
}

internal fun registerSignalHandler() {
    fun reg(name: String) {
        Signals.register(name) { signalHandler(name) }
    }
    shutdownSignals.forEach { reg(it) }
}

internal fun exitProcessAndForceHalt(code: Int): Nothing {
    MiraiConsole.mainLogger.debug { "[exitProcessAndForceHalt] called with code $code" }

    val exitFuncName = arrayOf("exit", "halt")
    val shutdownClasses = arrayOf("java.lang.System", "java.lang.Runtime", "java.lang.Shutdown")
    val isShutdowning = Thread.getAllStackTraces().asSequence().flatMap {
        it.value.asSequence()
    }.any { stackTrace ->
        stackTrace.className in shutdownClasses && stackTrace.methodName in exitFuncName
    }
    MiraiConsole.mainLogger.debug { "[exitProcessAndForceHalt] isShutdowning = $isShutdowning" }

    val task = Runnable {
        Thread.sleep(15000L)
        runCatching { net.mamoe.mirai.console.internal.shutdown.ShutdownDaemon.dumpCrashReport(true) }
        val fc = when (code) {
            0 -> 5784171
            else -> code
        }

        MiraiConsole.mainLogger.debug { "[exitProcessAndForceHalt] timed out, force halt with code $fc" }
        Runtime.getRuntime().halt(fc)
    }
    if (isShutdowning) {
        task.run()
        error("Runtime.halt returned normally, while it was supposed to halt JVM.")
    } else {
        Thread(task, "Mirai Console Force Halt Daemon").start()
        exitProcess(code)
    }
}


internal object ConsoleCommandSenderImplTerminal : MiraiConsoleImplementation.ConsoleCommandSenderImpl {
    override suspend fun sendMessage(message: String) {
        kotlin.runCatching {
            printToScreen(message + ANSI_RESET)
        }.onFailure { exception ->
            // If failed. It means JLine Terminal not working...
            PrintStream(FileOutputStream(FileDescriptor.err)).use {
                it.println("Exception while ConsoleCommandSenderImplTerminal.sendMessage")
                exception.printStackTrace(it)
            }
        }
    }

    override suspend fun sendMessage(message: Message) {
        return sendMessage(message.toString())
    }
}