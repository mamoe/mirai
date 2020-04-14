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
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.events.EventListener
import net.mamoe.mirai.console.scheduler.PluginScheduler
import net.mamoe.mirai.console.scheduler.SchedulerTaskManagerInstance
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 所有插件的基类
 */
abstract class PluginBase
@JvmOverloads constructor(coroutineContext: CoroutineContext = EmptyCoroutineContext) : CoroutineScope {

    private val supervisorJob = SupervisorJob()
    final override val coroutineContext: CoroutineContext = coroutineContext + supervisorJob

    private var loaded = false
    private var enabled = false

    /**
     * 插件被分配的数据目录。数据目录会与插件名称同名。
     */
    val dataFolder: File by lazy {
        File(PluginManager.pluginsPath + "/" + PluginManager.lastPluginName)
            .also { it.mkdir() }
    }

    /**
     * 当一个插件被加载时调用
     */
    open fun onLoad() {

    }

    /**
     * 当所有插件全部被加载后被调用
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
     * 加载一个 [dataFolder] 中的 [Config]
     */
    fun loadConfig(fileName: String): Config {
        return Config.load(dataFolder.absolutePath + "/" + fileName)
    }

    val logger: MiraiLogger by lazy {
        SimpleLogger("Plugin $pluginName") { priority, message, e ->
            val identityString = "[${pluginName}]"
            MiraiConsole.logger(priority, identityString, 0, message)
            if (e != null) {
                MiraiConsole.logger(priority, identityString, 0, e)
            }
        }
    }

    /**
     * 加载 resources 中的文件
     */
    fun getResources(fileName: String): InputStream? {
        return try {
            this.javaClass.classLoader.getResourceAsStream(fileName)
        } catch (e: Exception) {
            PluginManager.getFileInJarByName(
                this.pluginName,
                fileName
            )
        }
    }

    /**
     * 加载 resource 中的 [Config]
     * 这个 [Config] 是只读的
     */
    fun getResourcesConfig(fileName: String): Config {
        require(fileName.contains(".")) { "Unknown Config Type" }
        return Config.load(getResources(fileName) ?: error("No such file: $fileName"), fileName.substringAfter('.'))
    }

    // internal

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

    /**
     * Java API Scheduler
     */
    private var scheduler: PluginScheduler? = null
    fun getScheduler(): PluginScheduler {
        if (scheduler === null) {
            scheduler = SchedulerTaskManagerInstance.getPluginScheduler(this)
        }
        return scheduler!!
    }

    /**
     * Java API EventListener
     */
    private var eventListener: EventListener? = null
    fun getEventListener(): EventListener {
        if (eventListener === null) {
            eventListener = EventListener(this)
        }
        return eventListener!!
    }

}

/**
 * 插件描述
 */
class PluginDescription(
    val file: File,
    val name: String,
    val author: String,
    val basePath: String,
    val version: String,
    val info: String,
    val depends: List<String>,//插件的依赖
    internal var loaded: Boolean = false,
    internal var noCircularDepend: Boolean = true
) {
    override fun toString(): String {
        return "name: $name\nauthor: $author\npath: $basePath\nver: $version\ninfo: $info\ndepends: $depends"
    }

    companion object {
        fun readFromContent(content_: String, file: File): PluginDescription {
            with(Config.load(content_, "yml")) {
                try {
                    return PluginDescription(
                        file = file,
                        name = this.getString("name"),
                        author = kotlin.runCatching {
                            this.getString("author")
                        }.getOrElse {
                            "unknown"
                        },
                        basePath = kotlin.runCatching {
                            this.getString("path")
                        }.getOrElse {
                            this.getString("main")
                        },
                        version = kotlin.runCatching {
                            this.getString("version")
                        }.getOrElse {
                            "unknown"
                        },
                        info = kotlin.runCatching {
                            this.getString("info")
                        }.getOrElse {
                            "unknown"
                        },
                        depends = kotlin.runCatching {
                            this.getStringList("depends")
                        }.getOrElse {
                            listOf()
                        }
                    )
                } catch (e: Exception) {
                    error("Failed to read Plugin.YML")
                }
            }
        }
    }
}
