/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "unused")
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console

import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.extension.foldExtensions
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScopeContext
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.nio.file.Path
import java.time.Instant


/**
 * mirai-console 实例
 *
 * @see INSTANCE
 * @see MiraiConsoleImplementation
 */
public interface MiraiConsole : CoroutineScope {
    /**
     * Console 运行根目录, 由前端决定确切路径.
     *
     * 所有子模块都会在这个目录之下创建子目录.
     *
     * @see PluginManager.pluginsPath
     * @see PluginManager.pluginsDataPath
     * @see PluginManager.pluginsConfigPath
     */
    public val rootPath: Path

    /**
     * Console 主日志.
     *
     * **实现细节**: 这个 [MiraiLogger] 的 [MiraiLogger.identity] 通常为 `main`
     *
     * **注意**: 插件不应该在任何时刻使用它.
     */
    @ConsoleInternalAPI
    public val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JarPluginLoader].
     *
     * @return 不可变 [List] ([java.util.Collections.unmodifiableList])
     */
    public val builtInPluginLoaders: List<PluginLoader<*, *>>

    /**
     * 此 Console 后端构建时间
     */
    public val buildDate: Instant

    /**
     * 此 Console 后端版本号
     */
    public val version: Semver

    @ConsoleExperimentalAPI
    public val pluginCenter: PluginCenter

    /**
     * 创建一个 logger
     */
    @ConsoleExperimentalAPI
    public fun createLogger(identity: String?): MiraiLogger

    public companion object INSTANCE : MiraiConsole by MiraiConsoleImplementationBridge {
        /**
         * 获取 [MiraiConsole] 的 [Job]
         */ // MiraiConsole.INSTANCE.getJob()
        public val job: Job
            get() = MiraiConsole.coroutineContext[Job]
                ?: throw MalformedMiraiConsoleImplementationError("Internal error: Job not found in MiraiConsole.coroutineContext")

        /**
         * 添加一个 [Bot] 实例到全局 Bot 列表, 但不登录.
         *
         * 调用 [Bot.login] 可登录.
         *
         * @see Bot.botInstances 获取现有 [Bot] 实例列表
         * @see BotConfigurationAlterer ExtensionPoint
         */
        // don't static
        @ConsoleExperimentalAPI("This is a low-level API and might be removed in the future.")
        public fun addBot(id: Long, password: String, configuration: BotConfiguration.() -> Unit = {}): Bot =
            addBotImpl(id, password, configuration)

        /**
         * 添加一个 [Bot] 实例到全局 Bot 列表, 但不登录.
         *
         * 调用 [Bot.login] 可登录.
         *
         * @see Bot.botInstances 获取现有 [Bot] 实例列表
         * @see BotConfigurationAlterer ExtensionPoint
         */
        @ConsoleExperimentalAPI("This is a low-level API and might be removed in the future.")
        public fun addBot(id: Long, password: ByteArray, configuration: BotConfiguration.() -> Unit = {}): Bot =
            addBotImpl(id, password, configuration)

        @Suppress("UNREACHABLE_CODE")
        private fun addBotImpl(id: Long, password: Any, configuration: BotConfiguration.() -> Unit = {}): Bot {
            var config = BotConfiguration().apply {
                fileBasedDeviceInfo()
                redirectNetworkLogToDirectory()
                parentCoroutineContext = MiraiConsole.childScopeContext("Bot $id")

                this.loginSolver = MiraiConsoleImplementationBridge.createLoginSolver(id, this)
                configuration()
            }

            config = BotConfigurationAlterer.foldExtensions(config) { acc, extension ->
                extension.alterConfiguration(id, acc)
            }

            return when (password) {
                is ByteArray -> Bot(id, password, config)
                is String -> Bot(id, password, config)
                else -> null!!
            }
        }
    }
}

/**
 * @see MiraiConsole.rootPath
 */
public val MiraiConsole.rootDir: File get() = rootPath.toFile()

/**
 * [MiraiConsoleImplementation] 实现有误时抛出.
 *
 * @see MiraiConsoleImplementation.start
 */
public class MalformedMiraiConsoleImplementationError : Error {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
}

