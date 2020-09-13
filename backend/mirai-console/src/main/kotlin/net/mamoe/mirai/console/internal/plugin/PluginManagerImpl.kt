/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.internal.data.cast
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.loader.PluginLoadException
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.CoroutineScopeUtils.childScope
import net.mamoe.mirai.utils.info
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList

internal object PluginManagerImpl : PluginManager, CoroutineScope by MiraiConsole.childScope("PluginManager") {

    override val pluginsPath: Path = MiraiConsole.rootPath.resolve("plugins").apply { mkdir() }
    override val pluginsFolder: File = pluginsPath.toFile()
    override val pluginsDataPath: Path = MiraiConsole.rootPath.resolve("data").apply { mkdir() }
    override val pluginsDataFolder: File = pluginsDataPath.toFile()
    override val pluginsConfigPath: Path = MiraiConsole.rootPath.resolve("config").apply { mkdir() }
    override val pluginsConfigFolder: File = pluginsConfigPath.toFile()

    @Suppress("ObjectPropertyName")
    private val _pluginLoaders: MutableList<PluginLoader<*, *>> by lazy {
        builtInLoaders.toMutableList()
    }

    private val logger = MiraiConsole.createLogger("plugin")

    @JvmField
    internal val resolvedPlugins: MutableList<Plugin> =
        CopyOnWriteArrayList() // write operations are mostly performed on init
    override val plugins: List<Plugin>
        get() = resolvedPlugins.toList()
    override val builtInLoaders: List<PluginLoader<*, *>> by lazy {
        MiraiConsole.builtInPluginLoaders.map { it.value }
    }
    override val pluginLoaders: List<PluginLoader<*, *>>
        get() = _pluginLoaders.toList()

    override val Plugin.description: PluginDescription
        get() = if (this is JvmPlugin) {
            this.safeLoader.getPluginDescription(this)
        } else resolvedPlugins.firstOrNull { it == this }
            ?.loader?.cast<PluginLoader<Plugin, PluginDescription>>()
            ?.getPluginDescription(this)
            ?: error("Plugin is unloaded")


    init {
        MiraiConsole.coroutineContext[Job]!!.invokeOnCompletion {
            plugins.forEach { it.disable() }
        }
    }

    // region LOADING

    private fun <P : Plugin, D : PluginDescription> PluginLoader<P, D>.loadPluginNoEnable(plugin: P) {
        kotlin.runCatching {
            this.load(plugin)
            resolvedPlugins.add(plugin)
        }.fold(
            onSuccess = {
                logger.info { "Successfully loaded plugin ${getPluginDescription(plugin).name}" }
            },
            onFailure = {
                logger.info { "Cannot load plugin ${getPluginDescription(plugin).name}" }
                throw it
            }
        )
    }

    private fun <P : Plugin, D : PluginDescription> PluginLoader<P, D>.enablePlugin(plugin: Plugin) {
        kotlin.runCatching {
            @Suppress("UNCHECKED_CAST")
            this.enable(plugin as P)
        }.fold(
            onSuccess = {
                logger.info { "Successfully enabled plugin ${plugin.description.name}" }
            },
            onFailure = {
                logger.info { "Cannot enable plugin ${plugin.description.name}" }
                throw it
            }
        )
    }

    internal class PluginLoadSession(
        val allKindsOfPlugins: List<PluginDescriptionWithLoader>,
    )

    ///////////////////////////////////////////////////////////////////////////
    // Phase #0:
    // - initialize all plugins using builtin loaders
    // - sort by dependencies
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 使用 [builtInLoaders] 寻找所有插件, 并初始化其主类.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(PluginMissingDependencyException::class)
    private fun findAndSortAllPluginsUsingBuiltInLoaders(): List<PluginDescriptionWithLoader> {
        val allDescriptions =
            builtInLoaders.listAndSortAllPlugins()
                .asSequence()
                .onEach { (_, descriptions) ->
                    descriptions.let(PluginManagerImpl::checkPluginDescription)
                }

        return allDescriptions.toList().sortByDependencies()
    }

    internal fun loadAllPluginsUsingBuiltInLoaders() {
        for ((l, _, p) in findAndSortAllPluginsUsingBuiltInLoaders()) {
            l.loadPluginNoEnable(p)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Phase #1:
    // - load PluginLoaderProvider
    ///////////////////////////////////////////////////////////////////////////

    internal fun initExternalPluginLoaders(): Int {
        var count = 0
        GlobalComponentStorage.run {
            PluginLoaderProvider.useExtensions { ext, plugin ->
                logger.info { "Loaded PluginLoader ${ext.instance} from ${plugin.name}" }
                _pluginLoaders.add(ext.instance)
                count++
            }
        }
        return count
    }

    // Phase #2
    internal fun scanPluginsUsingPluginLoadersIncludingThoseFromPluginLoaderProvider(): PluginLoadSession {
        return PluginLoadSession(_pluginLoaders.filterNot { builtInLoaders.contains(it) }.listAndSortAllPlugins())
    }

    internal fun loadPlugins(session: PluginLoadSession) {
        session.allKindsOfPlugins.forEach { it.loader.load(it.plugin) }
    }

    internal fun enableAllLoadedPlugins() {
        resolvedPlugins.forEach { it.enable() }
    }

    @kotlin.jvm.Throws(PluginLoadException::class)
    internal fun checkPluginDescription(description: PluginDescription) {
        kotlin.runCatching {
            PluginDescription.checkPluginDescription(description)
        }.getOrElse {
            throw PluginLoadException("PluginDescription check failed.", it)
        }
    }

    private fun List<PluginLoader<*, *>>.listAndSortAllPlugins(): List<PluginDescriptionWithLoader> {
        return flatMap { loader ->
            loader.listPlugins().map { plugin -> plugin.description.wrapWith(loader, plugin) }
        }.sortByDependencies()
    }

    @Throws(PluginMissingDependencyException::class)
    private fun <D : PluginDescription> List<D>.sortByDependencies(): List<D> {
        val resolved = ArrayList<D>(this.size)

        fun D.canBeLoad(): Boolean = this.dependencies.all { dependency ->
            val target = resolved.findDependency(dependency)
            if (target == null) {
                dependency.isOptional
            } else {
                target.checkSatisfies(dependency, this@canBeLoad)
                true
            }
        }

        fun List<D>.consumeLoadable(): List<D> {
            val (canBeLoad, cannotBeLoad) = this.partition { it.canBeLoad() }
            resolved.addAll(canBeLoad)
            return cannotBeLoad
        }

        fun Collection<PluginDependency>.filterIsMissing(): List<PluginDependency> =
            this.filterNot { it.isOptional || resolved.findDependency(it) != null }

        fun List<D>.doSort() {
            if (this.isEmpty()) return

            val beforeSize = this.size
            this.consumeLoadable().also { resultPlugins ->
                check(resultPlugins.size < beforeSize) {
                    throw PluginMissingDependencyException(resultPlugins.joinToString("\n") { badPlugin ->
                        "Cannot load plugin ${badPlugin.name}, missing dependencies: ${
                            badPlugin.dependencies.filterIsMissing().joinToString()
                        }"
                    })
                }
            }.doSort()
        }

        this.doSort()
        return resolved
    }
}

internal data class PluginDescriptionWithLoader(
    @JvmField val loader: PluginLoader<Plugin, PluginDescription>, // easier type
    @JvmField val delegate: PluginDescription,
    @JvmField val plugin: Plugin,
) : PluginDescription by delegate

@Suppress("UNCHECKED_CAST")
internal fun <D : PluginDescription> PluginDescription.unwrap(): D =
    if (this is PluginDescriptionWithLoader) this.delegate as D else this as D

@Suppress("UNCHECKED_CAST")
internal fun PluginDescription.wrapWith(loader: PluginLoader<*, *>, plugin: Plugin): PluginDescriptionWithLoader =
    PluginDescriptionWithLoader(
        loader as PluginLoader<Plugin, PluginDescription>, this, plugin
    )

internal fun List<PluginDescription>.findDependency(dependency: PluginDependency): PluginDescription? {
    return find { it.id.equals(dependency.id, ignoreCase = true) }
}

internal fun PluginDescription.checkSatisfies(dependency: PluginDependency, plugin: PluginDescription) {
    val requirement = dependency.versionRequirement
    if (requirement != null && this.version !in requirement) {
        throw PluginLoadException("Plugin '${plugin.id}' ('${plugin.id}') requires '${dependency.id}' with version $requirement while the resolved is ${this.version}")
    }
}
