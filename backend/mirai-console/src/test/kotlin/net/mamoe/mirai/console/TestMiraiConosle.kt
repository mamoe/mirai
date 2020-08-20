/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.DeferredPluginLoader
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.MemorySettingStorage
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import java.io.File
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.test.assertNotNull

@OptIn(ConsoleInternalAPI::class)
fun initTestEnvironment() {
    object : MiraiConsoleImplementation {
        override val rootDir: File = createTempDir()
        override val frontEnd: MiraiConsoleFrontEnd = object : MiraiConsoleFrontEnd {
            override val name: String get() = "Test"
            override val version: String get() = "1.0.0"
            override fun loggerFor(identity: String?): MiraiLogger = PlatformLogger(identity)
            override fun pushBot(bot: Bot) = println("pushBot: $bot")
            override suspend fun requestInput(hint: String): String = readLine()!!
            override fun createLoginSolver(): LoginSolver = LoginSolver.Default
        }
        override val mainLogger: MiraiLogger = DefaultLogger("main")
        override val builtInPluginLoaders: List<PluginLoader<*, *>> = listOf(DeferredPluginLoader { JarPluginLoader })
        override val consoleCommandSender: ConsoleCommandSender = object : ConsoleCommandSender() {
            override suspend fun sendMessage(message: Message) = println(message)
        }
        override val settingStorageForJarPluginLoader: SettingStorage get() = MemorySettingStorage()
        override val settingStorageForBuiltIns: SettingStorage get() = MemorySettingStorage()
        override val coroutineContext: CoroutineContext = SupervisorJob()
    }.start()
    CommandManager
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
