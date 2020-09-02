/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
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
import net.mamoe.mirai.console.plugin.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugin.PluginLoadException
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.info
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap

internal object JarPluginLoaderImpl :
    AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>(".jar"),
    CoroutineScope by MiraiConsole.childScope("JarPluginLoader", CoroutineExceptionHandler { _, throwable ->
        JarPluginLoaderImpl.logger.error("Unhandled Jar plugin exception: ${throwable.message}", throwable)
    }),
    JarPluginLoader {

    override val configStorage: PluginDataStorage
        get() = MiraiConsoleImplementationBridge.configStorageForJarPluginLoader

    @JvmStatic
    internal val logger: MiraiLogger = MiraiConsole.createLogger(JarPluginLoader::class.simpleName!!)

    override val dataStorage: PluginDataStorage
        get() = MiraiConsoleImplementationBridge.dataStorageForJarPluginLoader

    internal val classLoaders: MutableList<ClassLoader> = mutableListOf()

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER") // doesn't matter
    override val JvmPlugin.description: JvmPluginDescription
        get() = this.description

    private val pluginFileToInstanceMap: MutableMap<File, JvmPlugin> = ConcurrentHashMap()

    override fun Sequence<File>.extractPlugins(): List<JvmPlugin> {
        ensureActive()

        fun Sequence<Map.Entry<File, ClassLoader>>.findAllInstances(): Sequence<Map.Entry<File, JvmPlugin>> {
            return map { (f, pluginClassLoader) ->
                f to pluginClassLoader.findServices(
                    JvmPlugin::class,
                    KotlinPlugin::class,
                    AbstractJvmPlugin::class,
                    JavaPlugin::class
                ).loadAllServices()
            }.flatMap { (f, list) ->
                list.associateBy { f }.asSequence()
            }
        }

        val filePlugins = this.filterNot {
            pluginFileToInstanceMap.containsKey(it)
        }.associateWith {
            URLClassLoader(arrayOf(it.toURI().toURL()), MiraiConsole::class.java.classLoader)
        }.onEach { (_, classLoader) ->
            classLoaders.add(classLoader)
        }.asSequence().findAllInstances().onEach { loaded ->
            logger.info { "Successfully initialized JvmPlugin ${loaded}." }
        }.onEach { (file, plugin) ->
            pluginFileToInstanceMap[file] = plugin
        } + pluginFileToInstanceMap.asSequence()

        return filePlugins.toSet().map { it.value }
    }

    @Throws(PluginLoadException::class)
    override fun load(plugin: JvmPlugin) {
        ensureActive()
        runCatching {
            if (plugin is JvmPluginInternal) {
                plugin.internalOnLoad()
            } else plugin.onLoad()
        }.getOrElse {
            throw PluginLoadException("Exception while loading ${plugin.description.name}", it)
        }
    }

    override fun enable(plugin: JvmPlugin) {
        if (plugin.isEnabled) return
        ensureActive()
        if (plugin is JvmPluginInternal) {
            plugin.internalOnEnable()
        } else plugin.onEnable()
    }

    override fun disable(plugin: JvmPlugin) {
        if (!plugin.isEnabled) return
        ensureActive()

        if (plugin is JvmPluginInternal) {
            plugin.internalOnDisable()
        } else plugin.onDisable()
    }
}