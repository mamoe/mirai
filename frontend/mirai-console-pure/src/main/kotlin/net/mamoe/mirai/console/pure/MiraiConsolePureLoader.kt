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
)
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console.pure

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.minutesToMillis
import java.io.PrintStream

/**
 * mirai-console-pure CLI 入口点
 */
object MiraiConsolePureLoader {
    @JvmStatic
    fun main(args: Array<String>) {
        startAsDaemon()
        try {
            runBlocking {
                MiraiConsole.job.join()
            }
        } catch (e: CancellationException) {
            // ignored
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    @ConsoleExperimentalAPI
    fun startAsDaemon(instance: MiraiConsoleImplementationPure = MiraiConsoleImplementationPure()) {
        instance.start()
        overrideSTD()
        startupConsoleThread()
    }
}

internal object ConsoleDataHolder : AutoSavePluginDataHolder,
    CoroutineScope by MiraiConsole.childScope("ConsoleDataHolder") {
    @ConsoleExperimentalAPI
    override val autoSaveIntervalMillis: LongRange = 1.minutesToMillis..10.minutesToMillis

    @ConsoleExperimentalAPI
    override val dataHolderName: String
        get() = "Pure"
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


internal object ConsoleCommandSenderImplPure : MiraiConsoleImplementation.ConsoleCommandSenderImpl {
    override suspend fun sendMessage(message: String) {
        kotlin.runCatching {
            lineReader.printAbove(message)
        }.onFailure {
            consoleLogger.error(it)
        }
    }

    override suspend fun sendMessage(message: Message) {
        return sendMessage(message.contentToString())
    }
}