/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "unused")
@file:OptIn(ConsoleInternalApi::class)

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.AnsiMessageBuilder
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.utils.childScopeContext
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.verbose
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
    @ConsoleInternalApi
    public val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JvmPluginLoader].
     *
     * @return 不可变 [List] ([java.util.Collections.unmodifiableList])
     */
    public val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>>

    /**
     * 此 Console 后端构建时间
     */
    public val buildDate: Instant

    /**
     * 此 Console 后端版本号
     */
    public val version: SemVersion


    @ConsoleExperimentalApi
    public val pluginCenter: PluginCenter

    /**
     * 创建一个 logger
     */
    @ConsoleExperimentalApi
    public fun createLogger(identity: String?): MiraiLogger

    /**
     * 是否支持使用 Ansi 输出彩色信息
     *
     * 注: 不是每个前端都可能提供 `org.fusesource.jansi:jansi` 库支持,
     * 请不要直接使用 `org.fusesource.jansi:jansi`
     *
     * @see [AnsiMessageBuilder]
     */
    @ConsoleExperimentalApi
    public val isAnsiSupported: Boolean

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
        @ConsoleExperimentalApi("This is a low-level API and might be removed in the future.")
        public fun addBot(id: Long, password: String, configuration: BotConfiguration.() -> Unit = {}): Bot =
            addBotImpl(id, password, configuration)

        /**
         * 添加一个 [Bot] 实例到全局 Bot 列表, 但不登录.
         *
         * 调用 [Bot.login] 可登录.
         *
         * @see Bot.instances 获取现有 [Bot] 实例列表
         * @see BotConfigurationAlterer ExtensionPoint
         */
        @ConsoleExperimentalApi("This is a low-level API and might be removed in the future.")
        public fun addBot(id: Long, password: ByteArray, configuration: BotConfiguration.() -> Unit = {}): Bot =
            addBotImpl(id, password, configuration)

        @Suppress("UNREACHABLE_CODE")
        private fun addBotImpl(id: Long, password: Any, configuration: BotConfiguration.() -> Unit = {}): Bot {
            var config = BotConfiguration().apply {

                workingDir = MiraiConsole.rootDir
                    .resolve("bots")
                    .resolve(id.toString())
                    .also { it.mkdirs() }

                mainLogger.verbose { "Bot $id working in $workingDir" }

                val deviceInRoot = MiraiConsole.rootDir.resolve("device.json")
                val deviceInWorkingDir = workingDir.resolve("device.json")

                val deviceInfoInWorkingDir = workingDir.resolve("deviceInfo.json")
                if (!deviceInWorkingDir.exists()) {
                    when {
                        deviceInfoInWorkingDir.exists() -> {
                            // rename bots/id/deviceInfo.json to bots/id/device.json
                            mainLogger.verbose { "Renaming $deviceInfoInWorkingDir to $deviceInWorkingDir" }
                            deviceInfoInWorkingDir.renameTo(deviceInWorkingDir)
                        }
                        deviceInRoot.exists() -> {
                            // copy root/device.json to bots/id/device.json
                            mainLogger.verbose { "Coping $deviceInRoot to $deviceInWorkingDir" }
                            deviceInRoot.copyTo(deviceInWorkingDir)
                        }
                    }
                }

                fileBasedDeviceInfo("device.json")

                redirectNetworkLogToDirectory()
                this.botLoggerSupplier = {
                    MiraiLogger.Factory.create(Bot::class, "Bot.${it.id}")
                }
                parentCoroutineContext = MiraiConsole.childScopeContext("Bot $id")
                autoReconnectOnForceOffline()

                this.loginSolver = MiraiConsoleImplementationBridge.createLoginSolver(id, this)
                configuration()
            }

            config = GlobalComponentStorage.run {
                BotConfigurationAlterer.foldExtensions(config) { acc, extension ->
                    extension.alterConfiguration(id, acc)

                }
            }

            return when (password) {
                is ByteArray -> BotFactory.newBot(id, password, config)
                is String -> BotFactory.newBot(id, password, config)
                else -> throw IllegalArgumentException("Bad password type: `${password.javaClass.name}`. Require ByteArray or String")
            }
        }

        @ConsoleExperimentalApi("This is a low-level API and might be removed in the future.")
        public val isActive: Boolean
            get() = job.isActive
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

