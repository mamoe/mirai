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
import net.mamoe.mirai.console.internal.data.cast
import net.mamoe.mirai.console.internal.data.createInstanceOrNull
import net.mamoe.mirai.console.plugin.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugin.PluginLoadException
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.util.childScope
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info
import java.io.File
import java.io.InputStream
import java.net.URLClassLoader

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

    override fun Sequence<File>.extractPlugins(): List<JvmPlugin> {
        ensureActive()

        fun Sequence<ClassLoader>.loadAll(): Sequence<JvmPlugin> {
            return mapNotNull { pluginClassLoader ->
                pluginClassLoader.getResourceAsStream(JvmPlugin::class.qualifiedName!!)?.use(InputStream::readBytes)
                    ?.let(::String)?.let { it to pluginClassLoader }
            }.mapNotNull { (pluginQualifiedName, classLoader) ->
                kotlin.runCatching {
                    val clazz =
                        Class.forName(pluginQualifiedName, true, classLoader).cast<Class<out JvmPlugin>>()
                    clazz.kotlin.objectInstance
                        ?: clazz.kotlin.createInstanceOrNull() ?: clazz.newInstance()
                }.getOrElse {
                    logger.error(
                        { "Could not load PluginLoader ${pluginQualifiedName}." },
                        PluginLoadException("Could not load PluginLoader ${pluginQualifiedName}.", it)
                    )
                    return@mapNotNull null
                }.also {
                    logger.info { "Successfully loaded PluginLoader ${pluginQualifiedName}." }
                }
            }
        }

        val filePlugins = this.map {
            URLClassLoader(arrayOf(it.toURI().toURL()), MiraiConsole::class.java.classLoader)
        }.onEach { classLoader ->
            classLoaders.add(classLoader)
        }.loadAll()

        return filePlugins.toSet().toList()
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