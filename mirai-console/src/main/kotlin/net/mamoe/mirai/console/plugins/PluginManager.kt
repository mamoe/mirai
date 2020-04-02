/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "unused")

package net.mamoe.mirai.console.plugins

import kotlinx.coroutines.CancellationException
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.encodeToString
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File
import java.io.InputStream
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.JarFile


object PluginManager {
    internal val pluginsPath = (System.getProperty("user.dir") + "/plugins/").replace("//", "/").also {
        File(it).mkdirs()
    }

    private val logger = SimpleLogger("Plugin Manager") { p, message, e ->
        MiraiConsole.logger(p, "[Plugin Manager]", 0, message)
        MiraiConsole.logger(p, "[Plugin Manager]", 0, e)
    }

    //已完成加载的
    private val nameToPluginBaseMap: MutableMap<String, PluginBase> = mutableMapOf()
    private val pluginDescriptions: MutableMap<String, PluginDescription> = mutableMapOf()

    internal fun onCommand(command: Command, sender: CommandSender, args: List<String>) {
        nameToPluginBaseMap.values.forEach {
            try {
                it.onCommand(command, sender, args)
            } catch (e: Throwable) {
                logger.info(e)
            }
        }
    }


    fun getPluginDescription(base: PluginBase): PluginDescription {
        nameToPluginBaseMap.forEach { (s, pluginBase) ->
            if (pluginBase == base) {
                return pluginDescriptions[s]!!
            }
        }
        error("can not find plugin description")
    }

    fun getAllPluginDescriptions(): Collection<PluginDescription> {
        return pluginDescriptions.values
    }


    @Volatile
    internal var lastPluginName: String = ""

    /**
     * 尝试加载全部插件
     */
    fun loadPlugins() {
        val pluginsFound: MutableMap<String, PluginDescription> = mutableMapOf()
        val pluginsLocation: MutableMap<String, File> = mutableMapOf()

        logger.info("""开始加载${pluginsPath}下的插件""")

        File(pluginsPath).listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                val jar = JarFile(file)
                val pluginYml =
                    jar.entries().asSequence().filter { it.name.toLowerCase().contains("plugin.yml") }.firstOrNull()
                if (pluginYml == null) {
                    logger.info("plugin.yml not found in jar " + jar.name + ", it will not be consider as a Plugin")
                } else {
                    try {
                        val description =
                            PluginDescription.readFromContent(
                                URL("jar:file:" + file.absoluteFile + "!/" + pluginYml.name).openConnection().inputStream.use {
                                    it.readBytes().encodeToString()
                                })
                        pluginsFound[description.name] = description
                        pluginsLocation[description.name] = file
                    } catch (e: Exception) {
                        logger.info(e)
                    }
                }
            }
        }

        val pluginsClassLoader = PluginsClassLoader(pluginsLocation.values,this.javaClass.classLoader)

        //不仅要解决A->B->C->A, 还要解决A->B->C->A
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

        //load plugin
        fun loadPlugin(description: PluginDescription): Boolean {
            if (!description.noCircularDepend) {
                logger.error("Failed to load plugin " + description.name + " because it has circular dependency")
                return false
            }

            if(description.loaded || nameToPluginBaseMap.containsKey(description.name)){
                return true
            }

            description.depends.forEach { dependent ->
                if (!pluginsFound.containsKey(dependent)) {
                    logger.error("Failed to load plugin " + description.name + " because it need " + dependent + " as dependency")
                    return false
                }
                val depend = pluginsFound[dependent]!!

                if (!loadPlugin(depend)) {
                    logger.error("Failed to load plugin " + description.name + " because " + dependent + " as dependency failed to load")
                    return false
                }
            }

            logger.info("loading plugin " + description.name)

            try {
                val pluginClass = try{
                    pluginsClassLoader.loadClass(description.basePath)
                } catch (e: ClassNotFoundException) {
                    pluginsClassLoader.loadClass("${description.basePath}Kt")
                }

                return try {
                    val subClass = pluginClass.asSubclass(PluginBase::class.java)

                    lastPluginName = description.name
                    val plugin: PluginBase =
                        subClass.kotlin.objectInstance ?: subClass.getDeclaredConstructor().apply {
                            againstPermission()
                        }.newInstance()
                    plugin.dataFolder // initialize right now

                    description.loaded = true
                    logger.info("successfully loaded plugin " + description.name + " version " + description.version + " by " + description.author)
                    logger.info(description.info)

                    nameToPluginBaseMap[description.name] = plugin
                    pluginDescriptions[description.name] = description
                    plugin.pluginName = description.name
                    true
                } catch (e: ClassCastException) {
                    logger.error("failed to load plugin " + description.name + " , Main class does not extends PluginBase ")
                    false
                }
            } catch (e: ClassNotFoundException) {
                logger.error("failed to load plugin " + description.name + " , Main class not found under " + description.basePath)
                logger.error(e)
                return false
            }
        }

        pluginsFound.values.forEach {
            loadPlugin(it)
        }

        nameToPluginBaseMap.values.forEach {
            try {
                it.onLoad()
            } catch (ignored: Throwable) {
                logger.info(ignored)
                logger.info(it.pluginName + " failed to load, disabling it")
                logger.info(it.pluginName + " 推荐立即删除/替换并重启")
                if (ignored is CancellationException) {
                    disablePlugin(it,ignored)
                }else{
                    disablePlugin(it)
                }
            }
        }

        nameToPluginBaseMap.values.forEach {
            try {
                it.enable()
            } catch (ignored: Throwable) {
                logger.info(ignored)
                logger.info(it.pluginName + " failed to enable, disabling it")
                logger.info(it.pluginName + " 推荐立即删除/替换并重启")
                if (ignored is CancellationException) {
                    disablePlugin(it,ignored)
                }else{
                    disablePlugin(it)
                }
            }
        }

        logger.info("""加载了${nameToPluginBaseMap.size}个插件""")
    }

    private fun disablePlugin(
        plugin:PluginBase,
        exception: CancellationException? = null
    ){
        CommandManager.clearPluginCommands(plugin)
        plugin.disable(exception)
        nameToPluginBaseMap.remove(plugin.pluginName)
        pluginDescriptions.remove(plugin.pluginName)
    }


    @JvmOverloads
    fun disablePlugins(throwable: CancellationException? = null) {
        CommandManager.clearPluginsCommands()
        nameToPluginBaseMap.values.forEach {
            it.disable(throwable)
        }
        nameToPluginBaseMap.clear()
        pluginDescriptions.clear()
    }


    /**
     * 根据插件名字找Jar的文件
     * null => 没找到
     */
    fun getJarFileByName(pluginName: String): File? {
        File(pluginsPath).listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                val jar = JarFile(file)
                val pluginYml =
                    jar.entries().asSequence().filter { it.name.toLowerCase().contains("plugin.yml") }.firstOrNull()
                if (pluginYml != null) {
                    val description =
                        PluginDescription.readFromContent(
                            URL("jar:file:" + file.absoluteFile + "!/" + pluginYml.name).openConnection().inputStream.use {
                                it.readBytes().encodeToString()
                            })
                    if (description.name.toLowerCase() == pluginName.toLowerCase()) {
                        return file
                    }
                }
            }
        }
        return null
    }

    /**
     * 根据插件名字找Jar中的文件
     * null => 没找到
     */
    fun getFileInJarByName(pluginName: String, toFind: String): InputStream? {
        val jarFile = getJarFileByName(pluginName) ?: return null
        val jar = JarFile(jarFile)
        val toFindFile =
            jar.entries().asSequence().filter { it.name == toFind }.firstOrNull() ?: return null
        return URL("jar:file:" + jarFile.absoluteFile + "!/" + toFindFile.name).openConnection().inputStream
    }
}


private val trySetAccessibleMethod: Method? = runCatching {
    Class.forName("java.lang.reflect.AccessibleObject").getMethod("trySetAccessible")
}.getOrNull()


private fun Constructor<out PluginBase>.againstPermission() {
    kotlin.runCatching {
        trySetAccessibleMethod?.let { it.invoke(this) }
            ?: kotlin.runCatching {
                @Suppress("DEPRECATED")
                this.isAccessible = true
            }
    }
}

internal class PluginsClassLoader(files: Collection<File>, parent: ClassLoader) : URLClassLoader(files.map{it.toURI().toURL()}.toTypedArray(), parent)
