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
import net.mamoe.mirai.console.pure.MiraiConsoleUIPure
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import net.mamoe.mirai.utils.SimpleLogger.LogPriority
import java.io.File
import java.io.InputStream
import java.net.URLClassLoader
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


abstract class PluginBase
@JvmOverloads constructor(coroutineContext: CoroutineContext = EmptyCoroutineContext) : CoroutineScope {

    private val supervisorJob = SupervisorJob()
    final override val coroutineContext: CoroutineContext = coroutineContext + supervisorJob

    /**
     * 插件被分配的数据目录。数据目录会与插件名称同名。
     */
    val dataFolder: File by lazy {
        File(PluginManager.pluginsPath + "/" + PluginManager.lastPluginName).also {
            it.mkdir()
        }
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
     * 当任意指令被使用
     */
    open fun onCommand(command: Command, sender: CommandSender, args: List<String>) {

    }


    internal fun enable() {
        this.onEnable()
    }

    /**
     * 加载一个data folder中的Config
     * 这个config是read-write的
     */
    fun loadConfig(fileName: String): Config {
        return Config.load(dataFolder.absolutePath + "/" + fileName)
    }

    @JvmOverloads
    internal fun disable(throwable: CancellationException? = null) {
        this.coroutineContext[Job]!!.cancelChildren(throwable)
        try {
            this.onDisable()
        } catch (e: Exception) {
            logger.info(e)
        }
    }

    internal var pluginName: String = ""

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
     * 这个 [Config] 是 read-only 的
     */
    fun getResourcesConfig(fileName: String): Config {
        if (!fileName.contains(".")) {
            error("Unknown Config Type")
        }
        return Config.load(getResources(fileName) ?: error("No such file: $fileName"), fileName.substringAfter('.'))
    }
}

class PluginDescription(
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
        fun readFromContent(content_: String): PluginDescription {
            with(Config.load(content_, "yml")) {
                try {
                    return PluginDescription(
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

internal class PluginClassLoader(file: File, parent: ClassLoader) :
    URLClassLoader(arrayOf(file.toURI().toURL()), parent)
