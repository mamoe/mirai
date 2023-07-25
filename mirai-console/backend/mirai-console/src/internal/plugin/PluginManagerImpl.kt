/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.internal.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extensions.PluginLoaderProvider
import net.mamoe.mirai.console.internal.data.mkdir
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.plugin.NotYetLoadedPlugin
import net.mamoe.mirai.console.plugin.Plugin
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.description
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.safeLoader
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.loader.PluginLoadException
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.plugin.name
import net.mamoe.mirai.console.util.SemVersion
import net.mamoe.mirai.utils.*
import java.io.File
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

internal val PluginManager.impl: PluginManagerImpl get() = this.cast()

internal class PluginManagerImpl(
    private val parentCoroutineContext: CoroutineContext
) : PluginManager, CoroutineScope by parentCoroutineContext.childScope("PluginManager") {

    override val pluginsPath: Path = MiraiConsole.rootPath.resolve("plugins").apply { mkdir() }
    override val pluginsFolder: File = pluginsPath.toFile()
    override val pluginsDataPath: Path = MiraiConsole.rootPath.resolve("data").apply { mkdir() }
    override val pluginsDataFolder: File = pluginsDataPath.toFile()
    override val pluginsConfigPath: Path = MiraiConsole.rootPath.resolve("config").apply { mkdir() }
    override val pluginsConfigFolder: File = pluginsConfigPath.toFile()

    override val pluginLibrariesPath: Path = MiraiConsole.rootPath.resolve("plugin-libraries").apply { mkdir() }
    override val pluginLibrariesFolder: File = pluginLibrariesPath.toFile()

    override val pluginSharedLibrariesPath: Path =
        MiraiConsole.rootPath.resolve("plugin-shared-libraries").apply { mkdir() }
    override val pluginSharedLibrariesFolder: File = pluginSharedLibrariesPath.toFile()

    @Suppress("ObjectPropertyName")
    private val _pluginLoaders: MutableList<PluginLoader<*, *>> by lazy {
        builtInLoaders.toMutableList()
    }

    private val logger = MiraiLogger.Factory.create(PluginManager::class, "plugin")

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

    override fun getPluginDescription(plugin: Plugin): PluginDescription =
        plugin.safeLoader.getPluginDescription(plugin)

    init {
        // Kotlin coroutine job cancelling ordering:
        // - sub job 0 invokeOnCompletion called
        // - sub job 1 invokeOnCompletion called
        // - sub job N invokeOnCompletion called
        // - parent    invokeOnCompletion called
        // So we need register a child job to control plugins' disabling order
        this.childScopeContext("PluginManager shutdown monitor").job.invokeOnCompletion {
            plugins.asReversed().forEach { plugin ->
                if (plugin.isEnabled) {
                    disablePlugin(plugin)
                }
            }
        }

        resolvedPlugins.add(MiraiConsoleAsPlugin)
    }

    // region LOADING

    private fun <P : Plugin, D : PluginDescription> PluginLoader<P, D>.loadPluginNoEnable(plugin: P) {
        kotlin.runCatching {
            this.load(plugin)

            resolvedPlugins.add(
                when (plugin) {
                    is NotYetLoadedPlugin<*> -> plugin.resolve()

                    else -> plugin
                }
            )

        }.fold(
            onSuccess = {
                logger.info { "Successfully loaded plugin ${getPluginDescription(plugin).smartToString()}" }
            },
            onFailure = {
                logger.info { "Cannot load plugin ${getPluginDescription(plugin).smartToString()}" }
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
                logger.info { "Successfully enabled plugin ${getPluginDescription(plugin).smartToString()}" }
            },
            onFailure = {
                logger.info { "Cannot enable plugin ${getPluginDescription(plugin).smartToString()}" }
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
    @Throws(PluginResolutionException::class)
    private fun findAndSortAllPluginsUsingBuiltInLoaders(): List<PluginDescriptionWithLoader> {
        val allDescriptions =
            builtInLoaders.listAndSortAllPlugins()
                .asSequence()
                .onEach { (_, descriptions) ->
                    descriptions.let(::checkPluginDescription)
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
        GlobalComponentStorage.useEachExtensions(PluginLoaderProvider) {
            logger.info { "Loaded PluginLoader ${extension.instance} from ${plugin?.name ?: "<builtin>"}" }
            _pluginLoaders.add(extension.instance)
            count++
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
        resolvedPlugins.forEach { enablePlugin(it) }
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
            loader.listPlugins().map { plugin -> getPluginDescription(plugin).wrapWith(loader, plugin) }
        }.sortByDependencies()
    }

    @Throws(PluginResolutionException::class)
    private fun <D : PluginDescription> List<D>.sortByDependencies(): List<D> {
        val alreadyLoadedPlugins = resolvedPlugins.asSequence().map { it.description }.toList() // snapshot

        val originPluginDescriptions = this@sortByDependencies
        val pending2BeResolved = originPluginDescriptions.toMutableList()
        val resolved = ArrayList<D>(pending2BeResolved.size)

        fun <T> MutableCollection<T>.filterAndRemove(output: MutableCollection<T>, filter: (T) -> Boolean) {
            this.removeAll { item ->
                if (filter(item)) {
                    output.add(item)
                    true
                } else false
            }
        }

        // Step0. Check non contains death-locked dependencies graph.
        kotlin.run deathLockDependenciesCheck@{
            fun D.checkDependencyLink(list: MutableList<D>) {
                if (this in list) {
                    list.add(this)
                    throw PluginInfiniteCircularDependencyReferenceException(
                        "Found circular plugin dependency: " + list.joinToString(" -> ") { it.id }
                    )
                }
                list.add(this)
                this.dependencies.forEach { dependency ->
                    // In this step not care about dependency missing.
                    val dep0 = pending2BeResolved.findDependency(dependency) ?: return@forEach
                    dep0.checkDependencyLink(list)
                }
                list.removeLast()
            }

            pending2BeResolved.forEach { dependency ->
                dependency.checkDependencyLink(mutableListOf())
            }
        }

        // Step1. Fast process no-depended plugins
        pending2BeResolved.filterAndRemove(resolved) { it.dependencies.isEmpty() }

        // Step2. Check plugin dependencies graph
        kotlin.run checkDependenciesMissing@{
            val errorMsgs = mutableListOf<String>()
            pending2BeResolved.forEach { pluginDesc ->
                val missed = pluginDesc.dependencies.filter { dependency ->
                    val resolvedDep = originPluginDescriptions.findDependency(dependency)
                        ?: alreadyLoadedPlugins.findDependency(dependency)

                    if (resolvedDep != null) {
                        resolvedDep.checkSatisfies(dependency, pluginDesc)
                        false
                    } else !dependency.isOptional
                }
                if (missed.isNotEmpty()) {
                    errorMsgs.add(
                        "Cannot load plugin '${pluginDesc.name}', missing dependencies: ${
                            missed.joinToString(
                                ", "
                            ) { "'$it'" }
                        }"
                    )
                }
            }
            if (errorMsgs.isNotEmpty()) {
                throw PluginMissingDependencyException(errorMsgs.joinToString("\n"))
            }
        }

        // Step3. Sort plugins with dependencies
        var loopStart = 0 // For faster performance
        sortWithOptionalDependencies@
        while (true) {
            fun searchRemainingDependencies(start: Int, dependency: PluginDependency): Int {
                for (i in (start + 1) until pending2BeResolved.size) {
                    val dep0 = pending2BeResolved[i]
                    if (dep0.id.equals(dependency.id, ignoreCase = true)) {
                        dep0.checkSatisfies(dependency, pending2BeResolved[i])
                        return i
                    }
                }
                return -1
            }

            for (index in loopStart until pending2BeResolved.size) {
                // Ensure load after all depended plugins
                val dep = pending2BeResolved[index]
                for (pluginDependency in dep.dependencies) {
                    val dependencyIndex = searchRemainingDependencies(index, pluginDependency)
                    if (dependencyIndex != -1) {
                        pending2BeResolved.removeAt(index)
                        pending2BeResolved.add(dependencyIndex, dep)
                        continue@sortWithOptionalDependencies
                    }
                }
                loopStart = index + 1
            }

            resolved.addAll(pending2BeResolved)
            pending2BeResolved.clear()
            break@sortWithOptionalDependencies
        }

        return resolved
    }


    @Suppress("FunctionName")
    @TestOnly
    internal fun <D : PluginDescription> __sortPluginDescription(list: List<D>): List<D> {
        return list.sortByDependencies()
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

internal fun <T : PluginDescription> List<T>.findDependency(dependency: PluginDependency): T? {
    return find { it.id.equals(dependency.id, ignoreCase = true) }
}

internal fun PluginDescription.checkSatisfies(dependency: PluginDependency, plugin: PluginDescription) {
    val requirement = dependency.versionRequirement ?: return
    if (!SemVersion.parseRangeRequirement(requirement).test(this.version)) {
        throw PluginLoadException("Plugin '${plugin.id}' ('${plugin.id}') requires '${dependency.id}' with version $requirement while the resolved is ${this.version}")
    }
}
