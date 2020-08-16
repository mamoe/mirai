/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("WRONG_MODIFIER_CONTAINING_DECLARATION", "unused")
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.MiraiConsole.INSTANCE
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.center.PluginCenter
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.util.*


/**
 * mirai-console 实例
 *
 * @see INSTANCE
 * @see MiraiConsoleImplementation
 */
public interface MiraiConsole : CoroutineScope {
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
     * @return 不可变 [List] ([java.util.Collections.unmodifiableList])
     */
    public val builtInPluginLoaders: List<PluginLoader<*, *>>

    public val buildDate: Date

    public val version: String

    @ConsoleExperimentalAPI
    public val pluginCenter: PluginCenter

    @ConsoleExperimentalAPI
    public fun newLogger(identity: String?): MiraiLogger

    public companion object INSTANCE : MiraiConsole by MiraiConsoleImplementationBridge {
        /**
         * 获取 [MiraiConsole] 的 [Job]
         */ // MiraiConsole.INSTANCE.getJob()
        public val job: Job
            get() = MiraiConsole.coroutineContext[Job]
                ?: error("Internal error: Job not found in MiraiConsole.coroutineContext")
    }
}

public class IllegalMiraiConsoleImplementationError @JvmOverloads constructor(
    public override val message: String? = null,
    public override val cause: Throwable? = null
) : Error()

