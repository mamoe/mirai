/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
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
@file:OptIn(ConsoleInternalAPI::class, ConsoleFrontEndImplementation::class)

package net.mamoe.mirai.console.pure


import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.DeferredPluginLoader
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.pure.ConsoleInputImpl.requestInput
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DefaultLoginSolver
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * mirai-console-pure 后端实现
 *
 * @see MiraiConsoleFrontEndPure 前端实现
 * @see MiraiConsolePureLoader CLI 入口点
 */
internal class MiraiConsoleImplementationPure
@JvmOverloads constructor(
    override val rootPath: Path = Paths.get("."),
    override val builtInPluginLoaders: List<PluginLoader<*, *>> = Collections.unmodifiableList(
        listOf(DeferredPluginLoader { JarPluginLoader })
    ),
    override val frontEndDescription: MiraiConsoleFrontEndDescription = ConsoleFrontEndDescImpl,
    override val consoleCommandSender: ConsoleCommandSender = ConsoleCommandSenderImpl,
    override val dataStorageForJarPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val dataStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
    override val configStorageForJarPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config"))
) : MiraiConsoleImplementation, CoroutineScope by CoroutineScope(SupervisorJob()) {
    override val mainLogger: MiraiLogger by lazy {
        MiraiConsole.newLogger("main")
    }
    override val consoleInput: ConsoleInput get() = ConsoleInputImpl

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver {
        return DefaultLoginSolver(input = { requestInput("LOGIN> ") })
    }

    override fun newLogger(identity: String?): MiraiLogger = LoggerCreator(identity)

    init {
        with(rootPath.toFile()) {
            mkdir()
            require(isDirectory) { "rootDir $absolutePath is not a directory" }
        }
    }
}

private object ConsoleInputImpl : ConsoleInput {
    override suspend fun requestInput(hint: String): String = ConsoleUtils.miraiLineReader(hint)
}

private object ConsoleFrontEndDescImpl : MiraiConsoleFrontEndDescription {
    override val name: String get() = "Pure"
    override val vendor: String get() = "Mamoe Technologies"
    override val version: Semver = net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
}