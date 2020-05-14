/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugins

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import kotlin.coroutines.CoroutineContext

/**
 * 插件信息
 */
interface PluginDescription {
    val name: String
    val author: String
    val version: String
    val info: String
    val depends: List<String>
}

/**
 * 插件基类.
 *
 * 内建的插件类型:
 * - [JarPlugin]
 */
abstract class Plugin : CoroutineScope {
    abstract val description: PluginDescription
    abstract val loader: PluginLoader<*>

    @OptIn(MiraiExperimentalAPI::class)
    val logger: MiraiLogger by lazy { MiraiConsole.newLogger(description.name) }

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob(MiraiConsole.coroutineContext[Job]) + CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable)
        }

    open fun onLoaded() {}
    open fun onDisabled() {}
    open fun onEnabled() {}
}