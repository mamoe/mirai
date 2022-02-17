/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "unused")
@file:OptIn(ConsoleInternalApi::class)

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.him188.kotlin.dynamic.delegation.dynamicDelegation
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.AnsiMessageBuilder
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.*
import java.io.File
import java.nio.file.Path
import java.time.Instant

/**
 * Mirai Console 后端功能入口.
 *
 * # 使用 Mirai Console
 *
 * ## 获取 Mirai Console 后端实例
 *
 * 一般插件开发者只能通过 [MiraiConsole.INSTANCE] 获得 [MiraiConsole] 实例.
 *
 * ## Mirai Console 生命周期
 *
 * [MiraiConsole] 实现[协程作用域][CoroutineScope]. [MiraiConsole] 生命周期与该[协程作用域][CoroutineScope]的相同.
 *
 * 在 [MiraiConsole] 实例构造后就视为*已开始[生存][Job.isActive]*. 随后才会[正式启动][MiraiConsoleImplementation.start] (初始化和加载插件等).
 *
 * [取消 Job][Job.cancel] 时会同时停止 [MiraiConsole], 并进行清理工作 (例如调用 [JvmPlugin.onDisable].
 *
 * ## 获取插件管理器等功能实例
 *
 * [MiraiConsole] 是后端功能入口, 可调用其 [MiraiConsole.pluginManager] 获取到 [PluginManager] 等实例.
 *
 * # 实现 Mirai Console
 *
 * ## 实现 Mirai Console 后端
 *
 * [MiraiConsole] 不可直接实现.
 *
 * 要实现 Mirai Console 后端, 需实现接口 [MiraiConsoleImplementation] 为一个 `class` 切勿实现为 `object`(单例或静态).
 *
 * ## 启动 Mirai Console 后端
 *
 * Mirai Console 后端 (即本 [MiraiConsole] 类实例) 不可单独 (直接) 启动, 需要配合一个任意的前端实现.
 *
 * Mirai Console 的启动时机由前端决定. 前端可在恰当的时机调用 [MiraiConsoleImplementation.start] 来启动一个 [MiraiConsoleImplementation].
 *
 * [MiraiConsoleImplementation] 将会由 [bridge][MiraiConsoleImplementationBridge] 转接为 [MiraiConsole] 实现. 对 [MiraiConsole] 的调用都会被转发到前端实现的 [MiraiConsoleImplementation].
 *
 * @see INSTANCE
 * @see MiraiConsoleImplementation
 */
@NotStableForInheritance
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

    /**
     * [PluginManager] 实例. 在 [MiraiConsole] 生命周期内应保持不变.
     *
     * @since 2.10
     */
    public val pluginManager: PluginManager

    @ConsoleExperimentalApi
    public val pluginCenter: PluginCenter
        get() = throw UnsupportedOperationException("PluginCenter is not supported yet")

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

    /**
     * [MiraiConsole] 唯一实例. 一般插件开发者只能通过 [MiraiConsole.INSTANCE] 获得 [MiraiConsole] 实例.
     *
     * 对象以 [bridge][MiraiConsoleImplementationBridge] 实现, 将会桥接特定前端实现的 [MiraiConsoleImplementation] 到 [MiraiConsole].
     */
    public companion object INSTANCE : MiraiConsole by dynamicDelegation({ MiraiConsoleImplementation.getBridge() }) {
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
         * @see Bot.instances 获取现有 [Bot] 实例列表
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

                this.loginSolver = MiraiConsoleImplementation.getInstance().createLoginSolver(id, this)
                configuration()
            }

            config = GlobalComponentStorage.foldExtensions(BotConfigurationAlterer, config) { acc, extension ->
                extension.alterConfiguration(id, acc)
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

