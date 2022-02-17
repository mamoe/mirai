/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.data.AutoSavePluginDataHolder
import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginData
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.extension.ComponentStorage
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.command.CommandManagerImpl
import net.mamoe.mirai.console.internal.data.builtins.ConsoleDataScopeImpl
import net.mamoe.mirai.console.internal.logging.LoggerControllerImpl
import net.mamoe.mirai.console.internal.plugin.BuiltInJvmPluginLoaderImpl
import net.mamoe.mirai.console.internal.pluginManagerImpl
import net.mamoe.mirai.console.logging.LoggerController
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.NotStableForInheritance
import java.nio.file.Path
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.annotation.AnnotationTarget.*
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass


/**
 * 标记一个仅用于 [MiraiConsole] 前端实现的 API.
 *
 * 这些 API 只应由前端实现者使用, 而不应该被插件或其他调用者使用.
 */
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
@ConsoleFrontEndImplementation
public annotation class ConsoleFrontEndImplementation

/**
 * 实现 [MiraiConsole] 的接口
 *
 * **注意**: 随着 Console 的更新, 在版本号 `x.y.z` 的 `y` 修改时此接口可能就会发生 ABI 变动. 意味着前端实现着需要跟随 Console 更新.
 *
 * @see MiraiConsoleImplementation.start 启动
 */
@ConsoleFrontEndImplementation
public interface MiraiConsoleImplementation : CoroutineScope {
    /**
     * 获取原始 [MiraiConsoleImplementation] 实例.
     *
     * [MiraiConsoleImplementation.start] 实际上会创建 [MiraiConsoleImplementationBridge] 并启动该 bridge, 不会直接使用提供的 [MiraiConsoleImplementation] 实例.
     * [MiraiConsoleImplementation.getInstance] 获取到的将会是 bridge. 可通过 `bridge.origin` 获取原始在 [start] 传递的实例.
     *
     * @since 2.11.0-RC
     */
    public val origin: MiraiConsoleImplementation get() = this

    /**
     * [MiraiConsole] 的 [CoroutineScope.coroutineContext], 必须拥有如下元素
     *
     * - [Job]: 用于管理整个 [MiraiConsole] 的生命周期. 当此 [Job] 被 [Job.cancel] 后, [MiraiConsole] 就会结束.
     * - [CoroutineExceptionHandler]: 用于处理 [MiraiConsole] 所有协程抛出的 **未被捕捉** 的异常. 不是所有异常都会被传递到这里.
     */
    public override val coroutineContext: CoroutineContext

    /**
     * Console 运行根目录绝对路径 (否则可能会被一些 native 插件覆盖相对路径)
     * @see MiraiConsole.rootPath 获取更多信息
     */
    public val rootPath: Path

    /**
     * 本前端实现的描述信息
     */
    public val frontEndDescription: MiraiConsoleFrontEndDescription

    /**
     * 内建加载器列表, 一般需要包含 [JvmPluginLoader].
     *
     * @return 不可变的 [List], [Collections.unmodifiableList]
     */
    public val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>>

    /**
     * [JvmPluginLoader] 实例. 建议实现为 lazy:
     *
     * ```
     * override val jvmPluginLoader: JvmPluginLoader by lazy { backendAccess.createDefaultJvmPluginLoader(coroutineContext) }
     * ```
     *
     * @see BackendAccess.createDefaultJvmPluginLoader
     * @since 2.10.0-RC
     */
    public val jvmPluginLoader: JvmPluginLoader

    /**
     * 由 Kotlin 用户实现
     *
     * @see [ConsoleCommandSender]
     */
    @ConsoleFrontEndImplementation
    public interface ConsoleCommandSenderImpl {
        @JvmSynthetic
        public suspend fun sendMessage(message: Message)

        @JvmSynthetic
        public suspend fun sendMessage(message: String)
    }

    /**
     * 由 Java 用户实现
     *
     * @see [ConsoleCommandSender]
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @ConsoleFrontEndImplementation
    public interface JConsoleCommandSenderImpl : ConsoleCommandSenderImpl {
        @JvmName("sendMessage")
        public fun sendMessageJ(message: Message)

        @JvmName("sendMessage")
        public fun sendMessageJ(message: String)


        @JvmSynthetic
        public override suspend fun sendMessage(message: Message): Unit =
            withContext(Dispatchers.IO) { sendMessageJ(message) }

        @JvmSynthetic
        public override suspend fun sendMessage(message: String): Unit =
            withContext(Dispatchers.IO) { sendMessageJ(message) }
    }

    /**
     * [ConsoleCommandSender]
     */
    public val consoleCommandSender: ConsoleCommandSenderImpl

    /**
     * [CommandManager] 实现, 建议实现为 lazy:
     * ```
     * override val commandManager: CommandManager by lazy { backendAccess.createDefaultCommandManager(coroutineContext) }
     * ```
     *
     * @since 2.10.0-RC
     * @see BackendAccess.createDefaultCommandManager
     */
    public val commandManager: CommandManager

    public val dataStorageForJvmPluginLoader: PluginDataStorage
    public val configStorageForJvmPluginLoader: PluginDataStorage
    public val dataStorageForBuiltIns: PluginDataStorage
    public val configStorageForBuiltIns: PluginDataStorage

    /**
     * @see ConsoleInput 的实现
     * @see JConsoleInput
     */
    public val consoleInput: ConsoleInput

    /**
     * 供 Java 用户实现 [ConsoleInput]
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @ConsoleFrontEndImplementation
    public interface JConsoleInput : ConsoleInput {
        /**
         * @see ConsoleInput.requestInput
         */
        @JvmName("requestInput")
        public fun requestInputJ(hint: String): String

        override suspend fun requestInput(hint: String): String {
            return runInterruptible(Dispatchers.IO) { requestInputJ(hint) }
        }
    }

    /**
     * 创建一个 [LoginSolver]
     *
     * **备注**: 此函数通常在构造 [Bot] 实例, 即 [MiraiConsole.addBot] 时调用.
     *
     * @param requesterBot 请求者 [Bot.id]
     * @param configuration 请求者 [Bot.configuration]
     *
     * @see LoginSolver.Default
     */
    public fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver

    /**
     * 创建一个 [MiraiLogger].
     *
     * **注意**: [MiraiConsole] 会将 [net.mamoe.mirai.utils.MiraiLogger.setDefaultLoggerCreator] 设置为 `MiraiConsole::createLogger`.
     * 因此不要在 [createLogger] 中调用 [net.mamoe.mirai.utils.MiraiLogger.create]
     */
    public fun createLogger(identity: String?): MiraiLogger

    /**
     * 该前端是否支持使用 Ansi 输出彩色信息
     *
     * 注: 若为 `true`, 建议携带 `org.fusesource.jansi:jansi`
     */
    public val isAnsiSupported: Boolean get() = false

    /**
     * 前端预先定义的 [LoggerController], 以允许前端使用自己的配置系统
     */
    public val loggerController: LoggerController get() = LoggerControllerImpl()

    ///////////////////////////////////////////////////////////////////////////
    // ConsoleDataScope
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Mirai Console 内置的一些 [PluginConfig] 和 [PluginData] 的管理器.
     *
     * 建议实现为 lazy:
     * ```
     * override val consoleDataScope: MiraiConsoleImplementation.ConsoleDataScope by lazy {
     *     MiraiConsoleImplementation.ConsoleDataScope.createDefault(
     *         coroutineContext,
     *         dataStorageForBuiltIns,
     *         configStorageForBuiltIns
     *     )
     * }
     * ```
     *
     * @since 2.10.0-RC
     */
    public val consoleDataScope: ConsoleDataScope

    /**
     * Mirai Console 内置的一些 [PluginConfig] 和 [PluginData] 的管理器.
     *
     * @since 2.10.0-RC
     */
    @ConsoleFrontEndImplementation
    @NotStableForInheritance
    public interface ConsoleDataScope {
        public val dataHolder: AutoSavePluginDataHolder
        public val configHolder: AutoSavePluginDataHolder
        public fun addAndReloadConfig(config: PluginConfig)

        /**
         * @since 2.11.0-RC
         */
        public fun <T : PluginData> find(type: KClass<T>): T?

        /**
         * @since 2.11.0-RC
         */
        public fun <T : PluginData> get(type: KClass<T>): T =
            find(type) ?: throw NoSuchElementException(type.qualifiedName)

        public fun reloadAll()

        /**
         * @since 2.10.0-RC
         */
        @ConsoleFrontEndImplementation
        public companion object {
            /**
             * @since 2.11.0-RC
             */
            public inline fun <reified T : PluginData> ConsoleDataScope.find(): T? = find(T::class)

            /**
             * @since 2.11.0-RC
             */
            public inline fun <reified T : PluginData> ConsoleDataScope.get(): T = get(T::class)

            @JvmStatic
            public fun createDefault(
                coroutineContext: CoroutineContext,
                dataStorage: PluginDataStorage,
                configStorage: PluginDataStorage
            ): ConsoleDataScope = ConsoleDataScopeImpl(coroutineContext, dataStorage, configStorage)
        }
    }


    /// Hooks & Backend Access
    /**
     * 后端 在 [phase] 阶段执行前会调用此方法, 如果此方法抛出了一个错误会直接中断 console 初始化
     *
     * @since 2.5.0-dev-2
     */
    public fun prePhase(phase: String) {}

    /**
     * 后端 在 [phase] 阶段执行后会调用此方法, 如果此方法抛出了一个错误会直接中断 console 初始化
     *
     * @since 2.5.0-dev-2
     */
    public fun postPhase(phase: String) {}

    /**
     * 后端在 [start] 前会调用此方法
     *
     * @since 2.5.0-dev-2
     */
    public fun preStart() {}

    /**
     * 后端在 [start] 后会调用此方法
     *
     * @since 2.5.0-dev-2
     */
    public fun postStart() {}

    /**
     * 前端访问后端内部实现的桥
     *
     * @see backendAccess
     * @since 2.5.0-dev-2
     */
    @ConsoleFrontEndImplementation
    public interface BackendAccess {
        // GlobalComponentStorage
        /**
         * 在 Mirai Console 第一个 phase 之后会包含内建 storages.
         */
        public val globalComponentStorage: ComponentStorage

        // PluginManagerImpl.resolvedPlugins
        public val resolvedPlugins: MutableList<Plugin>

        /**
         * @since 2.10.0-RC
         */
        public fun createDefaultJvmPluginLoader(coroutineContext: CoroutineContext): JvmPluginLoader =
            BuiltInJvmPluginLoaderImpl(coroutineContext)

        /**
         * @since 2.10.0-RC
         */
        public fun createDefaultCommandManager(coroutineContext: CoroutineContext): CommandManager =
            CommandManagerImpl(coroutineContext)
    }

    /**
     * @see BackendAccess
     * @since 2.5.0-dev-2
     * @throws IllegalStateException 当前端实例不是 `this` 时抛出
     */
    public val backendAccess: BackendAccess
        get() = backendAccessInstance


    ///////////////////////////////////////////////////////////////////////////
    // ConsoleLaunchOptions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Console 启动参数, 修改参数会改变默认行为
     * @since 2.10.0-RC
     */
    @ConsoleExperimentalApi
    public class ConsoleLaunchOptions {
        @JvmField
        public var crashWhenPluginLoadFailed: Boolean = false
    }

    @ConsoleExperimentalApi
    public val consoleLaunchOptions: ConsoleLaunchOptions
        get() = ConsoleLaunchOptions()

    @ConsoleFrontEndImplementation
    public companion object {
        private val backendAccessInstance = object : BackendAccess {
            override val globalComponentStorage: ComponentStorage get() = getBridge().globalComponentStorage
            override val resolvedPlugins: MutableList<Plugin> get() = MiraiConsole.pluginManagerImpl.resolvedPlugins
        }

        init {
            Runtime.getRuntime().addShutdownHook(thread(false) {
                if (instanceInitialized) {
                    runBlocking { MiraiConsole.job.cancelAndJoin() }
                }
            })
        }

        @Volatile
        internal var currentBridge: MiraiConsoleImplementationBridge? = null
        internal val instanceInitialized: Boolean get() = currentBridge != null

        private val initLock = ReentrantLock()

        /**
         * 可由前端调用, 获取当前的 [MiraiConsoleImplementation] 实例. 注意该实例不是 [start] 时传递的实例, 而会是 [MiraiConsoleImplementationBridge].
         *
         * 必须在 [start] 之后才能使用, 否则抛出 [UninitializedPropertyAccessException].
         */
        @JvmStatic
        @ConsoleFrontEndImplementation
        public fun getInstance(): MiraiConsoleImplementation =
            currentBridge ?: throw UninitializedPropertyAccessException()

        /**
         * @since 2.11
         */
        internal fun getBridge(): MiraiConsoleImplementationBridge =
            currentBridge ?: throw UninitializedPropertyAccessException()

        /** 由前端调用, 初始化 [MiraiConsole] 实例并启动 */
        @JvmStatic
        @ConsoleFrontEndImplementation
        @Throws(MalformedMiraiConsoleImplementationError::class)
        public fun MiraiConsoleImplementation.start(): Unit = initLock.withLock {
            val currentBridge = currentBridge
            if (currentBridge != null && currentBridge.isActive) {
                error(
                    "Mirai Console is already initialized and is currently running. " +
                            "Run MiraiConsole.cancel to kill old instance before starting another instance."
                )
            }
            val newBridge = MiraiConsoleImplementationBridge(this)
            this@Companion.currentBridge = newBridge
            kotlin.runCatching {
                newBridge.doStart()
            }.onFailure { e ->
                kotlin.runCatching {
                    MiraiConsole.mainLogger.error("Failed to init MiraiConsole.", e)
                }.onFailure {
                    e.printStackTrace()
                }

                kotlin.runCatching {
                    MiraiConsole.cancel()
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }
}