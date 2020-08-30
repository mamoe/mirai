/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.MiraiLogger
import java.nio.file.Path
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.annotation.AnnotationTarget.*
import kotlin.coroutines.CoroutineContext


/**
 * 标记一个仅用于 [MiraiConsole] 前端实现的 API.
 *
 * 这些 API 只应由前端实现者使用, 而不应该被插件或其他调用者使用.
 */
@Retention(AnnotationRetention.SOURCE)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
public annotation class ConsoleFrontEndImplementation

/**
 * 实现 [MiraiConsole] 的接口
 *
 * **注意**: 随着 Console 的更新, 在版本号 `x.y.z` 的 `y` 修改时此接口可能就会变动. 意味着前端实现着需要跟随 Console 更新.
 *
 * @see MiraiConsoleImplementation.start 启动
 */
@ConsoleFrontEndImplementation
public interface MiraiConsoleImplementation : CoroutineScope {
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
     * 内建加载器列表, 一般需要包含 [JarPluginLoader].
     *
     * @return 不可变的 [List], [Collections.unmodifiableList]
     */
    public val builtInPluginLoaders: List<PluginLoader<*, *>>

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
        @JvmDefault
        public override suspend fun sendMessage(message: Message): Unit =
            withContext(Dispatchers.IO) { sendMessageJ(message) }

        @JvmSynthetic
        @JvmDefault
        public override suspend fun sendMessage(message: String): Unit =
            withContext(Dispatchers.IO) { sendMessageJ(message) }
    }

    public val consoleCommandSender: ConsoleCommandSenderImpl

    public val dataStorageForJarPluginLoader: PluginDataStorage
    public val configStorageForJarPluginLoader: PluginDataStorage
    public val dataStorageForBuiltIns: PluginDataStorage
    public val configStorageForBuiltIns: PluginDataStorage

    /**
     * @see ConsoleInput 的实现
     */
    public val consoleInput: ConsoleInput

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
     * **注意**: [MiraiConsole] 会将 [net.mamoe.mirai.utils.DefaultLogger] 设置为 `MiraiConsole::createLogger`.
     * 因此不要在 [createLogger] 中调用 [net.mamoe.mirai.utils.DefaultLogger]
     */
    public fun createLogger(identity: String?): MiraiLogger

    public companion object {
        internal lateinit var instance: MiraiConsoleImplementation
        private val initLock = ReentrantLock()

        /** 由前端调用, 初始化 [MiraiConsole] 实例并启动 */
        @JvmStatic
        @ConsoleFrontEndImplementation
        @Throws(MalformedMiraiConsoleImplementationError::class)
        public fun MiraiConsoleImplementation.start(): Unit = initLock.withLock {
            this@Companion.instance = this
            MiraiConsoleImplementationBridge.doStart()
        }
    }
}
