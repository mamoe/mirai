/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.util.PluginServiceHelper.findServices
import net.mamoe.mirai.console.internal.util.PluginServiceHelper.loadAllServices
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.plugin.loader.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoadException
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.verbose
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal object BuiltInJvmPluginLoaderImpl :
    AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>(".jar"),
    CoroutineScope by MiraiConsole.childScope("JvmPluginLoader", CoroutineExceptionHandler { _, throwable ->
        BuiltInJvmPluginLoaderImpl.logger.error("Unhandled Jar plugin exception: ${throwable.message}", throwable)
    }),
    JvmPluginLoader {

    override val configStorage: PluginDataStorage
        get() = MiraiConsoleImplementationBridge.configStorageForJvmPluginLoader

    @JvmStatic
    internal val logger: MiraiLogger = MiraiConsole.createLogger(JvmPluginLoader::class.simpleName!!)

    override val dataStorage: PluginDataStorage
        get() = MiraiConsoleImplementationBridge.dataStorageForJvmPluginLoader

    internal val classLoaders: MutableList<JvmPluginClassLoader> = mutableListOf()

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER") // doesn't matter
    override fun getPluginDescription(plugin: JvmPlugin): JvmPluginDescription = plugin.description

    private val pluginFileToInstanceMap: MutableMap<File, JvmPlugin> = ConcurrentHashMap()

    override fun Sequence<File>.extractPlugins(): List<JvmPlugin> {
        ensureActive()

        fun Sequence<Map.Entry<File, JvmPluginClassLoader>>.findAllInstances(): Sequence<Map.Entry<File, JvmPlugin>> {
            return onEach { (_, pluginClassLoader) ->
                val exportManagers = pluginClassLoader.findServices(
                    ExportManager::class
                ).loadAllServices()
                if (exportManagers.isEmpty()) {
                    val rules = pluginClassLoader.getResourceAsStream("export-rules.txt")
                    if (rules == null)
                        pluginClassLoader.declaredFilter = StandardExportManagers.AllExported
                    else rules.bufferedReader(Charsets.UTF_8).useLines {
                        pluginClassLoader.declaredFilter = ExportManagerImpl.parse(it.iterator())
                    }
                } else {
                    pluginClassLoader.declaredFilter = exportManagers[0]
                }
            }.map { (f, pluginClassLoader) ->
                f to pluginClassLoader.findServices(
                    JvmPlugin::class,
                    KotlinPlugin::class,
                    JavaPlugin::class
                ).loadAllServices()
            }.flatMap { (f, list) ->

                list.associateBy { f }.asSequence()
            }
        }

        val filePlugins = this.filterNot {
            pluginFileToInstanceMap.containsKey(it)
        }.associateWith {
            JvmPluginClassLoader(it, MiraiConsole::class.java.classLoader, classLoaders)
        }.onEach { (_, classLoader) ->
            classLoaders.add(classLoader)
        }.asSequence().findAllInstances().onEach {
            //logger.verbose { "Successfully initialized JvmPlugin ${loaded}." }
        }.onEach { (file, plugin) ->
            pluginFileToInstanceMap[file] = plugin
        } + pluginFileToInstanceMap.asSequence()

        return filePlugins.toSet().map { it.value }
    }

    private val loadedPlugins = ConcurrentHashMap<JvmPlugin, Unit>()

    @Throws(PluginLoadException::class)
    override fun load(plugin: JvmPlugin) {
        ensureActive()

        if (loadedPlugins.put(plugin, Unit) != null) {
            error("Plugin '${plugin.name}' is already loaded and cannot be reloaded.")
        }
        logger.verbose { "Loading plugin ${plugin.description.smartToString()}" }
        runCatching {
            check(plugin is JvmPluginInternal) { "A JvmPlugin must extend AbstractJvmPlugin to be loaded by JvmPluginLoader.BuiltIn" }
            plugin.internalOnLoad()
        }.getOrElse {
            throw PluginLoadException("Exception while loading ${plugin.description.smartToString()}", it)
        }
        val nameFolder = PluginManager.pluginsDataPath.resolve(plugin.description.name).toFile()
        if (plugin.description.name != plugin.description.id && nameFolder.exists()) {
            // need move
            val idFolder = PluginManager.pluginsDataPath.resolve(plugin.description.id).toFile()
            val moveDescription = "移动 ${plugin.description.smartToString()} 的配置目录(${nameFolder.path})到 ${idFolder.path}"
            if (idFolder.exists()) {
                if (idFolder.listFiles()?.size != 0) {
                    logger.error("$moveDescription 失败, 原因:配置目录(${idFolder.path})被占用")
                    logger.error("Mirai Console 将自动关闭, 请删除或移动该目录后再启动")
                    MiraiConsole.job.cancel()
                } else
                    idFolder.delete()
            }
            kotlin.runCatching {
                logger.info(moveDescription)
                if (!nameFolder.renameTo(idFolder)) {
                    logger.error("$moveDescription 失败")
                    logger.error("Mirai Console 将自动关闭, 请手动移动该文件夹后再启动")
                    MiraiConsole.job.cancel()
                }
            }.onFailure {
                logger.error("$moveDescription 失败, 原因:\n", it)
                logger.error("Mirai Console 将自动关闭, 请解决该错误后再启动")
                MiraiConsole.job.cancel()
            }
            logger.info("$moveDescription 完成")
        }
    }

    override fun enable(plugin: JvmPlugin) {
        if (plugin.isEnabled) error("Plugin '${plugin.name}' is already enabled and cannot be re-enabled.")
        ensureActive()
        runCatching {
            logger.verbose { "Enabling plugin ${plugin.description.smartToString()}" }
            if (plugin is JvmPluginInternal) {
                plugin.internalOnEnable()
            } else plugin.onEnable()

            // Extra space for logging align
            logger.verbose { "Enabled  plugin ${plugin.description.smartToString()}" }
        }.getOrElse {
            throw PluginLoadException("Exception while loading ${plugin.description.name}", it)
        }
    }

    override fun disable(plugin: JvmPlugin) {
        if (!plugin.isEnabled) error("Plugin '${plugin.name}' is not already disabled and cannot be re-disabled.")

        if (MiraiConsole.isActive)
            ensureActive()

        if (plugin is JvmPluginInternal) {
            plugin.internalOnDisable()
        } else plugin.onDisable()
    }
}
