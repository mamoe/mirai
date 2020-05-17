/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package net.mamoe.mirai.console.plugins

import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.event.EventListener
import net.mamoe.mirai.console.scheduler.PluginScheduler
import net.mamoe.mirai.utils.MiraiLogger
import java.io.File
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 所有插件的基类
 */
abstract class PluginBase
@JvmOverloads constructor(coroutineContext: CoroutineContext = EmptyCoroutineContext) : CoroutineScope {
    final override val coroutineContext: CoroutineContext = coroutineContext + SupervisorJob()

    /**
     * 插件被分配的数据目录。数据目录会与插件名称同名。
     */
    val dataFolder: File by lazy {
        TODO()
        /*
        File(PluginManager.pluginsPath + "/" + PluginManager.lastPluginName)
            .also { it.mkdir() }*/
    }

    /**
     * 当一个插件被加载时调用
     */
    open fun onLoad() {

    }

    /**
     * 当插件被启用时调用.
     * 此时所有其他插件都已经被调用了 [onLoad]
     */
    open fun onEnable() {

    }

    /**
     * 当插件关闭前被调用
     */
    open fun onDisable() {

    }

    /**
     * 当任意指令被使用时调用.
     *
     * 指令调用将优先触发 [Command.onCommand], 若该函数返回 `false`, 则不会调用 [PluginBase.onCommand]
     */
    open fun onCommand(command: Command, sender: CommandSender, args: List<String>) {

    }

    /**
     * 插件的日志
     */
    val logger: MiraiLogger by lazy {
        TODO()
        /*
        SimpleLogger("Plugin $pluginName") { priority, message, e ->
            val identityString = "[${pluginName}]"
            MiraiConsole.logger(priority, identityString, 0, message)
            if (e != null) {
                MiraiConsole.logger(priority, identityString, 0, e)
            }
        }*/
    }

    /**
     * 加载 resources 中的文件
     */
    fun getResources(fileName: String): InputStream? {
        return try {
            this.javaClass.classLoader.getResourceAsStream(fileName)
        } catch (e: Exception) {
            TODO()

            /*
            PluginManager.getFileInJarByName(
                this.pluginName,
                fileName
            )*/
        }
    }


    /**
     * Java API Scheduler
     */
    val scheduler: PluginScheduler? = PluginScheduler(this.coroutineContext)

    /**
     * Java API EventListener
     */
    val eventListener: EventListener = EventListener(@Suppress("LeakingThis") this)


    // internal

    private var loaded = false
    private var enabled = false

    internal fun load() {
        if (!loaded) {
            onLoad()
            loaded = true
        }
    }

    internal fun enable() {
        if (!enabled) {
            onEnable()
            enabled = true
        }
    }

    internal fun disable(throwable: CancellationException? = null) {
        if (enabled) {
            this.coroutineContext[Job]!!.cancelChildren(throwable)
            try {
                onDisable()
            } catch (e: Exception) {
                logger.error(e)
            }
            enabled = false
        }
    }

    internal var pluginName: String = ""
}
