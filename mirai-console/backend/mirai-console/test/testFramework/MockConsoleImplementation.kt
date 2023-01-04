/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.testFramework

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.data.MultiFilePluginDataStorageImpl
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import java.nio.file.Path
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.createTempDirectory

open class MockConsoleImplementation : MiraiConsoleImplementation {
    final override val rootPath: Path = createTempDirectory()

    override val frontEndDescription: MiraiConsoleFrontEndDescription
        get() = object : MiraiConsoleFrontEndDescription {
            override val name: String get() = "Test"
            override val vendor: String get() = "Test"
            override val version: SemVersion get() = SemVersion("1.0.0")
        }
    override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader })
    override val jvmPluginLoader: JvmPluginLoader by lazy {
        backendAccess.createDefaultJvmPluginLoader(coroutineContext)
    }
    override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl =
        object : MiraiConsoleImplementation.ConsoleCommandSenderImpl {
            override suspend fun sendMessage(message: Message) {
                println(message)
            }

            override suspend fun sendMessage(message: String) {
                println(message)
            }
        }
    override val commandManager: CommandManager by lazy { backendAccess.createDefaultCommandManager(coroutineContext) }
    override val dataStorageForJvmPluginLoader: PluginDataStorage =
        MultiFilePluginDataStorageImpl(rootPath.resolve("data"))
    override val configStorageForJvmPluginLoader: PluginDataStorage =
        MultiFilePluginDataStorageImpl(rootPath.resolve("config"))
    override val dataStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorageImpl(rootPath.resolve("data"))
    override val configStorageForBuiltIns: PluginDataStorage =
        MultiFilePluginDataStorageImpl(rootPath.resolve("config"))

    override val consoleInput: ConsoleInput = object : ConsoleInput {
        override suspend fun requestInput(hint: String): String {
            println(hint)
            return readLine() ?: error("No stdin")
        }
    }

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver =
        LoginSolver.Default!!

    override fun createLoggerFactory(context: MiraiConsoleImplementation.FrontendLoggingInitContext): MiraiLogger.Factory {
        return object : MiraiLogger.Factory {
            override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                return PlatformLogger(identity)
            }
        }
    }

    override val consoleDataScope: MiraiConsoleImplementation.ConsoleDataScope by lazy {
        MiraiConsoleImplementation.ConsoleDataScope.createDefault(
            coroutineContext,
            dataStorageForBuiltIns,
            configStorageForBuiltIns
        )
    }
    override val coroutineContext: CoroutineContext =
        CoroutineName("Console Main") + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }

}

inline fun <R> withConsoleImplementation(crossinline block: MockConsoleImplementation.() -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    MockConsoleImplementation().run {
        start()
        try {
            return block()
        } finally {
            cancel()
        }
    }
}