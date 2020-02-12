/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.plugin

import Command
import kotlinx.coroutines.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.encodeToString
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


abstract class PluginBase(coroutineContext: CoroutineContext) : CoroutineScope {
    constructor() : this(EmptyCoroutineContext)

    private val supervisorJob = SupervisorJob()
    final override val coroutineContext: CoroutineContext = coroutineContext + supervisorJob

    val dataFolder: File by lazy {
        File(PluginManager.pluginsPath + pluginDescription.name).also { it.mkdir() }
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
    open fun onCommand(command: Command, args: List<String>) {

    }


    internal fun enable() {
        this.onEnable()
    }


    fun loadConfig(fileName: String): Config {
        return Config.load(File(fileName))
    }


    @JvmOverloads
    internal fun disable(throwable: CancellationException? = null) {
        this.coroutineContext[Job]!!.cancelChildren(throwable)
        this.onDisable()
    }

    private lateinit var pluginDescription: PluginDescription

    internal fun init(pluginDescription: PluginDescription) {
        this.pluginDescription = pluginDescription
        this.onLoad()
    }

    fun getPluginManager() = PluginManager

    val logger: MiraiLogger by lazy {
        DefaultLogger(pluginDescription.name)
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
            val content = content_.split("\n")

            var name = "Plugin"
            var author = "Unknown"
            var basePath = "net.mamoe.mirai.PluginMain"
            var info = "Unknown"
            var version = "1.0.0"
            val depends = mutableListOf<String>();

            content.forEach {
                val line = it.trim()
                val lowercaseLine = line.toLowerCase()
                if (it.contains(":")) {
                    when {
                        lowercaseLine.startsWith("name") -> {
                            name = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("author") -> {
                            author = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("info") || lowercaseLine.startsWith("information") -> {
                            info = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("main") || lowercaseLine.startsWith("path") || lowercaseLine.startsWith(
                            "basepath"
                        ) -> {
                            basePath = line.substringAfter(":").trim()
                        }
                        lowercaseLine.startsWith("version") || lowercaseLine.startsWith("ver") -> {
                            version = line.substringAfter(":").trim()
                        }
                    }
                } else if (line.startsWith("-")) {
                    depends.add(line.substringAfter("-").trim())
                }
            }
            return PluginDescription(name, author, basePath, version, info, depends)
        }
    }
}

internal class PluginClassLoader(file: File, parent: ClassLoader) : URLClassLoader(arrayOf(file.toURI().toURL()), parent)

object PluginManager {
    internal val pluginsPath = System.getProperty("user.dir") + "/plugins/".replace("//", "/").also {
        File(it).mkdirs()
    }

    val logger = DefaultLogger("Mirai Plugin Manager")

    //已完成加载的
    private val nameToPluginBaseMap: MutableMap<String, PluginBase> = mutableMapOf()

    fun onCommand(command: Command, args: List<String>) {
        this.nameToPluginBaseMap.values.forEach {
            it.onCommand(command, args)
        }
    }

    /**
     * 尝试加载全部插件
     */
    fun loadPlugins() {
        val pluginsFound: MutableMap<String, PluginDescription> = mutableMapOf()
        val pluginsLocation: MutableMap<String, File> = mutableMapOf()

        File(pluginsPath).listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                val jar = JarFile(file)
                val pluginYml =
                    jar.entries().asSequence().filter { it.name.toLowerCase().contains("plugin.yml") }.firstOrNull()
                if (pluginYml == null) {
                    logger.info("plugin.yml not found in jar " + jar.name + ", it will not be consider as a Plugin")
                } else {
                    val description =
                        PluginDescription.readFromContent(URL("jar:file:" + file.absoluteFile + "!/" + pluginYml.name).openConnection().inputStream.use {
                            it.readBytes().encodeToString()
                        })
                    pluginsFound[description.name] = description
                    pluginsLocation[description.name] = file
                }
            }
        }

        fun checkNoCircularDepends(
            target: PluginDescription,
            needDepends: List<String>,
            existDepends: MutableList<String>
        ) {

            if (!target.noCircularDepend) {
                return
            }

            existDepends.add(target.name)

            if (needDepends.any { existDepends.contains(it) }) {
                target.noCircularDepend = false
            }

            existDepends.addAll(needDepends)

            needDepends.forEach {
                if (pluginsFound.containsKey(it)) {
                    checkNoCircularDepends(pluginsFound[it]!!, pluginsFound[it]!!.depends, existDepends)
                }
            }
        }


        pluginsFound.values.forEach {
            checkNoCircularDepends(it, it.depends, mutableListOf())
        }

        //load


        fun loadPlugin(description: PluginDescription): Boolean {
            if (!description.noCircularDepend) {
                logger.error("Failed to load plugin " + description.name + " because it has circular dependency")
                return false
            }

            //load depends first
            description.depends.forEach { dependent ->
                if (!pluginsFound.containsKey(dependent)) {
                    logger.error("Failed to load plugin " + description.name + " because it need " + dependent + " as dependency")
                    return false
                }
                val depend = pluginsFound[dependent]!!
                //还没有加载
                if (!depend.loaded && !loadPlugin(pluginsFound[dependent]!!)) {
                    logger.error("Failed to load plugin " + description.name + " because " + dependent + " as dependency failed to load")
                    return false
                }
            }
            //在这里所有的depends都已经加载了


            //real load
            logger.info("loading plugin " + description.name)

            try {
                val pluginClass = try {
                    PluginClassLoader((pluginsLocation[description.name]!!), this.javaClass.classLoader)
                        .loadClass(description.basePath)
                } catch (e: ClassNotFoundException) {
                    logger.info("failed to find Main: " + description.basePath + " checking if it's kotlin's path")
                    PluginClassLoader((pluginsLocation[description.name]!!), this.javaClass.classLoader)
                        .loadClass("${description.basePath}Kt")
                }
                return try {
                    val subClass = pluginClass.asSubclass(PluginBase::class.java)
                    val plugin: PluginBase = subClass.getDeclaredConstructor().newInstance()
                    description.loaded = true
                    logger.info("successfully loaded plugin " + description.name + " version " + description.version + " by " + description.author)
                    logger.info(description.info)

                    nameToPluginBaseMap[description.name] = plugin
                    plugin.init(description)
                    true
                } catch (e: ClassCastException) {
                    logger.error("failed to load plugin " + description.name + " , Main class does not extends PluginBase ")
                    false
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                logger.error("failed to load plugin " + description.name + " , Main class not found under " + description.basePath)
                return false
            }
        }

        pluginsFound.values.forEach {
            loadPlugin(it)
        }

        nameToPluginBaseMap.values.forEach {
            it.enable()
        }
    }


    @JvmOverloads
    fun disableAllPlugins(throwable: CancellationException? = null) {
        nameToPluginBaseMap.values.forEach {
            it.disable(throwable)
        }
    }
}



