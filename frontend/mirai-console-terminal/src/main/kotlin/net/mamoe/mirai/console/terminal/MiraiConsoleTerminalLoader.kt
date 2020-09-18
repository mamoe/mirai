/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
)
@file:OptIn(ConsoleInternalApi::class, ConsoleTerminalExperimentalApi::class)

package net.mamoe.mirai.console.terminal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.terminal.noconsole.SystemOutputPrintStream
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.minutesToMillis
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import kotlin.system.exitProcess

/**
 * mirai-console-terminal CLI 入口点
 */
object MiraiConsoleTerminalLoader {
    @JvmStatic
    fun main(args: Array<String>) {
        parse(args, exitProcess = true)
        startAsDaemon()
        try {
            runBlocking {
                MiraiConsole.job.join()
            }
        } catch (e: CancellationException) {
            // ignored
        }
    }

    @ConsoleTerminalExperimentalApi
    fun printHelpMessage() {
        val help = listOf(
            "" to "Mirai-Console[Terminal FrontEnd] v" + kotlin.runCatching {
                net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
            }.getOrElse { "<unknown>" },
            "" to "",
            "--help" to "显示此帮助",
            "" to "",
            "--no-console" to "使用无终端操作环境",
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
        overrideSTD()
        startupConsoleThread()
    }
}

internal object ConsoleDataHolder : AutoSavePluginDataHolder,
    CoroutineScope by MiraiConsole.childScope("ConsoleDataHolder") {
    @ConsoleExperimentalApi
    override val autoSaveIntervalMillis: LongRange = 1.minutesToMillis..10.minutesToMillis

    @ConsoleExperimentalApi
    override val dataHolderName: String
        get() = "Terminal"
}

internal fun overrideSTD() {
    System.setOut(
        PrintStream(
            BufferedOutputStream(
                logger = DefaultLogger("stdout").run { ({ line: String? -> info(line) }) }
            )
        )
    )
    System.setErr(
        PrintStream(
            BufferedOutputStream(
                logger = DefaultLogger("stderr").run { ({ line: String? -> warning(line) }) }
            )
        )
    )
}


internal object ConsoleCommandSenderImplTerminal : MiraiConsoleImplementation.ConsoleCommandSenderImpl {
    override suspend fun sendMessage(message: String) {
        kotlin.runCatching {
            lineReader.printAbove(message)
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