/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.ConsoleCommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.test.assertNotNull

fun initTestEnvironment() {
    MiraiConsoleInitializer.init(object : IMiraiConsole {
        override val rootDir: File = createTempDir()
        override val frontEnd: MiraiConsoleFrontEnd = object : MiraiConsoleFrontEnd {
            override fun loggerFor(identity: String?): MiraiLogger = DefaultLogger(identity)
            override fun pushBot(bot: Bot) = println("pushBot: $bot")
            override suspend fun requestInput(hint: String): String = readLine()!!
            override fun createLoginSolver(): LoginSolver = LoginSolver.Default
        }
        override val mainLogger: MiraiLogger = DefaultLogger("main")
        override val builtInPluginLoaders: List<PluginLoader<*, *>> = listOf(JarPluginLoader)
        override val consoleCommandOwner: ConsoleCommandOwner = object : ConsoleCommandOwner() {}
        override val consoleCommandSender: ConsoleCommandSender = object : ConsoleCommandSender() {
            override suspend fun sendMessage(message: Message) = println(message)
        }
        override val coroutineContext: CoroutineContext = SupervisorJob()
    })
}

internal object Testing {
    @Volatile
    internal var cont: Continuation<Any?>? = null

    @Suppress("UNCHECKED_CAST")
    suspend fun <R> withTesting(timeout: Long = 5000L, block: suspend () -> Unit): R {
        @Suppress("RemoveExplicitTypeArguments") // bug
        return if (timeout != -1L) {
            withTimeout<R>(timeout) {
                suspendCancellableCoroutine<R> { ct ->
                    this@Testing.cont = ct as Continuation<Any?>
                    runBlocking { block() }
                }
            }
        } else {
            suspendCancellableCoroutine<R> { ct ->
                this.cont = ct as Continuation<Any?>
                runBlocking { block() }
            }
        }
    }

    fun ok(result: Any? = Unit) {
        val cont = cont
        assertNotNull(cont)
        cont.resume(result)
    }
}
