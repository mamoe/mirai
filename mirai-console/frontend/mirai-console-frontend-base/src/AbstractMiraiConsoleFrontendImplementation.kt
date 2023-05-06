/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.frontendbase

import kotlinx.coroutines.*
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import kotlin.coroutines.CoroutineContext

/**
 * [MiraiConsoleImplementation] 的基本抽象实现
 *
 * @param frontendCoroutineName 该前端的名字, 如 `"MiraiConsoleImplementationTerminal"`
 * @see FrontendBase
 */
@OptIn(ConsoleFrontEndImplementation::class)
public abstract class AbstractMiraiConsoleFrontendImplementation(
    frontendCoroutineName: String,
) : MiraiConsoleImplementation, CoroutineScope {

    // region 此 region 的 字段 / 方法 为 console 默认/内部 实现, 如无必要不建议修改
    @OptIn(ConsoleInternalApi::class)
    private val delegateCoroutineScope by lazy {
        CoroutineScope(
            SupervisorJob() +
                    CoroutineName(frontendCoroutineName) +
                    CoroutineExceptionHandler { coroutineContext, throwable ->
                        if (throwable is CancellationException) {
                            return@CoroutineExceptionHandler
                        }
                        val coroutineName = coroutineContext[CoroutineName]?.name ?: "<unnamed>"
                        MiraiConsole.mainLogger.error("Exception in coroutine $coroutineName", throwable)
                    }
        )
    }
    override val coroutineContext: CoroutineContext get() = delegateCoroutineScope.coroutineContext

    override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader })
    override val jvmPluginLoader: JvmPluginLoader by lazy { backendAccess.createDefaultJvmPluginLoader(coroutineContext) }
    override val commandManager: CommandManager by lazy { backendAccess.createDefaultCommandManager(coroutineContext) }
    override val consoleDataScope: MiraiConsoleImplementation.ConsoleDataScope by lazy {
        @OptIn(ConsoleExperimentalApi::class)
        MiraiConsoleImplementation.ConsoleDataScope.createDefault(
            coroutineContext, dataStorageForBuiltIns, configStorageForBuiltIns
        )
    }

    @ConsoleExperimentalApi
    override val dataStorageForJvmPluginLoader: PluginDataStorage by lazy {
        MultiFilePluginDataStorage(rootPath.resolve("data"))
    }

    @ConsoleExperimentalApi
    override val dataStorageForBuiltIns: PluginDataStorage by lazy {
        MultiFilePluginDataStorage(rootPath.resolve("data"))
    }

    @ConsoleExperimentalApi
    override val configStorageForJvmPluginLoader: PluginDataStorage by lazy {
        MultiFilePluginDataStorage(rootPath.resolve("config"))
    }

    @ConsoleExperimentalApi
    override val configStorageForBuiltIns: PluginDataStorage by lazy {
        MultiFilePluginDataStorage(rootPath.resolve("config"))
    }
    // endregion

    // region
    protected abstract val frontendBase: FrontendBase
    // endregion


    // region Logging
    @OptIn(MiraiInternalApi::class)
    override fun createLoggerFactory(context: MiraiConsoleImplementation.FrontendLoggingInitContext): MiraiLogger.Factory {
        @Suppress("INVISIBLE_MEMBER")
        frontendBase.initScreen_forwardStdToScreen()

        // region Default Fallback Implementation

        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge.defaultLoggerFactory = {

            class DefaultMiraiConsoleFactory : MiraiLogger.Factory {
                // Don't directly use ::println
                // ::println will query System.out every time.
                private val stdout: ((String) -> Unit) = System.out::println

                override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                    return PlatformLogger(identity ?: requester.kotlin.simpleName ?: requester.simpleName, stdout)
                }
            }

            DefaultMiraiConsoleFactory()
        }
        // endregion

        val factoryImpl = context.acquirePlatformImplementation()
        context.invokeAfterInitialization {
            @Suppress("INVISIBLE_MEMBER")
            frontendBase.initScreen_forwardStdToMiraiLogger()
        }
        return factoryImpl
    }
    // endregion
}