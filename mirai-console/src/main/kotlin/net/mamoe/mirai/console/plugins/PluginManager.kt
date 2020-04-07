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
import java.net.JarURLConnection
import java.net.URL
import java.util.jar.JarFile


object PluginManager {
    internal val pluginsPath = (System.getProperty("user.dir") + "/plugins/").replace("//", "/").also {
        File(it).mkdirs()
    }

    private val logger = SimpleLogger("Plugin Manager") { p, message, e ->
        MiraiConsole.logger(p, "[Plugin Manager]", 0, message)
        MiraiConsole.logger(p, "[Plugin Manager]", 0, e)
    }

    /**
     * 加载成功的插件, 名字->插件
     */
    private val nameToPluginBaseMap: MutableMap<String, PluginBase> = mutableMapOf()

    /**
     * 加载成功的插件, 名字->插件摘要
     */
    private val pluginDescriptions: MutableMap<String, PluginDescription> = mutableMapOf()

    /**
     * 加载插件的PluginsLoader
     */
    private val pluginsLoader: PluginsLoader = PluginsLoader(this.javaClass.classLoader)

    /**
     * 插件优先级队列
     * 任何操作应该按这个Sequence顺序进行
     * 他的优先级取决于依赖,
     * 在这个队列中, 被依赖的插件会在依赖的插件之前
     */
    private val pluginsSequence: MutableList<PluginBase> = mutableListOf()


    /**
     * 广播Command方法
     */
    internal fun onCommand(command: Command, sender: CommandSender, args: List<String>) {
        pluginsSequence.forEach {
            try {
                it.onCommand(command, sender, args)
            } catch (e: Throwable) {
                logger.info(e)
            }
        }
    }

    /**
     * 通过插件获取介绍
     */
    fun getPluginDescription(base: PluginBase): PluginDescription {
        nameToPluginBaseMap.forEach { (s, pluginBase) ->
            if (pluginBase == base) {
                return pluginDescriptions[s]!!
            }
        }
        error("can not find plugin description")
    }

    /**
     * 获取所有插件摘要
     */
    fun getAllPluginDescriptions(): Collection<PluginDescription> {
        return pluginDescriptions.values
    }


    @Volatile
    internal var lastPluginName: String = ""



    /**
     * 寻找所有安装的插件（在文件夹）, 并将它读取, 记录位置
     * 这个不等同于加载的插件, 可以理解为还没有加载的插件
     */
    data class FindPluginsResult(
        val pluginsLocation: MutableMap<String, File>,
        val pluginsFound: MutableMap<String, PluginDescription>
    )

    internal fun findPlugins():FindPluginsResult{
        val pluginsLocation: MutableMap<String, File> = mutableMapOf()
        val pluginsFound: MutableMap<String, PluginDescription> = mutableMapOf()

        File(pluginsPath).listFiles()?.forEach { file ->
            if (file != null && file.extension == "jar") {
                val jar = JarFile(file)
                val pluginYml =
                    jar.entries().asSequence().filter { it.name.toLowerCase().contains("plugin.yml") }.firstOrNull()

                if (pluginYml == null) {
                    logger.info("plugin.yml not found in jar " + jar.name + ", it will not be consider as a Plugin")
                } else {
                    try {
                        val description = PluginDescription.readFromContent(
                            URL("jar:file:" + file.absoluteFile + "!/" + pluginYml.name).openConnection().let {
                                val res = it.inputStream.use { input ->
                                    input.readBytes().encodeToString()
                                }
                                // 关闭jarFile，解决热更新插件问题
                                (it as JarURLConnection).jarFile.close()
                                res
                            })
                        pluginsFound[description.name] = description
                        pluginsLocation[description.name] = file
                    } catch (e: Exception) {
                        logger.info(e)
                    }
                }
            }
        }
        return FindPluginsResult(pluginsLocation, pluginsFound)
    }

    /**
     * 尝试加载全部插件
     */
    fun loadPlugins() {
        logger.info("""开始加载${pluginsPath}下的插件""")
        val findPluginsResult = findPlugins()
        val pluginsFound = findPluginsResult.pluginsFound
        val pluginsLocation = findPluginsResult.pluginsLocation

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

        //load plugin individually
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

                if (!loadPlugin(depend)) {//先加载depend
                    logger.error("Failed to load plugin " + description.name + " because " + dependent + " as dependency failed to load")
                    return false
                }
            }

            logger.info("loading plugin " + description.name)

            val jarFile = pluginsLocation[description.name]!!
            val pluginClass = try{
                pluginsLoader.loadPluginMainClassByJarFile(description.name,description.basePath,jarFile)
            } catch (e: ClassNotFoundException) {
                pluginsLoader.loadPluginMainClassByJarFile(description.name,"${description.basePath}Kt",jarFile)
            }

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
            pluginsSequence.add(plugin)//按照实际加载顺序加入队列
            return true
        }


        //清掉优先级队列, 来重新填充
        pluginsSequence.clear()

        pluginsFound.values.forEach {
            try{
                // 尝试加载插件
                loadPlugin(it)
            }catch (e: Throwable) {
                pluginsLoader.remove(it.name)
                when(e){
                    is ClassCastException -> logger.error("failed to load plugin " + it.name + " , Main class does not extends PluginBase",e)
                    is ClassNotFoundException -> logger.error("failed to load plugin " + it.name + " , Main class not found under " + it.basePath,e)
                    is NoClassDefFoundError -> logger.error("failed to load plugin " + it.name + " , dependent class not found.",e)
                    else -> logger.error("failed to load plugin " + it.name,e)
                }
            }
        }


        pluginsSequence.forEach {
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

        pluginsSequence.forEach {
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
        pluginsLoader.remove(plugin.pluginName)
        pluginsSequence.remove(plugin)
    }


    @JvmOverloads
    fun disablePlugins(throwable: CancellationException? = null) {
        CommandManager.clearPluginsCommands()
        pluginsSequence.forEach {
            it.disable(throwable)
        }
        nameToPluginBaseMap.clear()
        pluginDescriptions.clear()
        pluginsLoader.clear()
        pluginsSequence.clear()
    }


    /**
     * 根据插件名字找Jar的文件
     * null => 没找到
     * 这里的url的jarFile没关，热更新插件可能出事
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
     * 这里的url的jarFile没关，热更新插件可能出事
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

