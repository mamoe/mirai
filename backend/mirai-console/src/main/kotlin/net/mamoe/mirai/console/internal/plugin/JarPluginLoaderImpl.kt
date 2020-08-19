/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.plugin.AbstractFilePluginLoader
import net.mamoe.mirai.console.plugin.PluginLoadException
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.net.URI
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.createInstance

internal object JarPluginLoaderImpl :
    AbstractFilePluginLoader<JvmPlugin, JvmPluginDescription>(".jar"),
    CoroutineScope,
    JarPluginLoader {

    private val logger: MiraiLogger = MiraiConsole.newLogger(JarPluginLoader::class.simpleName!!)

    @ConsoleExperimentalAPI
    override val settingStorage: SettingStorage
        get() = MiraiConsoleImplementationBridge.settingStorageForJarPluginLoader

    override val coroutineContext: CoroutineContext =
        MiraiConsole.coroutineContext +
                SupervisorJob(MiraiConsole.coroutineContext[Job]) +
                CoroutineExceptionHandler { _, throwable ->
                    logger.error("Unhandled Jar plugin exception: ${throwable.message}", throwable)
                }

    private val classLoader: PluginsLoader = PluginsLoader(this.javaClass.classLoader)

    init { // delayed
        coroutineContext[Job]!!.invokeOnCompletion {
            classLoader.clear()
        }
    }

    @Suppress("EXTENSION_SHADOWED_BY_MEMBER") // doesn't matter
    override val JvmPlugin.description: JvmPluginDescription
        get() = this.description

    override fun Sequence<File>.mapToDescription(): List<JvmPluginDescription> {
        return this.associateWith { URI("jar:file:${it.absolutePath.replace('\\', '/')}!/plugin.yml").toURL() }
            .mapNotNull { (file, url) ->
                kotlin.runCatching {
                    url.readText()
                }.fold(
                    onSuccess = { yaml ->
                        Yaml.nonStrict.decodeFromString(JvmPluginDescription.serializer(), yaml)
                    },
                    onFailure = {
                        logger.error("Cannot load plugin file ${file.name}", it)
                        null
                    }
                )?.also { it._file = file }
            }
    }

    @Suppress("RemoveExplicitTypeArguments") // until Kotlin 1.4 NI
    @Throws(PluginLoadException::class)
    override fun load(description: JvmPluginDescription): JvmPlugin =
        description.runCatching<JvmPluginDescription, JvmPlugin> {
            ensureActive()
            val main = classLoader.loadPluginMainClassByJarFile(
                pluginName = name,
                mainClass = mainClassName,
                jarFile = file
            ).kotlin.run {
                objectInstance
                    ?: kotlin.runCatching { createInstance() }.getOrNull()
                    ?: (java.constructors + java.declaredConstructors)
                        .firstOrNull { it.parameterCount == 0 }
                        ?.apply { kotlin.runCatching { isAccessible = true } }
                        ?.newInstance()
            } ?: error("No Kotlin object or public no-arg constructor found")

            check(main is JvmPlugin) { "The main class of Jar plugin must extend JvmPlugin, recommending JavaPlugin or KotlinPlugin" }

            if (main is JvmPluginInternal) {
                main._description = description
                main.internalOnLoad()
            } else main.onLoad()
            main
        }.getOrElse<JvmPlugin, JvmPlugin> {
            throw PluginLoadException("Exception while loading ${description.name}", it)
        }

    override fun enable(plugin: JvmPlugin) {
        ensureActive()
        if (plugin is JvmPluginInternal) {
            plugin.internalOnEnable()
        } else plugin.onEnable()
    }

    override fun disable(plugin: JvmPlugin) {
        if (plugin is JvmPluginInternal) {
            plugin.internalOnDisable()
        } else plugin.onDisable()
    }
}