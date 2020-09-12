/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.data.MemoryPluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import java.nio.file.Path
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.test.assertNotNull

@OptIn(ConsoleInternalApi::class)
fun initTestEnvironment() {
    object : MiraiConsoleImplementation {
        override val rootPath: Path = createTempDir().toPath()

        @ConsoleExperimentalApi
        override val frontEndDescription: MiraiConsoleFrontEndDescription
            get() = object : MiraiConsoleFrontEndDescription {
                override val name: String
                    get() = "Test"
                override val vendor: String
                    get() = "Test"
                override val version: Semver
                    get() = Semver("1.0.0")

            }
        override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader })
        override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl =
            object : MiraiConsoleImplementation.ConsoleCommandSenderImpl {
                override suspend fun sendMessage(message: Message) {
                    println(message)
                }

                override suspend fun sendMessage(message: String) {
                    println(message)
                }
            }
        override val dataStorageForJvmPluginLoader: PluginDataStorage = MemoryPluginDataStorage()
        override val configStorageForJvmPluginLoader: PluginDataStorage = MemoryPluginDataStorage()
        override val dataStorageForBuiltIns: PluginDataStorage = MemoryPluginDataStorage()
        override val configStorageForBuiltIns: PluginDataStorage = MemoryPluginDataStorage()

        override val consoleInput: ConsoleInput = object : ConsoleInput {
            override suspend fun requestInput(hint: String): String {
                println(hint)
                return readLine() ?: error("No stdin")
            }
        }

        override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver =
            LoginSolver.Default

        override fun createLogger(identity: String?): MiraiLogger {
            return PlatformLogger(identity)
        }

        override val coroutineContext: CoroutineContext = SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
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
