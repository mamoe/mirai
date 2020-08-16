/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.console

import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import kotlin.annotation.AnnotationTarget.*


/**
 * 标记一个仅用于 [MiraiConsole] 前端实现的 API. 这些 API 只应由前端实现者使用, 而不应该被插件或其他调用者使用.
 *
 * 前端实现时
 */
@Retention(AnnotationRetention.SOURCE)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
public annotation class ConsoleFrontEndImplementation

/**
 * 由前端实现这个接口
 *
 * @see  MiraiConsoleImplementation.start
 */
@ConsoleFrontEndImplementation
public interface MiraiConsoleImplementation : CoroutineScope {
    /**
     * Console 运行路径
     */
    public val rootDir: File

    /**
     * Console 前端接口
     */
    @ConsoleExperimentalAPI
    public val frontEnd: MiraiConsoleFrontEnd

    /**
     * 与前端交互所使用的 Logger
     */
    public val mainLogger: MiraiLogger

    /**
     * 内建加载器列表, 一般需要包含 [JarPluginLoader].
     *
     * @return 不可变的 [List]
     */
    public val builtInPluginLoaders: List<PluginLoader<*, *>>

    public val consoleCommandSender: ConsoleCommandSender

    public val settingStorageForJarPluginLoader: SettingStorage
    public val settingStorageForBuiltIns: SettingStorage

    public companion object {
        internal lateinit var instance: MiraiConsoleImplementation
        private val initLock = ReentrantLock()

        /** 由前端调用, 初始化 [MiraiConsole] 实例, 并启动 */
        @JvmStatic
        @ConsoleFrontEndImplementation
        public fun MiraiConsoleImplementation.start(): Unit = initLock.withLock {
            this@Companion.instance = this
            MiraiConsoleImplementationBridge.doStart()
        }
    }
}
